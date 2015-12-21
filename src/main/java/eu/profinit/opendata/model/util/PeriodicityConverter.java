package eu.profinit.opendata.model.util;

import eu.profinit.opendata.model.Periodicity;

import javax.persistence.AttributeConverter;

/**
 * Created by DM on 13. 11. 2015.
 */
public class PeriodicityConverter implements AttributeConverter<Periodicity, String> {
    @Override
    public String convertToDatabaseColumn(Periodicity periodicity) {
        if(periodicity != null) return periodicity.name().toLowerCase();
        return "aperiodic";
    }

    @Override
    public Periodicity convertToEntityAttribute(String s) {
        return Periodicity.valueOf(s.toUpperCase());
    }
}
