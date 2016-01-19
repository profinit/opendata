package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.query.PartnerQueryService;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by dm on 12/16/15.
 */
@Component
public class PartnerSetter implements RecordPropertyConverter {

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String ico = null;
        String dic = null;
        String name = null;

        if(sourceValues.containsKey("ico")) {
            ico = sourceValues.get("ico").getStringCellValue();
        }
        if(sourceValues.containsKey("dic")) {
            dic = sourceValues.get("dic").getStringCellValue();
        }
        if(sourceValues.containsKey("name")) {
            name = sourceValues.get("name").getStringCellValue();
        }

        //Sanity check
        if(isNullOrEmpty(ico) && isNullOrEmpty(dic) && isNullOrEmpty(name)) {
            throw new TransformException("Could not set partner because ico, dic and name are all null or blank",
                    TransformException.Severity.RECORD_LOCAL);
        }

        Entity partner = partnerQueryService.findOrCreateEntity(name, ico, dic);

        Field field = null;
        try {
            field = Record.class.getDeclaredField(fieldName);
            Class<?> fieldType = field.getType();
            if (!fieldType.getName().equals(Entity.class.getName())) {
                throw new TransformException("Field " + fieldName + " doesn't have type Entity", TransformException.Severity.FATAL);
            }
            field.setAccessible(true);
            field.set(record, partner);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new TransformException("Field " + fieldName + " probably doesn't exist", e, TransformException.Severity.FATAL);
        } catch (TransformException e) {
            throw e;
        }


    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }


}
