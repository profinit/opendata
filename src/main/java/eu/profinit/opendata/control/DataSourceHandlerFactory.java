package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataSourceHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by dm on 11/24/15.
 */
@Component
public class DataSourceHandlerFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public <T extends DataSourceHandler> T getHandlerFromClass(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
