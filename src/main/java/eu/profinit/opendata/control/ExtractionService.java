package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by dm on 11/23/15.
 */
@Component
public class ExtractionService {

    private EntityManager em;

    @Autowired
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
