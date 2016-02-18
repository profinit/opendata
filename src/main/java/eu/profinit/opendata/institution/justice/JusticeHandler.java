package eu.profinit.opendata.institution.justice;

import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.model.DataSource;

/**
 * Created by dm on 2/17/16.
 */
public class JusticeHandler extends GenericDataSourceHandler{
    @Override
    protected void updateDataInstances(DataSource ds) {
        // Data instances are created manually for contracts, nothing to do.
    }
}
