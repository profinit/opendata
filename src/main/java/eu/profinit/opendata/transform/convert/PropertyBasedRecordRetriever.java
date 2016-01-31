package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Map;

import static eu.profinit.opendata.common.Util.isNullOrEmpty;

/**
 * Created by dm on 12/16/15.
 */
@Component
public class PropertyBasedRecordRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues) throws TransformException {
        if(sourceValues.containsKey("inputAuthorityIdentifier")) {
            String inputAuthorityIdentifier = sourceValues.get("inputAuthorityIdentifier").getStringCellValue();

            if(!isNullOrEmpty(inputAuthorityIdentifier)) {
                TypedQuery<Record> query = em.createNamedQuery("findByAuthorityIdAndEntity", Record.class)
                        .setParameter("authorityIdentifier", inputAuthorityIdentifier)
                        .setParameter("authority", currentRetrieval.getDataInstance().getDataSource().getEntity());

                return query.getSingleResult();
            }
        }

        return null;
    }
}
