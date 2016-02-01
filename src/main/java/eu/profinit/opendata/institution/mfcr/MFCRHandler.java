package eu.profinit.opendata.institution.mfcr;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

import javax.persistence.EntityManager;

/**
 * Created by dm on 12/19/15.
 */
public interface MFCRHandler extends DataSourceHandler {
    //Test
    void setEm(EntityManager em);
    void updateInvoicesDataInstance(DataSource ds);
    void updateOrdersDataInstance(DataSource ds);
}
