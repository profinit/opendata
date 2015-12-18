package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by dm on 12/16/15.
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
            Double amountCzkWithoutVat = record.getAmountCzkWithoutVat();
            Double amountCzkWithVat = record.getAmountCzkWithVat();
            Double suspectAmount = amountCzkWithVat == null ? amountCzkWithoutVat : amountCzkWithVat;

            if(originalCurrencyAmount != null && !"CZK".equals(record.getCurrency())
                    && (originalCurrencyAmount.equals(inputAmount) || originalCurrencyAmount.equals(suspectAmount))) {

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
