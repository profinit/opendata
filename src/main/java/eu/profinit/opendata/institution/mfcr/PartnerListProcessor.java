package eu.profinit.opendata.institution.mfcr;

import eu.profinit.opendata.institution.mfcr.rest.JSONPackageListResource;
import eu.profinit.opendata.model.DataSource;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dm on 2/12/16.
 */
public interface PartnerListProcessor {
    void processListOfPartners(DataSource ds, InputStream inputStream) throws IOException;

    //Test
    void setEm(EntityManager em);
}
