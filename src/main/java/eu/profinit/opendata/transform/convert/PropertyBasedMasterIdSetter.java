package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.query.RecordQueryService;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dm on 1/31/16.
 */
public class PropertyBasedMasterIdSetter implements RecordPropertyConverter {

    @Autowired
    private MasterIdSetter masterIdSetter;

    @Autowired
    private RecordQueryService recordQueryService;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger) throws TransformException {
        HashMap<String, String> filters = new HashMap<>();

        for(String key : sourceValues.keySet()) {
            filters.put(key, sourceValues.get(key).getStringCellValue());
        }

        List<Record> found = recordQueryService.findRecordsByFilter(filters);

        if(!found.isEmpty()) {
            Record first = found.get(0);
            record.setMasterId(first.getMasterId());
        }
        else {
            masterIdSetter.updateRecordProperty(record, sourceValues, fieldName, logger);
        }
    }
}
