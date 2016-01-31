package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * Created by dm on 1/31/16.
 */
public interface SourceRowFilter extends TransformComponent {
    boolean proceedWithRow(Retrieval currentRetrieval, Map<String, Cell> sourceValues) throws TransformException;
}
