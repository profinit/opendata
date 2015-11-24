package eu.profinit.opendata.business;

import eu.profinit.opendata.model.DataSourceHandler;

/**
 * Created by dm on 11/24/15.
 */
public class DataSourceHandlerFactory {
    public <T extends DataSourceHandler> T getHandlerFromClass(Class<T> clazz)
            throws IllegalAccessException, InstantiationException {

        return clazz.newInstance();
    }
}
