package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets a double field specified by fieldName to an amount specified by the numeric cell with argumentName "inputAmount".
 * Expect the "originalCurrencyAmount" property to be already set. Does nothing if both values are equal and currency
 * is not CZK (no conversion).
 */
@Component
public class CZKAmountSetter extends MoneySetter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        try {

            Double inputAmount = sourceValues.get("inputAmount").getNumericCellValue();
            Double originalCurrencyAmount = record.getOriginalCurrencyAmount();

            // suspectAmount will hold the CZK amount that's already been set, or null
            Double amountCzk = record.getAmountCzk();
            if(originalCurrencyAmount != null && !"CZK".equals(record.getCurrency())
                    && (originalCurrencyAmount.equals(inputAmount) || originalCurrencyAmount.equals(amountCzk))) {

                // The currency is not CZK, but original and CZK amounts are the same, which means there has been
                // no conversion. Skip the property.
                logger.trace("Skipping this CZK amount because it seems there is no currency conversion in the source file");
                return;
            }

        } catch (Exception e) {
            String message = "Couldn't set Double value for field " + fieldName;
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }

        // If we got here, we're fine to go ahead and set the property
        super.updateRecordProperty(record, sourceValues, fieldName, logger);
    }
}
