package eu.profinit.opendata.test.mfcr;

import eu.profinit.opendata.institution.mfcr.*;
import eu.profinit.opendata.institution.mfcr.rest.JSONClient;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageList;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageListResource;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageListResult;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.test.ApplicationContextTestCase;
import org.junit.Test;


import javax.persistence.EntityManager;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * Created by dm on 11/28/15.
 */
public class TestMFCR extends ApplicationContextTestCase {

    @Test
    public void testJSONClient() throws Exception {

        JSONClient client = (JSONClient) applicationContext.getBean(JSONClient.class);
        JSONPackageList packageList = client.getPackageList("seznam-objednavek-minsterstva-financi");

        assertNotNull(packageList);
        assertNotNull(packageList.getResult());
        JSONPackageListResult result = packageList.getResult();
        assertTrue(result.getResources().size() >= 2);

        JSONPackageListResource resource = result.getResources().get(0);
        assertNotNull(resource.getUrl());
        assertTrue(resource.getFormat().equals("xls") || resource.getFormat().equals("csv"));
    }

    @Test
    public void testCreateDataInstances() throws Exception {
        MFCRHandler handler = (MFCRHandler) applicationContext.getBean(MFCRHandler.class);
        EntityManager mockEm = mock(EntityManager.class);
        handler.setEm(mockEm);

        DataSource ds = new DataSource();
        ds.setDataInstances(new ArrayList<>());
        ds.setActive(true);
        ds.setPeriodicity(Periodicity.MONTHLY);

        DataInstance oldDataInstance = new DataInstance();
        oldDataInstance.setDataSource(ds);
        oldDataInstance.setPeriodicity(Periodicity.MONTHLY);
        oldDataInstance.setUrl("Not really a URL");

        ds.getDataInstances().add(oldDataInstance);
        ds.setRecordType(RecordType.ORDER);

        handler.updateDataInstances(ds);

        assertNotNull(oldDataInstance.getExpires());
        assertEquals(2, ds.getDataInstances().size());

    }



}
