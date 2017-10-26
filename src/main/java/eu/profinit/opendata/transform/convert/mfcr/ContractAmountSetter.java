package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets a MFCR contract originalCurrencyAmount. The fieldName argument is ignored. If the "contractAmount" source value
 * is present, the contract amount is set to that value. Otherwise, the current contract amount is incremented by the
 * "invoiceAmount" source value.
 */
@Component
public class ContractAmountSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        if(record.getRecordId() != null) return; // We don't want to change amounts for records that aren't new

        if(record.getOriginalCurrencyAmount() == null) {
            record.setOriginalCurrencyAmount(0.);
        }

        // Expecting source values "contractAmount" and "invoiceAmount"
        Cell invoiceAmountCell = sourceValues.get("invoiceAmount");
        Cell contractAmountCell = sourceValues.get("contractAmount");

        if(invoiceAmountCell.getCellType() == Cell.CELL_TYPE_NUMERIC &&
                invoiceAmountCell.getNumericCellValue() > 0) {
            double invoiceAmount = invoiceAmountCell.getNumericCellValue();
            record.setOriginalCurrencyAmount(record.getOriginalCurrencyAmount() + invoiceAmount);
            return;
        }

        if(contractAmountCell.getCellType() == Cell.CELL_TYPE_NUMERIC &&
                contractAmountCell.getNumericCellValue() > 0) {
            record.setOriginalCurrencyAmount(contractAmountCell.getNumericCellValue());
        }
    }
}
