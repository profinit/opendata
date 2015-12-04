package eu.profinit.opendata.transform;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by dm on 12/3/15.
 */
@Component
public class ComponentFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public TransformComponent getComponent(String className) throws ClassNotFoundException {
        return (TransformComponent) applicationContext.getBean(Class.forName(className));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
