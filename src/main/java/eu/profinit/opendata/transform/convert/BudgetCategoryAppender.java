package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the budgetCategory property. Expects an input string cell with argumentName "category" which can be null.
 * Ignores the fieldName argument.
 */
@Component
public class BudgetCategoryAppender implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        try {

            String currentCategory = record.getBudgetCategory();

            if(sourceValues.get("category") == null) return;

            String newCategory = sourceValues.get("category").getStringCellValue();
            if(Util.isNullOrEmpty(newCategory)) return;

            if(currentCategory != null && newCategory.length() > 0) {
                record.setBudgetCategory(currentCategory + "; " + newCategory);
            }
            else if (currentCategory == null) {
                record.setBudgetCategory(newCategory);
            }


        } catch (Exception e) {
            String message = "Couldn't set String value for field " + fieldName;
            logger.error(e);
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }

    }
}
