package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.common.Util;
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

import static eu.profinit.opendata.common.Util.isNullOrEmpty;

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

        if(sourceValues.containsKey("ico") && sourceValues.get("ico") != null) {
            Cell icoCell = sourceValues.get("ico");
            icoCell.setCellType(Cell.CELL_TYPE_STRING);

            if(canBeValidICO(icoCell)) {
                ico = icoCell.getStringCellValue();
                if (ico.length() < 8) {
                    ico = String.format("%08d", Integer.parseInt(ico));
                }
            }
        }
        if(sourceValues.containsKey("dic") && sourceValues.get("dic") != null
                && !isNullOrEmpty(sourceValues.get("dic").getStringCellValue())) {
            dic = sourceValues.get("dic").getStringCellValue();
        }
        if(sourceValues.containsKey("name") && sourceValues.get("name") != null
                && !isNullOrEmpty(sourceValues.get("name").getStringCellValue())) {
            name = sourceValues.get("name").getStringCellValue();
        }

        //Sanity check
        if(isNullOrEmpty(ico) && isNullOrEmpty(dic) && isNullOrEmpty(name)) {
            throw new TransformException("Could not set partner because ico, dic and name are all null or blank",
                    TransformException.Severity.PROPERTY_LOCAL);
        }

        logger.trace("Calling PartnerQueryService to find or create entity");
        Entity partner = partnerQueryService.findOrCreateEntity(name, ico, dic);
        logger.trace("Got back entity " + partner.getName());

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
        } finally {
            logger.trace("PartnerSetter exiting");
        }
    }

    private boolean canBeValidICO(Cell cell) {
        return !Util.isNullOrEmpty(cell.getStringCellValue()) && cell.getStringCellValue().length() > 1;
    }


}
