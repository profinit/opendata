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
 * Created by dm on 2/19/16.
 */
@Component
public class AllAmountSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        Double amount = sourceValues.get("inputAmount").getNumericCellValue();
        record.setOriginalCurrencyAmount(amount);
        if(record.getCurrency().equals("CZK")) {
            record.setAmountCzkWithVat(amount);
        }
    }
}
