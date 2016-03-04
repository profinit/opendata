package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the type of a record extracted from a MFCR invoice document to either INVOICE or PAYMENT. Expects a source cell
 * with argumentName = "inputType" and value either "Přijaté faktury" or "Ostatní platby". The fieldName argument is
 * ignored.
 */
@Component
public class InvoiceVsPaymentSetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String type = sourceValues.get("inputType").getStringCellValue();
        if(type.equals("Přijaté faktury")) {
            record.setRecordType(RecordType.INVOICE);
        }
        else if(type.equals("Ostatní platby")) {
            record.setRecordType(RecordType.PAYMENT);
        }
        else {
            throw new TransformException("Unknown type: " + type, TransformException.Severity.RECORD_LOCAL);
        }
    }
}
