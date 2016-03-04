package eu.profinit.opendata.transform.convert.mzp;

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
 * Creates an UnresolvedRelationship from an invoice (the record argument) to a contract identified by its
 * authorityIdentifier (argumentName = "contractId"). If the exact same unresolved relationship is already in the
 * database, does nothing.
 * @see UnresolvedRelationship
 * @see eu.profinit.opendata.control.RelationshipResolver
 */
@Component
public class MZPCorrelator implements RecordPropertyConverter {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {


        if(sourceValues.get("contractId") != null
                && !Util.isNullOrEmpty(sourceValues.get("contractId").getStringCellValue())) {

            String contractId = sourceValues.get("contractId").getStringCellValue();

            //Make sure the relationship doesn't exist yet, resolved or not
            List<UnresolvedRelationship> ulist = em.createQuery("Select u from UnresolvedRelationship u " +
                    "where u.boundAuthorityIdentifier = :contractId", UnresolvedRelationship.class)
                    .setParameter("contractId", contractId)
                    .getResultList();

            List<Record> rlist = em.createQuery("Select r from Record r " +
                    "where r.authorityIdentifier = :contractId and r.recordType = :recordType", Record.class)
                    .setParameter("contractId", contractId).setParameter("recordType", RecordType.CONTRACT)
                    .getResultList();

            if(!ulist.isEmpty()) return;
            if(!rlist.isEmpty()) {
                Record candidate = rlist.get(0);
                if(candidate.getParentRecord() != null) return;
            }

            UnresolvedRelationship u = new UnresolvedRelationship();
            u.setBoundAuthorityIdentifier(contractId);
            u.setSavedRecord(record);
            u.setRecordType(RecordType.CONTRACT);
            u.setSavedRecordIsParent(false);

            if(record.getUnresolvedRelationships() == null) {
                record.setUnresolvedRelationships(new ArrayList<>());
            }
            record.getUnresolvedRelationships().add(u);
        }
    }
}
