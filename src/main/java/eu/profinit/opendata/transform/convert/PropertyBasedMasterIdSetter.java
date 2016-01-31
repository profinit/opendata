package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
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
 * Created by dm on 1/31/16.
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
