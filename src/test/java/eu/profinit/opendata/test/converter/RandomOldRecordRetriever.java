package eu.profinit.opendata.test.converter;

import java.time.Instant;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;

/**
 * Created by dm on 12/13/15.
 */
@Component
public class RandomOldRecordRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger) throws TransformException {
        if(sourceValues.get("orderNumber").getStringCellValue().equals("1403000370")) {
            Record record = new Record();
            record.setRecordId(-666L);
            record.setCurrency("CZK");
            record.setDateCreated(new java.sql.Date(Instant.now().toEpochMilli()));
            record.setMasterId("123456789");
            return record;
        }
        return null;
    }
}
