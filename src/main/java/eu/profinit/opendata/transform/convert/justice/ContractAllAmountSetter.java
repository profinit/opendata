package eu.profinit.opendata.transform.convert.justice;

import eu.profinit.opendata.common.Util;
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
 * MSp contracts contain the currency and the amount in a single string cell. The format can change from cell to cell.
 * This converter accepts a MSp contract cell (argumentName = "inputAmount") and sets the Record's currency,
 * originalCurrencyAmount, amountCzk (if currency is CZK) and in some cases the periodicity as well. The fieldName
 * attribute is ignored.
 */
@Component
public class ContractAllAmountSetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        record.setCurrency("CZK");

        String amountString = sourceValues.get("inputAmount").getStringCellValue();

        if(amountString.contains("/1 den")) {
            record.setPeriodicity(Periodicity.DAILY);
        }

        amountString = amountString.replace("/1 den", "");
        amountString = amountString.replace(" ", "");
        amountString = amountString.replace(".", "");
        amountString = amountString.replace(',', '.');
        String regex = "(?<amount>-?[0-9\\.]+)(?<currency>\\w{2,3})?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(amountString);

        if(!matcher.find()) {
            logger.warn("Setting amounts failed");
            return;
        }

        String cleanAmount = matcher.group("amount");
        String currency = matcher.group("currency");

        if(!Util.isNullOrEmpty(currency) && !currency.equalsIgnoreCase("KČ")) {
            record.setCurrency(currency.toUpperCase());
        }

        Double totalAmount = Double.parseDouble(cleanAmount);
        record.setOriginalCurrencyAmount(totalAmount);
        if(record.getCurrency().equals("CZK")) {
            record.setAmountCzk(totalAmount);
        }
    }
}
