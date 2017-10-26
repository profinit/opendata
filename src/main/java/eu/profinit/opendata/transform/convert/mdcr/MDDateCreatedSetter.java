package eu.profinit.opendata.transform.convert.mdcr;

import eu.profinit.opendata.transform.convert.*;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Tries to set the dateCreated on an MDCR contract. Tries all three possible source columns. If all three fail,
 * uses the authority identifier to at least get the year.
 */
@Component
public class MDDateCreatedSetter implements RecordPropertyConverter {

    @Autowired
    private DateSetter dateSetter;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String text = sourceValues.get("authorityIdentifier").getStringCellValue();
        try {
            boolean didSucceed = tryCell(record, sourceValues.get("dateFrom"), fieldName, logger)
                    || tryCell(record, sourceValues.get("dateInEffect"), fieldName, logger)
                    || tryCell(record, sourceValues.get("dateSubmitted"), fieldName, logger);

            if (!didSucceed) {
                Integer year = Integer.parseInt(text.substring(text.indexOf('/')));
                GregorianCalendar gc = new GregorianCalendar();
                gc.set(year, Calendar.JANUARY, 1);
                java.sql.Date date = new java.sql.Date(gc.getTimeInMillis());
                record.setDateCreated(date);
            }
        } catch (Exception ex) {
            String message = "Couldn't set dateCreated value for MDCR contract with identifier " + text;
            throw new TransformException(message, ex, TransformException.Severity.RECORD_LOCAL);
        }

    }

    public boolean tryCell(Record record, Cell sourceCell, String fieldName, Logger logger) {
        Map<String, Cell> map = new HashMap<>();
        map.put("inputDate", sourceCell);

        // Try parsing a string (i. e. "1.10.2015")
        if(sourceCell != null && sourceCell.getCellType() == Cell.CELL_TYPE_STRING) {
            String text = sourceCell.getStringCellValue();
            if(text.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
                DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
                try {
                    java.sql.Date date = new java.sql.Date(dateFormat.parse(text).getTime());
                    record.setDateCreated(date);
                    return true;
                } catch (ParseException e) {
                    return false;
                }
            }
            return false;
        }

        try {
            dateSetter.updateRecordProperty(record, map, fieldName, logger);
            return record.getDateCreated() != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
