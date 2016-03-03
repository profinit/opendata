package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Sets the masterId property of a Record to a randomly generated UUID. The fieldName attribute is ignored.
 */
@Component
public class RandomMasterIdSetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        record.setMasterId(UUID.randomUUID().toString());

    }
}
