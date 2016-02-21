package eu.profinit.opendata.model;

/**
 * A component capable of processing a DataSource.
 * @see DataSource
 */
public interface DataSourceHandler {
    void processDataSource(DataSource ds);
}
