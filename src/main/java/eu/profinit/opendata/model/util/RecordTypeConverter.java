package eu.profinit.opendata.model.util;

import eu.profinit.opendata.model.RecordType;

import javax.persistence.AttributeConverter;

/**
 * Created by DM on 13. 11. 2015.
 */
public class RecordTypeConverter implements AttributeConverter<RecordType, String> {
    @Override
    public String convertToDatabaseColumn(RecordType recordType) {
        return recordType.name().toLowerCase();
    }

    @Override
    public RecordType convertToEntityAttribute(String s) {
        return RecordType.valueOf(s.toUpperCase());
    }
}
