package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by dm on 11/23/15.
 */
@Component
public class ExtractionService {

    @PersistenceContext(unitName = "postgres")
    private EntityManager em;

    @Autowired
    private DataSourceHandlerFactory dataSourceHandlerFactory;

    private Logger log = LogManager.getLogger(ExtractionService.class);

    public void runExtraction() {
        log.info("Extraction process began");

        //Get all active data sources and invoke their handling class
        List<DataSource> activeDataSources = em.createNamedQuery("findActiveDataSources", DataSource.class).getResultList();
        if(activeDataSources.isEmpty()) {
            log.info("There are no active data sources. Exiting.");
        }

        for(DataSource ds : activeDataSources) {
            Class<? extends DataSourceHandler> handlingClass = ds.getHandlingClass();
            log.debug("Instantiating handling class " + handlingClass.getName() + " for data source " + ds.getDataSourceId());
            DataSourceHandler handler = dataSourceHandlerFactory.getHandlerFromClass(handlingClass);

            try {
                handler.processDataSource(ds);
            } catch (Exception e) {
                log.error("Processing of data source failed due to an exception", e);
            }
        }
    }


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
}
