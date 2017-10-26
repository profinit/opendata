package eu.profinit.opendata.transform.convert.mdcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.query.PartnerQueryService;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.PropertyBasedRecordRetriever;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dm on 5/29/16.
 */
@Component
public class MDInvoiceRetriever implements RecordRetriever {

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Autowired
    private PropertyBasedRecordRetriever propertyBasedRecordRetriever;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger)
            throws TransformException {

        try {
            // Get filter values
            String partnerName =
                    partnerQueryService.normalizeEntityName(sourceValues.get("partnerName").getStringCellValue());

            Map<String, String> filter = new HashMap<>();
            filter.put("authorityIdentifier", sourceValues.get("authorityIdentifier").getStringCellValue());
            filter.put("budgetCategory", Double.toString(sourceValues.get("budgetCategory").getNumericCellValue()));
            filter.put("subject", sourceValues.get("subject").getStringCellValue());

            Record found = propertyBasedRecordRetriever.retrieveRecordByStrings(currentRetrieval, filter, RecordType.INVOICE);
            if(found != null && found.getPartner().getName().equals(partnerName)) {
                return found;
            }

        } catch (Exception ex) {
            // We'll just return null
            logger.warn("Old record retrieval failed", ex);
        }
        return null;
    }
}
