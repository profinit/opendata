package eu.profinit.opendata.test.mfcr;

import eu.profinit.opendata.handler.mfcr.JSONClient;
import eu.profinit.opendata.handler.mfcr.JSONPackageList;
import eu.profinit.opendata.handler.mfcr.JSONPackageListResource;
import eu.profinit.opendata.handler.mfcr.JSONPackageListResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.junit.Assert.*;

/**
 * Created by dm on 11/28/15.
 */
public class TestMFCR {

    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {

        applicationContext = new ClassPathXmlApplicationContext("beans.xml");
    }

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
}
