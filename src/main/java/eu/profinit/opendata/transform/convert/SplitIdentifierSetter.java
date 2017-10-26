package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the "authorityIdentifier" of a Record to "categoryCode - serialNumber", where both are keys in the sourceValues
 * map. The fieldName property is ignored.
 */
@Component
public class SplitIdentifierSetter implements RecordPropertyConverter{
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        record.setAuthorityIdentifier(getIdentifierFromSourceValues(sourceValues));
    }

    public String getIdentifierFromSourceValues(Map<String, Cell> sourceValues) {
        String categoryCode = sourceValues.get("categoryCode").getStringCellValue();
        Double serialNumber = sourceValues.get("serialNumber").getNumericCellValue();
        String serialNumberString = Integer.toString(serialNumber.intValue());
        return categoryCode + "-" + serialNumberString;
    }
}
