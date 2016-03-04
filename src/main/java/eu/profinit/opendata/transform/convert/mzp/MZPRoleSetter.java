package eu.profinit.opendata.transform.convert.mzp;

import eu.profinit.opendata.model.AuthorityRole;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the authorityRole attribute of an MZP record. Expects a source cell with argumentName = "inputRole" with value
 * "Dodavatelská" or "Odběratelská". Based on the value, sets the authority role to either CUSTOMER or SUPPLIER.
 * The fieldName argument is ignored.
 */
@Component
public class MZPRoleSetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String inputRole = sourceValues.get("inputRole").getStringCellValue();
        if(inputRole.equals("Dodavatelská")) {
            record.setAuthorityRole(AuthorityRole.CUSTOMER);
        }
        else if(inputRole.equals("Odběratelská")) {
            record.setAuthorityRole(AuthorityRole.SUPPLIER);
        }
        else {
            throw new TransformException("Uknown authority role: " + inputRole,
                    TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
