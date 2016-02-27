package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformDriver;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

/**
 * Created by dm on 12/13/15.
 */
@Component
public class DateSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        Date inputDate = sourceValues.get("inputDate").getDateCellValue();
        setField(record, inputDate, fieldName, logger);

    }

    public void setField(Record record, Date date, String fieldName, Logger logger) throws TransformException {
        try {
            Field field = Record.class.getDeclaredField(fieldName);
            Class<?> fieldType = field.getType();
            if (!fieldType.getName().equals(java.sql.Date.class.getName())) {
                throw new TransformException("Field " + fieldName + " doesn't have type java.sql.Date", TransformException.Severity.FATAL);
            }
            field.setAccessible(true);

            if(date == null) {
                logger.trace("Couldn't set Date - input String is null");
                return;
            }
            java.sql.Date dateToSet = new java.sql.Date(date.getTime());

            field.set(record, dateToSet);
        } catch (TransformException e) {
            throw e;
        } catch (Exception e) {
            String message = "Couldn't set java.sql.Date value for field " + fieldName;
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
