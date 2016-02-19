package eu.profinit.opendata.transform.convert.justice;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by dm on 2/19/16.
 */
@Component
public class JusticeInvoiceIdentifierSetter implements RecordPropertyConverter{
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        record.setAuthorityIdentifier(getIdentifierFromSourceValues(sourceValues));
    }

    public String getIdentifierFromSourceValues(Map<String, Cell> sourceValues) {
        String categoryCode = sourceValues.get("categoryCode").getStringCellValue();
        Double serialNumber = sourceValues.get("serialNumber").getNumericCellValue();
        String serialNumberString = serialNumber.intValue() + "";
        return categoryCode + "-" + serialNumberString;
    }
}
