package eu.profinit.opendata.transform.convert.mfcr;

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
 * Created by dm on 2/11/16.
 */
@Component
public class PartnerByCodeSetter implements RecordPropertyConverter {

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String code = sourceValues.get("code").getStringCellValue();

        Entity authority = record.getRetrieval().getDataInstance().getDataSource().getEntity();
        Entity partner = partnerQueryService.findFromPartnerList(authority, code.substring(0, code.length() - 3));

        record.setPartner(partner);
    }
}
