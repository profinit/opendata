package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Retrieval;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * A common interface for components that can disqualify source file rows from processing based on source cell values.
 */
public interface SourceRowFilter extends TransformComponent {
    /**
     * Determines whether the current retrieval should proceed on a given row based on the specified source values.
     * @param currentRetrieval The current Retrieval.
     * @param sourceValues A map of Cells from the source document. The key corresponds to the "argumentName"
     *                     attribute of the sourceFileColumn element in the mapping XML.
     * @return True if the row should be processed, false if it should be discarded.
     * @throws TransformException
     */
    boolean proceedWithRow(Retrieval currentRetrieval, Map<String, Cell> sourceValues) throws TransformException;
}
