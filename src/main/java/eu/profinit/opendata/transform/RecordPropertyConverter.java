package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * Created by dm on 12/2/15.
 */
public interface RecordPropertyConverter extends TransformComponent {
    void updateRecordProperty(Record record, Map<String, Cell> sourceValues) throws TransformException;
}
