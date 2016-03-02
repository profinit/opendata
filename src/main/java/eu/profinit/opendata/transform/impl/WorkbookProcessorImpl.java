package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.*;
import eu.profinit.opendata.transform.jaxb.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by dm on 1/16/16.
 */
@Component
public class WorkbookProcessorImpl implements WorkbookProcessor {

    @PersistenceContext
    private EntityManager em;

    @Value("${record.requiredFields}")
    private String recordRequiredFields;

    @Autowired
    private ComponentFactory converterFactory;

    @Autowired
    private eu.profinit.opendata.control.DownloadService DownloadService;

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    // The default value is only used for testing, it's overwritten in doRetrieval
    private Logger log = LogManager.getLogger(WorkbookProcessorImpl.class);


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = {TransformException.class, RuntimeException.class})
    public void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval, Logger logger)
            throws TransformException {

        if(logger != null) {
            log = logger;
        }

        log.info("Started processing workbook");

        for(MappedSheet mappingSheet : mapping.getMappedSheet()) {

            Sheet sheet = null;
            if(mappingSheet.getName() != null) {
                sheet = workbook.getSheet(mappingSheet.getName());
                log.info("Processing sheet " + mappingSheet.getName());
            }
            else {
                sheet = workbook.getSheetAt(mappingSheet.getNumber().intValue());
                log.info("Processing sheet " + mappingSheet.getNumber());
            }

            int start_row_num = mappingSheet.getHeaderRow().intValue() + 1;
            if (retrieval.getDataInstance().getLastProcessedRow() != null) {
                start_row_num = retrieval.getDataInstance().getLastProcessedRow() + 1;
            }
            log.info("First data row will be " + start_row_num);

            // Create the column name mapping - if two or more columns share the same name, a numeric suffi
            // is appended going from left to right. First occurrence gets no suffix, second gets 01, etc.
            log.trace("Mapping column names to their indexes");
            Map<String, Integer> columnNames = new HashMap<>();
            Row headerRow = sheet.getRow(mappingSheet.getHeaderRow().intValue());
            for (int i = 1; Util.isRowEmpty(headerRow); i++) {
                headerRow = sheet.getRow(mappingSheet.getHeaderRow().intValue() + i);
                if (retrieval.getDataInstance().getLastProcessedRow() == null) {
                    start_row_num++;
                }
            }

            Iterator<Cell> cellIterator = headerRow.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String columnName = cell.getStringCellValue().trim();
                int i = 1;
                while (columnNames.containsKey(columnName)) {
                    columnName += String.format("%02d", i);
                    i++;
                }
                columnNames.put(columnName, cell.getColumnIndex());
                log.trace("Name: " + cell.getStringCellValue() + ", Index: " + cell.getColumnIndex());
            }

            for (int i = start_row_num; i <= sheet.getLastRowNum(); i++) {
                log.debug("Processing row " + i);
                try {
                    if (Util.isRowEmpty(sheet.getRow(i))) {
                        log.warn("Encountered empty row at index " + i + ", skipping");
                        continue;
                    }

                    Record record = processRow(
                            sheet.getRow(i), mappingSheet, mapping.getPropertySet(), retrieval, columnNames);

                    if (record == null) {
                        continue;
                    }

                    //A call to persist will throw a PersistenceException if all required attributes aren't filled
                    //Which means the whole transaction will blow up. We need to check manually
                    checkRecordIntegrity(record);

                    log.debug("Record finished, persisting");
                    if (retrieval.equals(record.getRetrieval()) && !retrieval.getRecords().contains(record)) {
                        retrieval.getRecords().add(record);
                        retrieval.setNumRecordsInserted(retrieval.getNumRecordsInserted() + 1);
                    } else if (record.getRecordId() != null) {
                        em.merge(record);
                    }
                    if (retrieval.getDataInstance().isIncremental()) {
                        retrieval.getDataInstance().setLastProcessedRow(i);
                    }
                } catch (TransformException ex) {
                    if (ex.getSeverity().equals(TransformException.Severity.FATAL)) {
                        throw ex;
                    } else {
                        //Property local exceptions should get caught deeper down, these are record local
                        //In case we are updating an old record, we should be fine, since we don't save or merge the record
                        //The bad row will still count as a bad record though!
                        log.warn("A record-local exception occurred, skipping row " + i + " as bad record", ex);
                        retrieval.setNumBadRecords(retrieval.getNumBadRecords() + 1);
                    }
                }
            }
            log.info("Sheet finished");
        }
    }

    private Record processRow(Row row, MappedSheet mapping, List<PropertySet> propertySets,
                              Retrieval retrieval, Map<String, Integer> columnNames) throws TransformException {

        Record record = null;
        boolean newRecord;

        // Apply a filter, if there is one in the mapping
        if(mapping.getFilter() != null && !mapping.getFilter().isEmpty()) {
            for(RowFilter rowFilter : mapping.getFilter()) {
                log.trace("Mapping specifies a filter with class " + rowFilter.getClassName() + ", instantiating");
                SourceRowFilter filter = (SourceRowFilter) instantiateComponent(rowFilter.getClassName());
                if(!filter.proceedWithRow(retrieval, getCellMapForArguments(row, rowFilter.getSourceFileColumn(), columnNames))) {
                    log.debug("Filter " + rowFilter.getClassName() + " has disqualified this row");
                    return null;
                }
            }
        }

        // Invoke an old record retriever, if there is one in the mapping
        if(mapping.getRetriever() != null) {
            log.trace("Mapping specifies a retriever with class " + mapping.getRetriever().getClassName() + ", instantiating");
            RecordRetriever retriever = (RecordRetriever) instantiateComponent(mapping.getRetriever().getClassName());
            record = retriever.retrieveRecord(retrieval,
                    getCellMapForArguments(row, mapping.getRetriever().getSourceFileColumn(), columnNames), log);
        }

        //Create the Record
        if(record == null) {
            log.debug("Creating new record");
            record = new Record();
            record.setRetrieval(retrieval);
            record.setAuthority(retrieval.getDataInstance().getDataSource().getEntity());
            newRecord = true;
        }
        else {
            log.debug("Retriever has returned an old record to update with id " + record.getRecordId());
            newRecord = false;
        }

        // For each element in the sheet mapping
        for(Object recordPropertyOrSet : mapping.getPropertyOrPropertySet()) {

            // If the mapping specifies a PropertySet, retrieve it, unpack it and process each Property in turn
            if(recordPropertyOrSet instanceof PropertySetRef) {
                String name = ((PropertySetRef) recordPropertyOrSet).getRef();
                Optional<PropertySet> propertySet = propertySets.stream()
                        .filter(i -> i.getName().equals(name)).findAny();

                if(!propertySet.isPresent()) {
                    throw new TransformException("PropertySet " + name + " is not present in the mapping",
                            TransformException.Severity.FATAL);
                }
                for(RecordProperty recordProperty : propertySet.get().getProperty()) {
                    setRecordProperty(record, recordProperty, newRecord, row, columnNames);
                }
            }
            // Otherwise just process the single Property
            else {
                setRecordProperty(record, (RecordProperty) recordPropertyOrSet, newRecord, row, columnNames);
            }

        }

        log.debug("Finished processing row");
        return record;
    }

    private void setRecordProperty(Record record, RecordProperty recordProperty, boolean newRecord,
                                   Row row, Map<String, Integer> columnNames) throws TransformException{

        log.trace("Updating property " + recordProperty.getName());

        //In case we're updating a record that's already been inserted
        if(!newRecord && recordProperty.isOnlyNewRecords()) {
            log.trace("Skipping property as it can only be applied to new records");
            return;
        }

        //For each property, either set the corresponding fixed value by resolving a string
        if(recordProperty.getValue() != null) {
            setFixedValue(record, recordProperty);
        }
        else {
            //Or instantiate and call the corresponding converter with a hashmap of arguments
            setProcessedValue(recordProperty, record, row, columnNames);
        }
    }

    /**
     * Instantiates a RecordPropertyConverter and invokes its updateRecordProperty method with arguments retrieved from
     * the source workbook row being processed.
     * @param recordProperty The RecordProperty defined in the mapping XML.
     * @param record The currently processed Record.
     * @param row The currently processed Row.
     * @param columnNames The mapping of column names to column indices in the workbook
     * @throws TransformException
     * @see RecordPropertyConverter#updateRecordProperty(Record, Map, String, Logger)
     */
    private void setProcessedValue(RecordProperty recordProperty, Record record, Row row,
                                   Map<String, Integer> columnNames) throws TransformException {

        log.trace("Instantiating property converter " + recordProperty.getConverter());
        RecordPropertyConverter rpc = (RecordPropertyConverter) instantiateComponent(recordProperty.getConverter());
        Map<String, Cell> argumentMap = getCellMapForArguments(row, recordProperty.getSourceFileColumn(), columnNames);

        try {
            rpc.updateRecordProperty(record, argumentMap, recordProperty.getName(), log);
        } catch (TransformException e) {
            if(e.getSeverity().equals(TransformException.Severity.PROPERTY_LOCAL)) {
                log.warn(e.getMessage(), e);
            }
            else throw e;
        }
    }

    /**
     * Sets a fixed value for a Record property, as defined by the RecordProperty object in the mapping file. The method
     * will automatically convert the value to the appropriate type based on the type of the field being set. This can
     * be a primitive type, a String or an enum value.
     * @param record The record on which to set the property.
     * @param recordProperty The RecordProperty defined in the mapping XML.
     * @throws TransformException In case the data type conversion fails.
     */
    private void setFixedValue(Record record, RecordProperty recordProperty) throws TransformException {
        try {
            Field field = Record.class.getDeclaredField(recordProperty.getName());
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            field.set(record, getValueFromString(recordProperty.getValue(), fieldType));
        }
        catch (IllegalAccessException | NoSuchFieldException | TransformException | RuntimeException e) {
            String message = "Couldn't set a fixed value for field " + recordProperty.getName();
            if (recordProperty.isRequired()) {
                //For IllegalAccess and NoSuchField, this will happen every time
                TransformException.Severity severity = TransformException.Severity.RECORD_LOCAL;
                if(!RuntimeException.class.equals(e.getClass())) {
                    severity = TransformException.Severity.FATAL;
                }
                throw new TransformException(message, e, severity);

            } else {
                log.warn(message, e);
            }
        }
    }

    @Override
    public void checkRecordIntegrity(Record record) throws TransformException {
        List<Field> offendingFields = new ArrayList<>();
        String[] requiredFields = recordRequiredFields.split(",");

        for(String fieldName : requiredFields) {
            try {
                Field field = Record.class.getDeclaredField(fieldName);
                field.setAccessible(true); //Is this a Bad Idea(TM)?
                if(field.get(record) == null) {
                    offendingFields.add(field);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.warn("Couldn't check integrity of field " + fieldName, e);
            }
        }

        if(!offendingFields.isEmpty()) {
            StringBuilder buffer = new StringBuilder("Finished Record is missing required values for columns: ");
            for(Field field : offendingFields) {
                buffer.append(field.getName()).append(", ");
            }
            buffer.delete(buffer.length() - 2, buffer.length() - 1);

            throw new TransformException(buffer.toString(), TransformException.Severity.RECORD_LOCAL);
        }

    }


    @SuppressWarnings("unchecked")
    private <T> T getValueFromString(String string, Class<T> type) throws TransformException {
        //Primitive, wrapper or string
        if(isSimpleType(type)) {
            return type.cast(toObject(type, string));
        }

        //Enum value
        if(type.isEnum()) {
            Enum<?> value = Enum.valueOf((Class<Enum>)type, string);
            return type.cast(value);
        }

        throw new TransformException("Cannot set a fixed value for a non-primitive, non-enum field",
                TransformException.Severity.FATAL);
    }


    /**
     * Creates the mapping between arguments passed to TransformComponents and actual workbook cells.
     * @param row The currently processed workbook row
     * @param sourceColumns SourceColumns defined in the mapping for a single element
     * @param columnNames The mapping of column names to column indices in the workbook
     * @return A map of "argumentName: cell" retrieved from the workbook row
     * @throws TransformException
     */
    private Map<String, Cell> getCellMapForArguments(Row row, List<SourceColumn> sourceColumns,
                                                     Map<String, Integer> columnNames) throws TransformException {
        Map<String, Cell> argumentMap = new HashMap<>();
        for(SourceColumn sourceColumn : sourceColumns) {
            Integer columnIndex = columnNames.get(sourceColumn.getOriginalName());
            if(columnIndex == null) {
                if(sourceColumn.getNumber() != null) {
                    columnIndex = sourceColumn.getNumber();
                }
                else {
                    log.trace("Couldn't find source coulumn with name " + sourceColumn.getOriginalName());
                    continue;
                }
            }
            argumentMap.put(sourceColumn.getArgumentName(), row.getCell(columnIndex));
        }
        return argumentMap;
    }


    private TransformComponent instantiateComponent(String className) throws TransformException {
        try {
            return converterFactory.getComponent(className);
        } catch (ClassNotFoundException | ClassCastException e) {
            String message = "Could not instantiate component " + className;
            throw new TransformException(message, e, TransformException.Severity.FATAL);
        }
    }

    //Utility
    private static final Set<Class> SIMPLE_TYPES = new HashSet<>(Arrays.asList(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            String.class, Boolean.TYPE, Character.TYPE, Byte.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE));

    private static boolean isSimpleType(Class clazz) {
        return SIMPLE_TYPES.contains(clazz);
    }

    private static Object toObject( Class clazz, String value ) {
        if( Boolean.class == clazz || Boolean.TYPE == clazz ) return Boolean.parseBoolean( value );
        if( Byte.class == clazz    || Byte.TYPE    == clazz ) return Byte.parseByte( value );
        if( Short.class == clazz   || Short.TYPE   == clazz ) return Short.parseShort( value );
        if( Integer.class == clazz || Integer.TYPE == clazz ) return Integer.parseInt( value );
        if( Long.class == clazz    || Long.TYPE    == clazz ) return Long.parseLong( value );
        if( Float.class == clazz   || Float.TYPE   == clazz ) return Float.parseFloat( value );
        if( Double.class == clazz  || Double.TYPE  == clazz ) return Double.parseDouble( value );
        return value;
    }


    //Test
    @Override
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEm() {
        return em;
    }
}
