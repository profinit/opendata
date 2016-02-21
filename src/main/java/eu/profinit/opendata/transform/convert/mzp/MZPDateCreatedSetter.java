package eu.profinit.opendata.transform.convert.mzp;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

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
public class MZPDateCreatedSetter implements RecordPropertyConverter{
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        sourceValues.get("inputDate").setCellType(Cell.CELL_TYPE_STRING);
        String inputDateString = sourceValues.get("inputDate").getStringCellValue();

        if(Util.isNullOrEmpty(inputDateString)) {
            throw new TransformException("Couldn't set dateCreated. Input date string is null or empty.",
                    TransformException.Severity.PROPERTY_LOCAL);
        }

        Pattern pattern = Pattern.compile("^(\\d\\d.\\d\\d.\\d\\d\\d\\d).*");
        Matcher matcher = pattern.matcher(inputDateString);

        if(!matcher.find()) {
            throw new TransformException("Couldn't set dateCreated. No date found in input string.",
                    TransformException.Severity.PROPERTY_LOCAL);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Date date = sdf.parse(matcher.group(1));
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            record.setDateCreated(sqlDate);
        } catch (ParseException e) {
            throw new TransformException("Couldn't set dateCreated because of a parse error.", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
