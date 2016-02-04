package eu.profinit.opendata.control.impl;

import eu.profinit.opendata.control.ExtractionService;
import eu.profinit.opendata.control.DataSourceHandlerFactory;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by dm on 11/23/15.
 */
@Service
public class ExtractionServiceImpl implements ExtractionService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DataSourceHandlerFactory dataSourceHandlerFactory;

    private Logger log = LogManager.getLogger(ExtractionServiceImpl.class);

    @Override
    @Transactional
    public void runExtraction() {
        log.info("Extraction process began");

        //Get all active data sources and invoke their handling class
        List<DataSource> activeDataSources = getActiveDatasources();
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

        log.info("All active data sources have been processed");
    }

    private List<DataSource> getActiveDatasources() {
        return em.createNamedQuery("findActiveDataSources", DataSource.class).getResultList();
    }


    @Override
    public DataSourceHandlerFactory getDataSourceHandlerFactory() {
        return dataSourceHandlerFactory;
    }

    @Override
    public void setDataSourceHandlerFactory(DataSourceHandlerFactory dataSourceHandlerFactory) {
        this.dataSourceHandlerFactory = dataSourceHandlerFactory;
    }

    @Override
    public EntityManager getEm() {
        return em;
    }

    @Override
    public void setEm(EntityManager em) {
        this.em = em;
    }
}
