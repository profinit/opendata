package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.query.RecordQueryService;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dm on 2/1/16.
 */
@Component
public class ContractParentSetter implements RecordPropertyConverter {

    @Autowired
    private RecordQueryService recordQueryService;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String sd = sourceValues.get("type").getStringCellValue();

        // Any amendments to a contract have a two-digit suffix. We search for their parents here.
        if(sd.equals("D")) {
            Map<String, String> filter = new HashMap<>();
            filter.put("authorityIdentifier", record.getAuthorityIdentifier()
                    .substring(0, record.getAuthorityIdentifier().length() - 2));

            List<Record> found = recordQueryService.findRecordsByFilter(filter, record.getRetrieval());
            if(!found.isEmpty()) {
                record.setParentRecord(found.get(0));
            }
        }
    }
}
