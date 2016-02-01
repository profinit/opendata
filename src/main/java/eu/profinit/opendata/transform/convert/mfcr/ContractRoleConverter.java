package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.AuthorityRole;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by dm on 2/1/16.
 */
@Component
public class ContractRoleConverter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        if(sourceValues.get("role").getStringCellValue().equals("D")) {
            record.setAuthorityRole(AuthorityRole.CUSTOMER);
        }
        else if(sourceValues.get("role").getStringCellValue().equals("O")){
            record.setAuthorityRole(AuthorityRole.SUPPLIER);
        }
        else {
            throw new TransformException("Unrecognized authority role: " + sourceValues.get("role"),
                    TransformException.Severity.RECORD_LOCAL);
        }
    }
}
