package eu.profinit.opendata.test.converter;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by dm on 12/13/15.
 */
@Component
public class Killjoy implements RecordPropertyConverter {

    public static final String MANIFESTO = "I just want to watch the world burn";

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        if(sourceValues.get("orderNumber").getStringCellValue().equals("1403000370")) {
            throw new TransformException(MANIFESTO, TransformException.Severity.FATAL);
        }
    }
}
