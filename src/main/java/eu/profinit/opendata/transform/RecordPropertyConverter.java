package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;

import java.util.HashMap;

/**
 * Created by dm on 12/2/15.
 */
public interface RecordPropertyConverter {
    void updateRecordProperty(Record record, HashMap<String, Object> sourceValues) throws TransformException;
}
