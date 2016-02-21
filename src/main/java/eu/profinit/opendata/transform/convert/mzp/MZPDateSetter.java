package eu.profinit.opendata.transform.convert.mzp;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dm on 2/21/16.
 */
@Component
public class MZPDateSetter implements RecordPropertyConverter{
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        sourceValues.get("inputDate").setCellType(Cell.CELL_TYPE_STRING);
        String inputDateString = sourceValues.get("inputDate").getStringCellValue();

        if(Util.isNullOrEmpty(inputDateString)) {
            logger.trace("Couldn't set field " + fieldName + ". Input date string is null or empty.");
            return;
        }

        Pattern pattern = Pattern.compile("^(\\d\\d.\\d\\d.\\d{2,4}).*");
        Matcher matcher = pattern.matcher(inputDateString);

        if(!matcher.find()) {
            throw new TransformException("Couldn't set dateCreated. No date found in input string.",
                    TransformException.Severity.PROPERTY_LOCAL);
        }

        try {
            String dateString = matcher.group(1);
            String dateFormat = dateString.length() < 9 ? "dd.MM.yy" : "dd.MM.yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            Date date = sdf.parse(dateString);
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            Field field = Record.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            field.set(record, sqlDate);

        } catch (Exception e) {
            throw new TransformException("Couldn't set MZP date field", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
