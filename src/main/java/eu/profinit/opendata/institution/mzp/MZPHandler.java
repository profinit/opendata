package eu.profinit.opendata.institution.mzp;

import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.model.DataSource;
import org.springframework.stereotype.Component;

/**
 * The handler for MZP. All data instances are added manually so there's no special behavior.
 */
@Component
public class MZPHandler extends GenericDataSourceHandler {
    @Override
    protected void updateDataInstances(DataSource ds) {
        // Nothing to do, data instances are added manually
    }
}
