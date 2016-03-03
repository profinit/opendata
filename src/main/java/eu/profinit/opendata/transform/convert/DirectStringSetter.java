package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Sets a string property specified by fieldName. Expects a string cell with argumentName "inputString".
 */
@Component
public class DirectStringSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger log) throws TransformException {

        try {
            Field field = Record.class.getDeclaredField(fieldName);
            Class<?> fieldType = field.getType();
            if (!fieldType.getName().equals(String.class.getName())) {
                throw new TransformException("Field " + fieldName + " doesn't have type String", TransformException.Severity.FATAL);
            }
            field.setAccessible(true);

            if(sourceValues.get("inputString").getCellType() != Cell.CELL_TYPE_STRING) {
                sourceValues.get("inputString").setCellType(Cell.CELL_TYPE_STRING);
            }

            field.set(record, sourceValues.get("inputString").getStringCellValue());
        } catch (TransformException e) {
            throw e;
        } catch (Exception e) {
            String message = "Couldn't set String value for field " + fieldName;
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }
    }

}
