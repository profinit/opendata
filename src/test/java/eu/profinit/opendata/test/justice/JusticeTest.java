package eu.profinit.opendata.test.justice;

import eu.profinit.opendata.institution.justice.JusticeHandler;
import eu.profinit.opendata.model.*;
import eu.profinit.opendata.test.ApplicationContextTestCase;
import eu.profinit.opendata.test.DataGenerator;
import eu.profinit.opendata.test.util.DatabaseUtils;
import eu.profinit.opendata.transform.TransformDriver;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

/**
 * Created by dm on 2/18/16.
 */
public class JusticeTest extends ApplicationContextTestCase {

    @Autowired
    private DatabaseUtils databaseUtils;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TransformDriver transformDriver;

    @Autowired
    private JusticeHandler justiceHandler;

    @Test
    @Transactional
    public void testProcessContracts() throws Exception {
        databaseUtils.cleanRecords();

        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xlsx");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("justice/test-contracts.xlsx").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        em.persist(entity);
        DataSource ds = DataGenerator.getDataSource(entity);
        ds.setRecordType(RecordType.CONTRACT);
        em.persist(ds);
        dataInstance.setDataSource(ds);
        em.persist(dataInstance);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "mappings/justice/mapping-contracts.xml", inputStream);
        em.persist(retrieval);

        List<Record> recordList = em.createQuery(
                "SELECT r FROM Record r WHERE r.retrieval = :retr", Record.class)
                .setParameter("retr", retrieval)
                .getResultList();
        assertEquals(14, recordList.size());

    }

    @Test
    public void testGetInvoiceDataInstances() throws Exception {

        DataSource ds = new DataSource();
        ds.setDataInstances(new ArrayList<>());
        ds.setRecordType(RecordType.INVOICE);

        EntityManager mockEm = mock(EntityManager.class);
        justiceHandler.setEm(mockEm);

        justiceHandler.updateDataInstances(ds);
        Collection<DataInstance> dataInstanceList = ds.getDataInstances();
        assertTrue(6 < dataInstanceList.size());

    }
}
