package eu.profinit.opendata.control;

import eu.profinit.opendata.common.PersistenceAware;

/**
 * A component for running the top-level logic of the application. The ExtractionService is responsible for retrieving
 * DataSources, instantiating and invoking handlers, and executing transforms on all eligible DataInstances.
 * Should only be instantiated by the Spring ApplicationContext.
 */
public interface ExtractionService extends PersistenceAware {
    /**
     * Initiates the extraction process for all active DataSources, provided that it is run from within a Spring
     * ApplicationContext. DataSources are retrieved and each DataSource's handler is invoked. Then the relationship
     * resolving service is run on the whole database.
     */
    void runExtraction();

    /**
     * For mocking only
     * @return
     */
    DataSourceHandlerFactory getDataSourceHandlerFactory();

    /**
     * For mocking only
     * @param DataSourceHandlerFactory
     */
    void setDataSourceHandlerFactory(DataSourceHandlerFactory dataSourceHandlerFactory);
}
