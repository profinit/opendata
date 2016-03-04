package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the inEffect attribute of a MFCR contract. Expects a source cell with argumentName = "validity" with value "V"
 * or "U". Based on the value, sets inEffect to either true or false. The fieldName argument is ignored.
 */
@Component
public class ContractValiditySetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String vu = sourceValues.get("validity").getStringCellValue();
        if(vu.equals("V")) {
            record.setInEffect(true);
        }
        else if(vu.equals("U")) {
            record.setInEffect(false);
        }
        else {
            throw new TransformException("Unknown validity: " + vu, TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
