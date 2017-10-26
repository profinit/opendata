package eu.profinit.opendata.control.impl;

import eu.profinit.opendata.control.DataSourceHandlerFactory;
import eu.profinit.opendata.model.DataSourceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by dm on 11/24/15.
 */
@Component
public class DataSourceHandlerFactoryImpl implements ApplicationContextAware, DataSourceHandlerFactory {

    private ApplicationContext applicationContext;
    private Logger log = LogManager.getLogger(DataSourceHandlerFactoryImpl.class);

    @Override
    public <T extends DataSourceHandler> T getHandlerFromClass(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        log.trace("Application context set into DataSourceHandlerFactory");
        this.applicationContext = applicationContext;
    }
}
