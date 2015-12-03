package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;

import java.util.Map;

/**
 * Created by dm on 12/2/15.
 */
public interface RecordPropertyConverter {
    void updateRecordProperty(Record record, Map<String, Object> sourceValues) throws TransformException;
}
