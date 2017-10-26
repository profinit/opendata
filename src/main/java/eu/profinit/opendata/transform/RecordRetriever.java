package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * Common interface for components that look through saved records and try to determine if one already exists based on
 * passed source values or not. Each mapping can specify a single retriever that will be invoked before any property
 * setters.
 */
public interface RecordRetriever extends TransformComponent {
    /**
     * Looks through Records saved in the database and/or processed in the current Retrieval to find one that
     * corresponds to the passed sourceValues.
     * @param currentRetrieval The Retrieval object whose saved Records will be searched through along with the database
     * @param sourceValues A map of Cells from the source document. The key corresponds to the "argumentName"
     *                     attribute of the sourceFileColumn element in the mapping XML.
     * @param logger The retrieval logger.
     * @return A single Record, if found, or null. If more than one is found, a FATAL exception is thrown.
     * @throws TransformException
     */
    Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger) throws TransformException;
}
