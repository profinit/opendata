package eu.profinit.opendata.institution.mocr;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for the MF handler. See implementation class for behavior details.
 * @see eu.profinit.opendata.institution.mocr.MFCRHandlerImpl
 */
public interface MOCRHandler extends DataSourceHandler {
    /**
     * Used for mocking
     * @param em
     */
    void setEm(EntityManager em);

    /**
     * Internal method, exposed for testing purposes. Not to be called directly.
     * @param ds
     */
    void updateDataInstances(DataSource ds);

}
