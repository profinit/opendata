package eu.profinit.opendata.model.util;

import eu.profinit.opendata.model.EntityType;
import javax.persistence.AttributeConverter;

public class EntityTypeConverter implements AttributeConverter<EntityType, String> {

    @Override
    public String convertToDatabaseColumn(EntityType entityType) {
        return entityType.name().toLowerCase();
    }

    @Override
    public EntityType convertToEntityAttribute(String s) {
        return EntityType.valueOf(s.toUpperCase());
    }
}