package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import eu.profinit.opendata.transform.jaxb.RecordProperty;
import eu.profinit.opendata.transform.jaxb.SourceColumn;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * Created by dm on 12/2/15.
 */
@Component
public class TransformDriver {

    @PersistenceContext(unitName = "postgres")
    private EntityManager em;

    @Autowired
    private ConverterFactory converterFactory;

    private Logger log = Logger.getLogger(TransformDriver.class);

    public Retrieval doRetrieval(DataInstance dataInstance, InputStream inputStream, String mappingFile) {
        Retrieval retrieval = new Retrieval();
        retrieval.setDataInstance(dataInstance);
        retrieval.setDate(Timestamp.from(Instant.now()));

        em.getTransaction().begin();
        try {
            Workbook workbook = openXLSFile(inputStream, dataInstance);
            Mapping mapping = loadMapping(mappingFile);
            processWorkbook(workbook, mapping, retrieval);
            em.getTransaction().commit();
        }
        catch (IOException | JAXBException e) {
            log.error("Couldn't process downloaded file", e);
            em.getTransaction().rollback();
            retrieval.setSuccess(false);
            retrieval.setFailureReason(e.getMessage());
        }
        catch (TransformException e) {
            log.error("An irrecoverable error occurred while performing transformation", e);
            em.getTransaction().rollback();
            retrieval.setSuccess(false);
            retrieval.setFailureReason(e.getMessage());
        }

        return retrieval;
    }

    private Workbook openXLSFile(InputStream inputStream, DataInstance dataInstance) throws IOException {
        if(dataInstance.getFormat().equals("xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            return new XSSFWorkbook(inputStream);
        }
    }

    private Mapping loadMapping(String mappingFile) throws JAXBException, IOException {
        ClassPathResource cpr = new ClassPathResource(mappingFile);
        JAXBContext jaxbContext = JAXBContext.newInstance(Mapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        JAXBElement<?> mappingJAXBElement = (JAXBElement<?>) u.unmarshal(cpr.getFile());
        return (Mapping) mappingJAXBElement.getValue();
    }

    //TODO: Supposes 1 record per row, which isn't the case even for MFCR (amendments, splits, etc.)
    //Mapping file could have a special converter that could return an old record to update
    //But then some properties shouldn't be updated in case we do find an old record - XML needs to reflect this
    private void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval) throws TransformException {
        Sheet sheet = workbook.getSheetAt(0);  //TODO: Mapping file should specify how to handle multiple sheets
        int start_row_num = mapping.getHeaderRow().intValue() + 1; //TODO: This won't work for incrementally updated files

        for(int i = start_row_num; i <= sheet.getLastRowNum(); i++) {
            try {
                Record record = processRow(sheet.getRow(i), mapping, retrieval);
                retrieval.setNumRecordsInserted(retrieval.getNumRecordsInserted() + 1);
                em.persist(record);
            }
            catch (TransformException ex) {
                if(ex.getSeverity().equals(TransformException.Severity.FATAL)) {
                    throw ex;
                }
                else {
                    //Property local exceptions should get caught deeper down, these are record local
                    //In case we are updating an old record, we should be fine, since we don't save or merge the record
                    //The bad row will still count as a bad record though!
                    retrieval.setNumBadRecords(retrieval.getNumBadRecords() + 1);
                }
            }

        }
    }


    private Record processRow(Row row, Mapping mapping, Retrieval retrieval) throws TransformException {

        //Create the Record
        Record record = new Record();
        record.setRetrieval(retrieval);
        record.setAuthority(retrieval.getDataInstance().getDataSource().getEntity());

        //Loop through RecordProperties of the mapping
        for(RecordProperty recordProperty : mapping.getProperty()) {
            //For each property, either set the corresponding fixed value by resolving a string
            if(recordProperty.getValue() != null) {
                setFixedValue(record, recordProperty);
            }

            else {
                //Or instantiate and call the corresponding converter with a hashmap of arguments
                setProcessedValue(recordProperty, record, row);
            }
        }

        return record;

    }

    private RecordPropertyConverter instantiateConverter(String className) throws TransformException {
        try {
            return converterFactory.getConverter(className);
        } catch (ClassNotFoundException | ClassCastException e) {
            String message = "Could not instantiate converter " + className;
            throw new TransformException(message, e, TransformException.Severity.FATAL);
        }
    }

    private String getSourceValue(Row row, String columnName) {
        //TODO: Write me
        return "Blah";
    }

    private void setProcessedValue(RecordProperty recordProperty, Record record, Row row) throws TransformException {
        RecordPropertyConverter rpc = instantiateConverter(recordProperty.getConverter());
        Map<String, Object> argumentMap = new HashMap<>();
        for(SourceColumn sourceColumn : recordProperty.getSourceFileColumn()) {
            argumentMap.put(sourceColumn.getArgumentName(), getSourceValue(row, sourceColumn.getOriginalName()));
        }
        try {
            rpc.updateRecordProperty(record, argumentMap);
        } catch (TransformException e) {
            if(e.getSeverity().equals(TransformException.Severity.PROPERTY_LOCAL)) {
                log.warn(e.getMessage(), e);
            }
            else throw e;
        }
    }


    private void setFixedValue(Record record, RecordProperty recordProperty) throws TransformException {
        try {
            Field field = Record.class.getField(recordProperty.getName());
            Class<?> fieldType = field.getType();
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


}
