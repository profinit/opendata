package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.SourceRowFilter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by dm on 1/31/16.
 */
@Component
public class InvoiceFilter implements SourceRowFilter {
    @Override
    public boolean proceedWithRow(Retrieval currentRetrieval, Map<String, Cell> sourceValues) throws TransformException {
        String type = sourceValues.get("rowType").getStringCellValue();
        return type.trim().toLowerCase().equals("přijaté faktury");
    }
}
