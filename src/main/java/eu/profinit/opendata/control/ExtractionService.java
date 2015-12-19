package eu.profinit.opendata.control;

import eu.profinit.opendata.common.PersistenceAware;

/**
 * Created by dm on 12/19/15.
 */
public interface ExtractionService extends PersistenceAware {
    void runExtraction();

    DataSourceHandlerFactory getDataSourceHandlerFactory();

    void setDataSourceHandlerFactory(DataSourceHandlerFactory DataSourceHandlerFactory);
}
