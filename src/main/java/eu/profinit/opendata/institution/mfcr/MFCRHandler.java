package eu.profinit.opendata.institution.mfcr;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

import javax.persistence.EntityManager;

/**
 * Interface for the MF handler. See implementation class for behavior details.
 * @see eu.profinit.opendata.institution.mfcr.impl.MFCRHandlerImpl
 */
public interface MFCRHandler extends DataSourceHandler {
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

    /**
     * Used for mocking
     * @param plp
     */
    void setPartnerListProcessor(PartnerListProcessor plp);
}
