package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.jaxb.OldRecordRetriever;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * Created by dm on 12/16/15.
 */
public class IdBasedOldRecordRetriever implements RecordRetriever {
    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues) throws TransformException {
        return null;
    }
}
