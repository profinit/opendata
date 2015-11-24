package eu.profinit.opendata.test;

import eu.profinit.opendata.model.*;
import junit.framework.TestCase;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import static eu.profinit.opendata.test.DataGenerator.*;

/**
 * Created by DM on 9. 11. 2015.
 */
public class PersistenceTest extends TestCase {
    
    private EntityManager em;

    public PersistenceTest() {
        try {
            em = Persistence.createEntityManagerFactory("postgres")
                    .createEntityManager();
        }
        catch(Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Override
    public void setUp() throws Exception {
        assertTrue(em.isOpen());

        em.getTransaction().begin();
        deleteAll("Entity");
        deleteAll("DataSource");
        deleteAll("DataInstance");
        deleteAll("Record");
        deleteAll("Retrieval");
        em.getTransaction().commit();

    }

    public void testSaveEntity() throws Exception {
        em.getTransaction().begin();
        Entity entity = getTestMinistry();
        em.persist(entity);
        em.getTransaction().commit();
        assertNotNull(entity.getEntityId());
        Long id = entity.getEntityId();

        Entity retrieved = em.find(Entity.class, id);
        assertNotNull(retrieved);
        assertEquals(EntityType.MINISTRY, retrieved.getEntityType());

        em.getTransaction().begin();
        em.remove(entity);
        assertNull(em.find(Entity.class, id));
        em.getTransaction().commit();
    }

    public void testRelationships() {
        Entity ministry = getTestMinistry();
        Entity company = getTestCompany();
        DataSource ds = getDataSource(ministry);
        DataInstance di = getDataInstance(ds);
        Retrieval ret = getRetrieval(di);
        Record contract = getContract(ret, ministry, company);
        Record inv = getInvoice(ret, ministry, company);

        inv.setParentRecord(contract);

        em.getTransaction().begin();
        em.persist(ministry);
        em.persist(company);
        em.persist(ds);
        em.persist(di);
        em.persist(ret);
        em.persist(contract);
        em.persist(inv);
        em.getTransaction().commit();

        assertNotNull(ministry.getEntityId());
        assertNotNull(contract.getRecordId());
        assertNotNull(di.getDataInstanceId());

        em.clear();

        //Entity to Data Source
        ministry = em.find(Entity.class, ministry.getEntityId());
        ds = em.find(DataSource.class, ds.getDataSourceId());
        assertEquals(ds.getEntity().getEntityId(), ministry.getEntityId());
        assertNotNull(ministry);
        assertNotNull(ministry.getDataSources());
        assertTrue(ministry.getDataSources().contains(ds));

        //Data Source to Data Instance
        ds = em.find(DataSource.class, ds.getDataSourceId());
        assertTrue(ds.getDataInstances().contains(di));

        //Data Instance to Retrieval
        di = em.find(DataInstance.class, di.getDataInstanceId());
        assertTrue(di.getRetrievals().contains(ret));

        //Retrieval to Record
        ret = em.find(Retrieval.class, ret.getRetrievalId());
        contract = em.find(Record.class, contract.getRecordId());
        inv = em.find(Record.class, inv.getRecordId());
        assertTrue(ret.getRecords().contains(contract));
        assertTrue(ret.getRecords().contains(inv));

        //Record to Authority
        assertTrue(ministry.getRecordsAsAuthority().contains(contract));
        assertTrue(ministry.getRecordsAsAuthority().contains(inv));
        assertTrue(ministry.getRecordsAsPartner().isEmpty());

        //Record to Partner
        company = em.find(Entity.class, company.getEntityId());
        assertTrue(company.getRecordsAsPartner().contains(contract));
        assertTrue(company.getRecordsAsPartner().contains(inv));
        assertTrue(company.getRecordsAsAuthority().isEmpty());

        //Record to Record
        contract = em.find(Record.class, contract.getRecordId());
        assertTrue(contract.getChildRecords().contains(inv));

        em.getTransaction().begin();
        em.remove(ministry);
        em.remove(company);
        em.getTransaction().commit();

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
