package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataSourceHandler;

/**
 * A component that can instantiate <code>DataSourceHandler</code> objects.
 */
public interface DataSourceHandlerFactory {
    /**
     * Retrieves a DataSourceHandler of the specified type. It may or may not be newly instantiated.
     * @param clazz The desired return type
     * @param <T> The desired return type
     * @return A DataSourceHandler
     */
    <T extends DataSourceHandler> T getHandlerFromClass(Class<T> clazz);
}
