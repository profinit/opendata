package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by dm on 2/10/16.
 */
@Component
public class BudgetCategoryAppender implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        try {

            String currentCategory = record.getBudgetCategory();
            String newCategory = sourceValues.get("category").getStringCellValue();

            if(currentCategory != null && newCategory.length() > 0) {
                record.setBudgetCategory(currentCategory + "; " + newCategory);
            }
            else if (currentCategory == null) {
                record.setBudgetCategory(newCategory);
            }


        } catch (Exception e) {
            String message = "Couldn't set String value for field " + fieldName;
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }

    }
}
