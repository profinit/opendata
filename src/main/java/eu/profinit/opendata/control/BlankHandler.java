package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataSource;
import org.springframework.stereotype.Component;

/**
 * A handler that doesn't add any behavior to the GenericDataSourceHandler. To be used where data instances can't ever
 * be generated automatically.
 */
@Component
public class BlankHandler extends GenericDataSourceHandler {
    @Override
    protected void updateDataInstances(DataSource ds) {

    }
}
