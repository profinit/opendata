package eu.profinit.opendata.transform.convert.mocr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by dm on 6/29/16.
 */
@Component
public class MOContractDateSetter implements RecordPropertyConverter {
    private static final String INPUT_DATE = "inputDate";

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        try {
            if (sourceValues.get(INPUT_DATE).getCellType() == Cell.CELL_TYPE_NUMERIC) {
                Date inputDate = sourceValues.get(INPUT_DATE).getDateCellValue();
                Field field = Record.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                java.sql.Date sqlDate = new java.sql.Date(inputDate.getTime());
                field.set(record, sqlDate);
            } else {
                sourceValues.get(INPUT_DATE).setCellType(Cell.CELL_TYPE_STRING);
                String dateString = sourceValues.get(INPUT_DATE).getStringCellValue();
                String dateFormat = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                Date date = sdf.parse(dateString);
                java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                Field field = Record.class.getDeclaredField(fieldName);
                field.setAccessible(true);

                field.set(record, sqlDate);
            }

        } catch (Exception e) {
            throw new TransformException("Couldn't set MOCR date field", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        }

    }
}
