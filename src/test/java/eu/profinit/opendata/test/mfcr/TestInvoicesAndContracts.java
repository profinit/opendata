package eu.profinit.opendata.test.mfcr;

import eu.profinit.opendata.control.RelationshipResolver;
import eu.profinit.opendata.institution.mfcr.MFCRHandler;
import eu.profinit.opendata.institution.mfcr.PartnerListProcessor;
import eu.profinit.opendata.model.*;
import eu.profinit.opendata.test.ApplicationContextTestCase;
import eu.profinit.opendata.test.DataGenerator;
import eu.profinit.opendata.test.util.DatabaseUtils;
import eu.profinit.opendata.transform.TransformDriver;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by dm on 1/30/16.
 */
public class TestInvoicesAndContracts extends ApplicationContextTestCase {

    @Autowired
    private MFCRHandler mfcrHandler;

    @Autowired
    private DatabaseUtils databaseUtils;

    @Autowired
    private PartnerListProcessor partnerListProcessor;

    @Autowired
    private TransformDriver transformDriver;

    @Autowired
    private RelationshipResolver relationshipResolver;

    @PersistenceContext
    private EntityManager em;


    @Test
    @Transactional
    public void testCreateInvoicesDataInstances() throws Exception {
        DataSource ds = new DataSource();
        ds.setDataInstances(new ArrayList<>());
        ds.setRecordType(RecordType.INVOICE);

        EntityManager mockEm = mock(EntityManager.class);
        mfcrHandler.setEm(mockEm);

        PartnerListProcessor mockPlp = mock(PartnerListProcessor.class);
        mfcrHandler.setPartnerListProcessor(mockPlp);

        mfcrHandler.updateDataInstances(ds);
        Collection<DataInstance> dataInstanceList = ds.getDataInstances();
        Assertions.assertThat(dataInstanceList.size()).isGreaterThanOrEqualTo(1);

        verify(mockPlp).processListOfPartners(Matchers.eq(ds), any(InputStream.class));

    }

    @Test
    public void testCreateContractsDataInstance() throws Exception {
        DataSource ds = new DataSource();
        ds.setDataInstances(new ArrayList<>());
        ds.setRecordType(RecordType.CONTRACT);

        EntityManager mockEm = mock(EntityManager.class);
        mfcrHandler.setEm(mockEm);

        mfcrHandler.updateDataInstances(ds);
        Collection<DataInstance> dataInstanceList = ds.getDataInstances();
        assertEquals(1, dataInstanceList.size());
        assertEquals(dataInstanceList.iterator().next().getDescription(), "Platné a neplatné smlouvy MF");
    }

    @Test
    @Transactional
    public void testProcessInvoiceWorkbook() throws Exception {
        databaseUtils.cleanRecords();

        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xlsx");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("mfcr/test-invoices.xlsx").getInputStream();

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
        assertEquals(39, recordList.size());

    }

    @Test
    @Transactional
    public void testProcessContractsWorkbook() throws Exception {
        databaseUtils.cleanRecords();

        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("mfcr/test-contracts.xls").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        em.persist(entity);
        DataSource ds = DataGenerator.getDataSource(entity);
        ds.setRecordType(RecordType.CONTRACT);
        em.persist(ds);
        dataInstance.setDataSource(ds);
        em.persist(dataInstance);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "mappings/mfcr/mapping-contracts.xml", inputStream);
        em.persist(retrieval);

        List<Record> recordList = em.createQuery(
                "SELECT r FROM Record r WHERE r.retrieval = :retr", Record.class)
                .setParameter("retr", retrieval)
                .getResultList();
        assertEquals(39, recordList.size());

        // Make sure we are setting parents for amendments
        List<Record> amendments =
                recordList.stream().filter(i -> i.getAuthorityIdentifier().length() > 10).collect(Collectors.toList());

        assertNotNull(amendments.get(0).getParentRecord());

        // Connection to invoices
        List<UnresolvedRelationship> unresolvedRelationships = em.createQuery(
                "Select u from UnresolvedRelationship u", UnresolvedRelationship.class)
                .getResultList();
        assertEquals(16, unresolvedRelationships.size());
    }

    @Test
    @Transactional
    public void testProcessPartnersAndOldInvoices() throws Exception {
        databaseUtils.cleanRecords();

        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xlsx");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("mfcr/test-old-invoices.xlsx").getInputStream();

        Entity entity = em.createQuery("Select e from Entity e where e.ico = '00006947'", Entity.class)
                .getSingleResult();
        DataSource ds = DataGenerator.getDataSource(entity);
        ds.setRecordType(RecordType.CONTRACT);
        em.persist(ds);
        dataInstance.setDataSource(ds);
        em.persist(dataInstance);

        InputStream partnerInputStream = new ClassPathResource("mfcr/test-partners.xlsx").getInputStream();
        partnerListProcessor.processListOfPartners(ds, partnerInputStream);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "mappings/mfcr/mapping-old-invoices.xml", inputStream);
        em.persist(retrieval);

        List<Record> recordList = em.createQuery(
                "SELECT r FROM Record r WHERE r.retrieval = :retr", Record.class)
                .setParameter("retr", retrieval)
                .getResultList();
        assertEquals(23, recordList.size());

        List<Entity> entityList = em.createQuery("Select e FROM Entity e WHERE e.public = false", Entity.class)
                .getResultList();
        assertEquals(15, entityList.size());

        for(Record record : recordList) {
            assertNotNull(record.getPartner());
        }
    }

}
