package eu.profinit.opendata.transform.convert.mk;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.query.PartnerQueryService;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Retrieves MK contracts based on their authorityIdentifier or, if it's null, the partner name, subject and
 * dateCreated. Only an exact match in all considered attributes counts. If the authorityIdentifier is non-null, all
 * the other attributes are ignored. If it is null, all other attributes must be non-null.
 * Throws a FATAL exception if more than one candidate record is found.
 */
@Component
public class MKContractRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger)
            throws TransformException {

        // Get filter values
        String authId = sourceValues.get("authorityIdentifier").getStringCellValue();

        String partnerName =
                partnerQueryService.normalizeEntityName(sourceValues.get("partnerName").getStringCellValue());

        String subject = sourceValues.get("subject").getStringCellValue();

        java.sql.Date dateCreated =
                new java.sql.Date(sourceValues.get("dateCreated").getDateCellValue().getTime());

        // Get all MK contracts from the DB
        List<Record> allContracts = em.createQuery(
                "Select r from Record r where r.authority = :authority and r.recordType = :contract", Record.class)
                .setParameter("authority", currentRetrieval.getDataInstance().getDataSource().getEntity())
                .setParameter("contract", RecordType.CONTRACT)
                .getResultList();

        // Filter by authId, partner name, subject and date created
        List<Record> filtered;
        if(!Util.isNullOrEmpty(authId)) {
            filtered = allContracts.stream().filter(
                    i -> i.getAuthorityIdentifier() != null && i.getAuthorityIdentifier().equals(authId))
                    .collect(Collectors.toList());
        }
        else {
            filtered = allContracts.stream().filter(i ->
                    i.getPartner().getName().equals(partnerName)
                    && i.getSubject().equals(subject)
                    && i.getDateCreated().equals(dateCreated)
            ).collect(Collectors.toList());
        }

        // Return whatever we find
        if(!filtered.isEmpty()) {
            if(filtered.size() > 1) {
                throw new TransformException("More than one old candidate contract has been found",
                        TransformException.Severity.FATAL);
            }
            else {
                return filtered.get(0);
            }
        }
        return null;
    }
}
