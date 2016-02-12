package eu.profinit.opendata.institution.mfcr;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dm on 12/19/15.
 */
public interface MFCRHandler extends DataSourceHandler {
    //Test
    void setEm(EntityManager em);
    void updateDataInstances(DataSource ds);
    void setPartnerListProcessor(PartnerListProcessor plp);
}
