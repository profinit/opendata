package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * Created by dm on 12/4/15.
 */
public interface RecordRetriever extends TransformComponent {
    Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger) throws TransformException;
}
