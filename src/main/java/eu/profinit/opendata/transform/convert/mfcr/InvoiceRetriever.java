package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.PropertyBasedRecordRetriever;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dm on 2/10/16.
 */
@Component
public class InvoiceRetriever implements RecordRetriever {

    @Autowired
    private PropertyBasedRecordRetriever propertyBasedRecordRetriever;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger)
            throws TransformException {

        String type = sourceValues.get("inputType").getStringCellValue();
        RecordType recordType = type.equals("Přijaté faktury") ? RecordType.INVOICE : RecordType.PAYMENT;

        HashMap<String, String> filters = new HashMap<>();
        sourceValues.get("authorityIdentifier").setCellType(Cell.CELL_TYPE_STRING);
        filters.put("authorityIdentifier", sourceValues.get("authorityIdentifier").getStringCellValue());

        return propertyBasedRecordRetriever.retrieveRecordByStrings(currentRetrieval, filters, recordType);
    }
}
