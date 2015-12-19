package eu.profinit.opendata.transform;

import eu.profinit.opendata.control.DownloadService;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import eu.profinit.opendata.transform.jaxb.RecordProperty;
import eu.profinit.opendata.transform.jaxb.SourceColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by dm on 12/2/15.
 */
@Component
public class TransformDriver {

    @PersistenceContext
    private EntityManager em;

    @Value("${record.requiredFields}")
    private String recordRequiredFields;

    @Autowired
    private ComponentFactory converterFactory;

    @Autowired
    private DownloadService DownloadService;

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").withZone(ZoneId.systemDefault());

    // The default value is only used for testing, it's overwritten in doRetrieval
    private Logger log = LogManager.getLogger(TransformDriver.class);

    public Retrieval doRetrieval(DataInstance dataInstance, String mappingFile) {
        return doRetrieval(dataInstance, mappingFile, null);
    }

    public Retrieval doRetrieval(DataInstance dataInstance, String mappingFile, InputStream inputStream) {

        ThreadContext.put("TIMESTAMP", formatter.format(Instant.now()));
        log = LogManager.getLogger("transform");
        log.info("Starting retrieval on data instance " + dataInstance.getDataInstanceId());

        Retrieval retrieval = new Retrieval();
        retrieval.setDataInstance(dataInstance);
        retrieval.setDate(Timestamp.from(Instant.now()));
        retrieval.setRecords(new ArrayList<>());

        try {
            if(inputStream == null) {
                log.info("Downloading data file from " + dataInstance.getUrl());
                inputStream = DownloadService.downloadDataFile(dataInstance);
            }

            Workbook workbook = openXLSFile(inputStream, dataInstance);
            Mapping mapping = loadMapping(mappingFile);
            processWorkbook(workbook, mapping, retrieval);

            log.info("Whole workbook procesed successfully");
            retrieval.setSuccess(true);
        }
        catch (Exception e) {
            log.error("An irrecoverable error occurred while performing transformation", e);
            retrieval.setSuccess(false);
            retrieval.setFailureReason(e.getMessage());
            retrieval.setNumRecordsInserted(0);
            retrieval.setRecords(new ArrayList<>());
        }

        ThreadContext.clearAll();
        return retrieval;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   rollbackFor = {TransformException.class, RuntimeException.class})
    private void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval) throws TransformException {
        log.info("Started processing workbook");
        Sheet sheet = workbook.getSheetAt(0);  //TODO: Mapping file should specify how to handle multiple sheets

        int start_row_num = mapping.getHeaderRow().intValue() + 1;
        if(retrieval.getDataInstance().getLastProcessedRow() != null) {
            start_row_num = retrieval.getDataInstance().getLastProcessedRow() + 1;
        }
        log.info("First data row will be " + start_row_num);

        //Create the column name mapping
        log.trace("Mapping column names to their indexes");
        Map<String, Integer> columnNames = new HashMap<>();
        Row headerRow = sheet.getRow(mapping.getHeaderRow().intValue());
        Iterator<Cell> cellIterator = headerRow.cellIterator();
        while(cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            columnNames.put(cell.getStringCellValue(), cell.getColumnIndex());
            log.trace("Name: " + cell.getStringCellValue() + ", Index: " + cell.getColumnIndex());
        }

        for(int i = start_row_num; i <= sheet.getLastRowNum(); i++) {
            log.debug("Processing row " + i);
            try {
                if(isRowEmpty(sheet.getRow(i))) {
                    log.warn("Encountered empty row at index " + i + ", skippint");
                    continue;
                }
                Record record = processRow(sheet.getRow(i), mapping, retrieval, columnNames);

                //A call to persist will throw a PersistenceException if all required attributes aren't filled
                //Which means the whole transaction will blow up. We need to check manually
                checkRecordIntegrity(record);

                log.debug("Persisting/merging record with existing id " + record.getRecordId());
                if(record.getRecordId() != null) {
                    em.merge(record);
                }
                else {
                    em.persist(record);
                    retrieval.getRecords().add(record);
                }
                retrieval.setNumRecordsInserted(retrieval.getNumRecordsInserted() + 1);
                retrieval.getDataInstance().setLastProcessedRow(i);
            }
            catch (TransformException ex) {
                if(ex.getSeverity().equals(TransformException.Severity.FATAL)) {
                    throw ex;
                }
                else {
                    //Property local exceptions should get caught deeper down, these are record local
                    //In case we are updating an old record, we should be fine, since we don't save or merge the record
                    //The bad row will still count as a bad record though!
                    log.warn("A record-local exception occurred, skipping row " + i + " as bad record", ex);
                    retrieval.setNumBadRecords(retrieval.getNumBadRecords() + 1);
                }
            }

        }
    }

    private Record processRow(Row row, Mapping mapping, Retrieval retrieval, Map<String, Integer> columnNames) throws TransformException {

        Record record = null;
        boolean newRecord;

        if(mapping.getRetriever() != null) {
            log.trace("Mapping specifies a retriever with class " + mapping.getRetriever().getClassName() + ", instantiating");
            RecordRetriever retriever = (RecordRetriever) instantiateComponent(mapping.getRetriever().getClassName());
            record = retriever.retrieveRecord(retrieval,
                    getCellMapForArguments(row, mapping.getRetriever().getSourceFileColumn(), columnNames));
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

        for(RecordProperty recordProperty : mapping.getProperty()) {
            log.trace("Updating property " + recordProperty.getName());

            //In case we're updating a record that's already been inserted
            if(!newRecord && recordProperty.isOnlyNewRecords()) {
                log.trace("Skipping property as it can only be applied to new records");
                continue;
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

        log.debug("Finished processing row");
        return record;

    }

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

    private void setFixedValue(Record record, RecordProperty recordProperty) throws TransformException {
        try {
            Field field = Record.class.getDeclaredField(recordProperty.getName());
            Class<?> fieldType = field.getType();
            field.setAccessible(true); //TODO: This is disgusting
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


    private Map<String, Cell> getCellMapForArguments(Row row, List<SourceColumn> sourceColumns,
                                                     Map<String, Integer> columnNames) throws TransformException {
        Map<String, Cell> argumentMap = new HashMap<>();
        for(SourceColumn sourceColumn : sourceColumns) {
            Integer columnIndex = columnNames.get(sourceColumn.getOriginalName());
            if(columnIndex == null) {
                throw new TransformException("Cannot find source column with name " + sourceColumn.getOriginalName(),
                        TransformException.Severity.FATAL);
            }
            argumentMap.put(sourceColumn.getArgumentName(), row.getCell(columnIndex));
        }
        return argumentMap;
    }

    public Workbook openXLSFile(InputStream inputStream, DataInstance dataInstance) throws IOException {
        log.debug("Opening file with format " + dataInstance.getFormat());
        if(dataInstance.getFormat().equals("xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            return new XSSFWorkbook(inputStream);
        }
    }


    public Mapping loadMapping(String mappingFile) throws JAXBException, IOException {
        ClassPathResource cpr = new ClassPathResource(mappingFile);
        JAXBContext jaxbContext = JAXBContext.newInstance(Mapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        return (Mapping) u.unmarshal(cpr.getFile());
        //return (Mapping) mappingJAXBElement.getValue();
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

    public static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                return false;
        }
        return true;
    }

    //Test
    public void setEm(EntityManager em) {
        this.em = em;
    }

    public EntityManager getEm() {
        return em;
    }
}
