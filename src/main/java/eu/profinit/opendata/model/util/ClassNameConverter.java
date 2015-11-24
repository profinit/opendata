package eu.profinit.opendata.model.util;

import eu.profinit.opendata.model.DataSourceHandler;
import org.apache.log4j.Logger;

import javax.persistence.AttributeConverter;

/**
 * Created by DM on 19. 11. 2015.
 */
public class ClassNameConverter implements AttributeConverter<Class<? extends DataSourceHandler>, String> {
    @Override
    public String convertToDatabaseColumn(Class<? extends DataSourceHandler> aClass) {
        if(aClass != null) {
            return aClass.getCanonicalName();
        } else return "";
    }

    @Override
    public Class<? extends DataSourceHandler> convertToEntityAttribute(String s) {
        if(s.length() > 0) {
            try {
                return (Class<? extends DataSourceHandler>) Class.forName(s);
            } catch (ClassNotFoundException | ClassCastException e) {
                Logger log = Logger.getLogger(ClassNameConverter.class);
                log.error("Error converting string " + s + " to class", e);
                return null;
            }
        }
        else return null;
    }
}
