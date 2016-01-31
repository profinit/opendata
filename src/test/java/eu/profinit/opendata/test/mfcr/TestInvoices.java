package eu.profinit.opendata.test.mfcr;

import eu.profinit.opendata.institution.mfcr.MFCRHandler;
import eu.profinit.opendata.model.*;
import eu.profinit.opendata.test.ApplicationContextTestCase;
import eu.profinit.opendata.test.DataGenerator;
import eu.profinit.opendata.test.util.DatabaseCleaner;
import eu.profinit.opendata.transform.TransformDriver;
import eu.profinit.opendata.transform.WorkbookProcessor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.util.*;

import static org.mockito.Mockito.mock;

/**
 * Created by dm on 1/30/16.
 */
public class TestInvoices extends ApplicationContextTestCase {

    @Autowired
    private MFCRHandler mfcrHandler;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private WorkbookProcessor workbookProcessor;

    @Autowired
    private TransformDriver transformDriver;

    @PersistenceContext
    private EntityManager em;


    @Test
    public void testCreateDataInstances() throws Exception {
        DataSource ds = new DataSource();
        ds.setDataInstances(new ArrayList<>());

        EntityManager mockEm = mock(EntityManager.class);
        mfcrHandler.setEm(mockEm);

        mfcrHandler.updateInvoicesDataInstance(ds);
        Collection<DataInstance> dataInstanceList = ds.getDataInstances();
        assertTrue(1 <= dataInstanceList.size());

    }

    @Test
    @Transactional
    public void testProcessInvoiceWorkbook() throws Exception {
        databaseCleaner.cleanRecords();

        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xlsx");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("test-invoices.xlsx").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        em.persist(entity);
        DataSource ds = DataGenerator.getDataSource(entity);
        em.persist(ds);
        dataInstance.setDataSource(ds);
        em.persist(dataInstance);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "mappings/mfcr/mapping-invoices.xml", inputStream);
        em.persist(retrieval);

        List<Record> recordList = em.createQuery(
                "SELECT r FROM Record r WHERE r.retrieval = :retr", Record.class)
                .setParameter("retr", retrieval)
                .getResultList();
        assertEquals(23, recordList.size());

    }
}
