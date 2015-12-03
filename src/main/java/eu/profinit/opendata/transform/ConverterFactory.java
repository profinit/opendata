package eu.profinit.opendata.transform;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by dm on 12/3/15.
 */
@Component
public class ConverterFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public RecordPropertyConverter getConverter(String className) throws ClassNotFoundException {
        return (RecordPropertyConverter) applicationContext.getBean(Class.forName(className));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
