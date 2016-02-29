package eu.profinit.opendata.transform;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * A component capable of instantiating TransformComponents based on their class name. Uses the Spring
 * ApplicationContext.
 */
@Component
public class ComponentFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * Retrieves a bean with the specified class name from the ApplicationContext.
     * @param className The fully qualified class name of the requested component.
     * @return The requested TransformComponent.
     * @throws ClassNotFoundException
     */
    public TransformComponent getComponent(String className) throws ClassNotFoundException {
        return (TransformComponent) applicationContext.getBean(Class.forName(className));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
