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
import java.util.List;
import java.util.Map;

/**
 * Creates an UnresolvedRelationship from a contract (the record argument) to an invoice identified by its
 * authorityIdentifier (argumentName = "invoiceId"). If the exact same unresolved relationship is already in the
 * database, does nothing.
 * @see UnresolvedRelationship
 * @see eu.profinit.opendata.control.RelationshipResolver
 */
@Component
public class ContractAndInvoiceCorrelator implements RecordPropertyConverter {

    private static final String RECORD_TYPE = "recordType";
    private static final String INVOICE_ID = "invoiceId";
    @PersistenceContext
    private EntityManager em;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        if(sourceValues.get(INVOICE_ID) != null
                && !Util.isNullOrEmpty(sourceValues.get(INVOICE_ID).getStringCellValue())) {

            String invoiceId = sourceValues.get(INVOICE_ID).getStringCellValue();

            //Make sure the relationship doesn't exist yet, resolved or not
            List<UnresolvedRelationship> ulist = em.createQuery("Select u from UnresolvedRelationship u " +
                    "where u.boundAuthorityIdentifier = :" + INVOICE_ID, UnresolvedRelationship.class)
                    .setParameter(INVOICE_ID, invoiceId)
                    .getResultList();

            List<Record> rlist = em.createQuery("Select r from Record r " +
                    "where r.authorityIdentifier = :"+INVOICE_ID+" and r.recordType = :" + RECORD_TYPE, Record.class)
                    .setParameter(INVOICE_ID, invoiceId).setParameter(RECORD_TYPE, RecordType.INVOICE)
                    .getResultList();

            if(!ulist.isEmpty()) return;
            if(!rlist.isEmpty()) {
                Record candidate = rlist.get(0);
                if(candidate.getParentRecord() != null) return;
            }

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
