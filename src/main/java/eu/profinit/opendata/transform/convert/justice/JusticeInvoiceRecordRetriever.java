package eu.profinit.opendata.transform.convert.justice;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.PropertyBasedRecordRetriever;
import eu.profinit.opendata.transform.convert.SplitIdentifierSetter;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dm on 2/19/16.
 */
@Component
public class JusticeInvoiceRecordRetriever implements RecordRetriever {

    @Autowired
    private PropertyBasedRecordRetriever propertyBasedRecordRetriever;

    @Autowired
    private SplitIdentifierSetter splitIdentifierSetter;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger) throws TransformException {

        String identifier = splitIdentifierSetter.getIdentifierFromSourceValues(sourceValues);
        Map<String, String> stringFilters = new HashMap<>();
        stringFilters.put("authorityIdentifier", identifier);
        return propertyBasedRecordRetriever.retrieveRecordByStrings(currentRetrieval, stringFilters, RecordType.INVOICE);
    }
}
