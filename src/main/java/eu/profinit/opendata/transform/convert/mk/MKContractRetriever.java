package eu.profinit.opendata.transform.convert.mk;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.query.PartnerQueryService;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dm on 2/23/16.
 */
@Component
public class MKContractRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues)
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
                "Select r from Record r where r.authority = :authority", Record.class)
                .setParameter("authority", currentRetrieval.getDataInstance().getDataSource().getEntity())
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
