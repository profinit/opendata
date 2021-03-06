package eu.profinit.opendata.test;

import eu.profinit.opendata.model.*;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import static eu.profinit.opendata.test.DataGenerator.*;

/**
 * Created by DM on 9. 11. 2015.
 */
public class PersistenceTest extends ApplicationContextTestCase {

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void testSaveEntity() throws Exception {
        Entity entity = getTestMinistry();
        em.persist(entity);
        assertNotNull(entity.getEntityId());
        Long id = entity.getEntityId();

        Entity retrieved = em.find(Entity.class, id);
        assertNotNull(retrieved);
        assertEquals(EntityType.MINISTRY, retrieved.getEntityType());

        em.remove(entity);
        assertNull(em.find(Entity.class, id));
    }

    @Test
    @Transactional
    public void testRelationships() {
        Entity ministry = getTestMinistry();
        Entity company = getTestCompany();
        DataSource ds = getDataSource(ministry);
        DataInstance di = getDataInstance(ds);
        Retrieval ret = getRetrieval(di);
        Record contract = getContract(ret, ministry, company);
        Record inv = getInvoice(ret, ministry, company);

        inv.setParentRecord(contract);

        em.persist(ministry);
        em.persist(company);
        em.persist(ds);
        em.persist(di);
        em.persist(ret);
        em.persist(contract);
        em.persist(inv);

        assertNotNull(ministry.getEntityId());
        assertNotNull(contract.getRecordId());
        assertNotNull(di.getDataInstanceId());

        em.flush();
        em.clear();

        //Entity to Data Source
        ministry = em.find(Entity.class, ministry.getEntityId());
        ds = em.find(DataSource.class, ds.getDataSourceId());
        assertEquals(ds.getEntity().getEntityId(), ministry.getEntityId());
        assertNotNull(ministry);
        assertNotNull(ministry.getDataSources());
        Assertions.assertThat(ministry.getDataSources()).contains(ds);

        //Data Source to Data Instance
        ds = em.find(DataSource.class, ds.getDataSourceId());
        Assertions.assertThat(ds.getDataInstances()).contains(di);

        //Data Instance to Retrieval
        di = em.find(DataInstance.class, di.getDataInstanceId());
        Assertions.assertThat(di.getRetrievals()).contains(ret);
        

        //Retrieval to Record
        ret = em.find(Retrieval.class, ret.getRetrievalId());
        contract = em.find(Record.class, contract.getRecordId());
        inv = em.find(Record.class, inv.getRecordId());
        Assertions.assertThat(ret.getRecords()).contains(contract);
        Assertions.assertThat(ret.getRecords()).contains(inv);

        //Record to Authority
        Assertions.assertThat(ministry.getRecordsAsAuthority()).contains(contract).contains(inv);
        Assertions.assertThat(ministry.getRecordsAsPartner()).isEmpty();

        //Record to Partner
        company = em.find(Entity.class, company.getEntityId());
        Assertions.assertThat(company.getRecordsAsPartner()).contains(contract).contains(inv);
        Assertions.assertThat(company.getRecordsAsAuthority()).isEmpty();

        //Record to Record
        contract = em.find(Record.class, contract.getRecordId());
        Assertions.assertThat(contract.getChildRecords()).contains(inv);

        em.remove(ministry);
        em.remove(company);

        inv = em.find(Record.class, inv.getRecordId());
        assertNull(inv);
    }

    @Override
    public void tearDown() throws Exception {

        em.close();
        em = null;
    }

    private int deleteAll(String table){
        String hql = String.format("delete from %s p", table);
        Query query = em.createQuery(hql);
        return query.executeUpdate();
    }
}
