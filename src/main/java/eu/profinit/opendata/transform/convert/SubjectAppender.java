package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Accepts two sourceValues: mainSubject and lineSubject. If the Record's subject property is empty, sets the mainSubject
 * and appends the lineSubject. If it isn't, only appends the lineSubject.
 * The fieldName attribute is ignored.
 */
@Component
public class SubjectAppender implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        try {

            String currentSubject = record.getSubject();
            String mainSubject = sourceValues.get("mainSubject").getStringCellValue();
            String lineSubject = "";

            if(sourceValues.get("lineSubject") != null) {
                lineSubject = sourceValues.get("lineSubject").getStringCellValue();
            }

            if(currentSubject != null && lineSubject.length() > 0) {
                record.setSubject(currentSubject + "; " + lineSubject);
            }
            else if (currentSubject == null) {
                record.setSubject(mainSubject + ": " + lineSubject);
            }

        } catch (Exception e) {
            String message = "Couldn't set String value for field " + fieldName;
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
