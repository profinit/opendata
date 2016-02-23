package eu.profinit.opendata.test.mk;

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
import java.util.List;

/**
 * Created by dm on 2/23/16.
 */
public class MKTest extends ApplicationContextTestCase {

    @Autowired
    private DatabaseUtils databaseUtils;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TransformDriver transformDriver;

    @Test
    @Transactional
    public void testContractsMapping() throws Exception {
        databaseUtils.cleanRecords();

        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xlsx");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("mk/test-contracts.xlsx").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        em.persist(entity);
        DataSource ds = DataGenerator.getDataSource(entity);
        ds.setRecordType(RecordType.CONTRACT);
        em.persist(ds);
        dataInstance.setDataSource(ds);
        em.persist(dataInstance);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "mappings/mk/mapping-contracts.xml", inputStream);
        em.persist(retrieval);

        List<Record> recordList = em.createQuery(
                "SELECT r FROM Record r WHERE r.retrieval = :retr", Record.class)
                .setParameter("retr", retrieval)
                .getResultList();
        assertEquals(40, recordList.size());

    }
}
