package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataSourceHandler;

/**
 * Created by dm on 12/19/15.
 */
public interface DataSourceHandlerFactory {
    <T extends DataSourceHandler> T getHandlerFromClass(Class<T> clazz);
}
