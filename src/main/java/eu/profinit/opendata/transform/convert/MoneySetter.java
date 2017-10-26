package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Sets a double property specified by the fieldName. Expects a numeric cell with argumentName "inputAmount".
 */
public abstract class MoneySetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        try {
            Field field = Record.class.getDeclaredField(fieldName);
            Class<?> fieldType = field.getType();
            if (!(fieldType.isAssignableFrom(Double.class)
                    || fieldType.isAssignableFrom(Double.TYPE))) {
                throw new TransformException("Field " + fieldName + " doesn't have type Double", TransformException.Severity.FATAL);
            }
            field.setAccessible(true);

            Double inputAmount = sourceValues.get("inputAmount").getNumericCellValue();

            field.set(record, inputAmount);
        } catch (TransformException e) {
            throw e;
        } catch (Exception e) {
            String message = "Couldn't set Double value for field " + fieldName;
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }

    }
}
