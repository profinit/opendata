package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.UnresolvedRelationship;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by dm on 2/13/16.
 */
@Component
public class ContractAndInvoiceCorrelator implements RecordPropertyConverter {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        if(sourceValues.get("invoiceId") != null
                && !Util.isNullOrEmpty(sourceValues.get("invoiceId").getStringCellValue())) {

            String invoiceId = sourceValues.get("invoiceId").getStringCellValue();
            UnresolvedRelationship u = new UnresolvedRelationship();
            u.setBoundAuthorityIdentifier(invoiceId);
            u.setSavedRecord(record);
            u.setRecordType(RecordType.INVOICE);
            u.setSavedRecordIsParent(true);

            if(record.getUnresolvedRelationships() == null) {
                record.setUnresolvedRelationships(new ArrayList<>());
            }
            record.getUnresolvedRelationships().add(u);
        }
    }
}
