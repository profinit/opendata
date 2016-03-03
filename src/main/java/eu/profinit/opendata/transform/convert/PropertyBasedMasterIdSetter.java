package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.query.RecordQueryService;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sets the masterId property of a Record. Tries to find an existing Record with specified attribute values (contained
 * in the sourceValues map). If one is found, sets the masterId of the new Record to the same masterId as the old Record.
 * If none is found, invokes the RandomMasterIdSetter to set a newly generated masterId. The fieldName attribute is
 * ignored.
 * @see RecordQueryService#findRecordsByFilter(Map, Retrieval)
 */
@Component
public class PropertyBasedMasterIdSetter implements RecordPropertyConverter {

    @Autowired
    private RandomMasterIdSetter randomMasterIdSetter;

    @Autowired
    private RecordQueryService recordQueryService;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger) throws TransformException {
        HashMap<String, String> filters = new HashMap<>();

        for(String key : sourceValues.keySet()) {
            sourceValues.get(key).setCellType(Cell.CELL_TYPE_STRING);
            filters.put(key, sourceValues.get(key).getStringCellValue());
        }

        List<Record> found = recordQueryService.findRecordsByFilter(filters, record.getRetrieval());

        if(!found.isEmpty()) {
            Record first = found.get(0);
            record.setMasterId(first.getMasterId());
        }
        else {
            randomMasterIdSetter.updateRecordProperty(record, sourceValues, fieldName, logger);
        }
    }
}
