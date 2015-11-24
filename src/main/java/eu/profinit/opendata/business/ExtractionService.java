package eu.profinit.opendata.business;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

/**
 * Created by dm on 11/23/15.
 */
public class ExtractionService {

    private EntityManager em;
    private DataSourceHandlerFactory dataSourceHandlerFactory;

    public DataSourceHandlerFactory getDataSourceHandlerFactory() {
        return dataSourceHandlerFactory;
    }

    public void setDataSourceHandlerFactory(DataSourceHandlerFactory dataSourceHandlerFactory) {
        this.dataSourceHandlerFactory = dataSourceHandlerFactory;
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }


    public void runExtraction() {
        //Get all active data sources and invoke their handling class
        List<DataSource> activeDataSources = em.createNamedQuery("findActiveDataSources", DataSource.class).getResultList();

        for(DataSource ds : activeDataSources) {
            Class<? extends DataSourceHandler> handlingClass = ds.getHandlingClass();
            try {
                DataSourceHandler handler = dataSourceHandlerFactory.getHandlerFromClass(handlingClass);
                handler.processDataSource(ds);
            } catch (InstantiationException | IllegalAccessException e) {
                Logger.getLogger(ExtractionService.class).error("Could not process data source " + ds.getDataSourceId(), e);
            }
        }
    }
}
