package eu.profinit.opendata.transform.convert.justice;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dm on 2/18/16.
 */
@Component
public class ContractDateCreatedSetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String authId = sourceValues.get("authorityIdentifier").getStringCellValue();
        Pattern regex = Pattern.compile("\\d+/(?<year>\\d{4})-MSP-CES");
        Matcher matcher = regex.matcher(authId);

        if(!matcher.find()) throw new TransformException("Can't find year in authorityIdentifier",
                TransformException.Severity.RECORD_LOCAL);

        int year = Integer.parseInt(matcher.group("year"));
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(year, Calendar.JANUARY, 1);
        Date date = new Date(gc.getTimeInMillis());

        record.setDateCreated(date);

    }
}
