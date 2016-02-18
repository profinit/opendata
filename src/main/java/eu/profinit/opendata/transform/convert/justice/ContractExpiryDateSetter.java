package eu.profinit.opendata.transform.convert.justice;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by dm on 2/18/16.
 */
@Component
public class ContractExpiryDateSetter implements RecordPropertyConverter {

    private DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        Cell dateCell = sourceValues.get("inputDate");
        String dateString = dateCell.getStringCellValue();

        if(Util.isNullOrEmpty(dateString)) {
            logger.trace("Not setting expiry date because input cell is empty");
            return;
        }

        if(dateString.toLowerCase().equals("doba neurčitá")) {
            logger.trace("Not setting expiry date because the contract doesn't expire");
        }

        try {
            Date date = dateFormat.parse(dateString);
            record.setDateOfExpiry(new java.sql.Date(date.getTime()));
        } catch (Exception ex) {
            throw new TransformException("Couldn't set expiry date", ex, TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
