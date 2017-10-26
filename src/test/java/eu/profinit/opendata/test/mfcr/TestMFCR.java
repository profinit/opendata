package eu.profinit.opendata.test.mfcr;

import eu.profinit.opendata.institution.mfcr.*;
import eu.profinit.opendata.institution.rest.JSONClient;
import eu.profinit.opendata.institution.rest.JSONPackageList;
import eu.profinit.opendata.institution.rest.JSONPackageListResource;
import eu.profinit.opendata.institution.rest.JSONPackageListResult;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.test.ApplicationContextTestCase;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;


import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * Created by dm on 11/28/15.
 */
public class TestMFCR extends ApplicationContextTestCase {

    @Value("${mfcr.json.api.url}")
    private String json_api_url;

    @Value("${mfcr.json.packages.url}")
    private String packages_path;

    @Test
    public void testJSONClient() throws Exception {

        JSONClient client = (JSONClient) applicationContext.getBean(JSONClient.class);
        JSONPackageList packageList = client.getPackageList(json_api_url, packages_path,
                "seznam-objednavek-minsterstva-financi");

        assertNotNull(packageList);
        assertNotNull(packageList.getResult());
        JSONPackageListResult result = packageList.getResult();
        Assertions.assertThat(result.getResources().size()).isGreaterThanOrEqualTo(2);

        JSONPackageListResource resource = result.getResources().get(0);
        assertNotNull(resource.getUrl());
        Assertions.assertThat(resource.getFormat()).isIn(Arrays.asList("xls", "csv"));
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
