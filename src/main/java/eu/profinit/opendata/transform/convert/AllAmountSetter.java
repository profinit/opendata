package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the originalCurrencyAmount. If the currency is currently set to "CZK",
 * sets the same amount into the amountCzk property as well. Expects a non-null
 * numeric cell with argument "inputAmount". Ignores the fieldName argument.
 */
@Component
public class AllAmountSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        Double amount = null;
        //fix for excels without values
        if (sourceValues.get("inputAmount") == null) {
            amount = 0d;
        } else {
            amount = sourceValues.get("inputAmount").getNumericCellValue();
        }
        record.setOriginalCurrencyAmount(amount);
        if (record.getCurrency().equals("CZK")) {
            record.setAmountCzk(amount);
        }
    }
}
