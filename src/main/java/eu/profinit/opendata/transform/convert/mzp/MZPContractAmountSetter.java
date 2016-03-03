package eu.profinit.opendata.transform.convert.mzp;

import eu.profinit.opendata.model.Periodicity;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dm on 2/21/16.
 */
@Component
public class MZPContractAmountSetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        Cell amountCell = sourceValues.get("inputAmount");

        if(amountCell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            String contents = amountCell.getStringCellValue().toLowerCase();
            Pattern pattern = Pattern.compile("^(?<periodicity>\\D*)(?<amount>(\\d|\\s)+)kč");
            Matcher matcher = pattern.matcher(contents);

            if(matcher.find()) {
                String periodicity = matcher.group("periodicity").trim();
                if(periodicity.equals("měsíčně")) {
                    record.setPeriodicity(Periodicity.MONTHLY);
                }
                else if(periodicity.equals("roční limit")) {
                    record.setPeriodicity(Periodicity.YEARLY);
                }

                String amountString = matcher.group("amount").replace(" ", "");
                Double amount = Double.parseDouble(amountString);
                record.setOriginalCurrencyAmount(amount);
                record.setAmountCzk(amount);
            }
            else {
                throw new TransformException("Couldn't set amounts, the cell is non-numeric but doesn't match the string pattern",
                        TransformException.Severity.PROPERTY_LOCAL);
            }
        }
        else {
            Double amount = amountCell.getNumericCellValue();
            record.setOriginalCurrencyAmount(amount);
            record.setAmountCzk(amount);
        }
    }
}
