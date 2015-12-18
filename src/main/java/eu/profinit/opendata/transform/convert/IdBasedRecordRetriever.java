package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Map;

/**
 * Created by dm on 12/16/15.
 */
@Component
public class IdBasedRecordRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues) throws TransformException {
        if(sourceValues.containsKey("inputAuthorityIdentifier")) {
            String inputAuthorityIdentifier = sourceValues.get("inputAuthorityIdentifier").getStringCellValue();
        }

        return null;
    }
}
