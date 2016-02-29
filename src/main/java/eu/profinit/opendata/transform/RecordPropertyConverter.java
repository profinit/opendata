package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * Common interface for components that process source document cells and set appropriate properties in created
 * Records. RecordPropertyConverters are instantiated inside the ApplicationContext and have full access to the
 * database and other areas of the application.
 */
public interface RecordPropertyConverter extends TransformComponent {
    /**
     * Processes passed source cells and usually sets a single property in passed Record. Some converters will
     * set multiple properties or alter the database in some other way.
     * @param record The Record to be updated.
     * @param sourceValues A map of Cells from the source document. The key corresponds to the "argumentName"
     *                     attribute of the sourceFileColumn element in the mapping XML.
     * @param fieldName The name of the field to be updated. Corresponds to the "fieldName" attribute of the
     *                  property element in the mapping XML. The implementation may choose to ignore this argument.
     * @param logger The transform logger
     * @throws TransformException
     */
    void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException;
}
