package eu.profinit.opendata.test;

import eu.profinit.opendata.model.*;
import eu.profinit.opendata.test.converter.Killjoy;
import eu.profinit.opendata.transform.TransformDriver;
import eu.profinit.opendata.transform.impl.TransformDriverImpl;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.sql.Date;
import java.time.Instant;
import java.util.List;


import static org.mockito.Mockito.*;

/**
 * Created by dm on 12/10/15.
 */
public class TransformDriverTest extends ApplicationContextTestCase {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TransformDriver transformDriver;

    @Test
    public void testLoadMapping() throws Exception {
        Mapping mapping = transformDriver.loadMapping("test-mapping.xml");
        assertNotNull(mapping);
        assertEquals(1, mapping.getHeaderRow().intValue());
        assertEquals("test-mapping", mapping.getName());
    }

    @Test
    public void testOpenWorkbook() throws Exception {
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();
        Workbook workbook = transformDriver.openXLSFile(inputStream, dataInstance);

        assertNotNull(workbook);
        Sheet sheet = workbook.getSheetAt(0);
        assertEquals(0, sheet.getFirstRowNum());
        int nonEmptyRows = 0;
        for(int i = 0; i < sheet.getLastRowNum(); i++) {
            if(!TransformDriverImpl.isRowEmpty(sheet.getRow(i))) {
                nonEmptyRows++;
            }
        }

        assertEquals(31, nonEmptyRows);
    }

    @Test
    public void testValidateRecord() throws Exception {
        Record record = new Record();
        record.setMasterId("blah");
        record.setCurrency("JPY");
        record.setDateCreated(new Date(Instant.now().toEpochMilli()));
        record.setRecordType(RecordType.CONTRACT);

        transformDriver.checkRecordIntegrity(record); // Shouldn't throw an exception

        record.setMasterId(null);
        try {
            transformDriver.checkRecordIntegrity(record);
        } catch (TransformException ex) {
            assertEquals(TransformException.Severity.RECORD_LOCAL, ex.getSeverity());
            return;
        }

        fail("Exception wasn't thrown while checking the integrity of an invalid record");

    }

    @Test
    public void testTransform() throws Exception {
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        DataSource ds = DataGenerator.getDataSource(entity);
        dataInstance.setDataSource(ds);

        EntityManager mockEm = mock(EntityManager.class);
        transformDriver.setEm(mockEm);

        ArgumentCaptor<Record> persistArgumentCaptor = ArgumentCaptor.forClass(Record.class);
        ArgumentCaptor<Record> mergeArgumentCaptor = ArgumentCaptor.forClass(Record.class);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "test-mapping.xml", inputStream);
        verify(mockEm, times(2)).merge(mergeArgumentCaptor.capture()); //Retriever will return 1 old record + data instance merge
        verify(mockEm, times(26)).persist(persistArgumentCaptor.capture()); //26 remaining good rows in the test data instance
        assertEquals(27, retrieval.getNumRecordsInserted());
        assertEquals(2, retrieval.getNumBadRecords()); //Two bad rows in the test DI
        assertEquals(30, dataInstance.getLastProcessedRow().intValue());

        //Check whether the fixed value is set properly
        assertEquals(RecordType.ORDER, persistArgumentCaptor.getValue().getRecordType());
        assertEquals(490.0, persistArgumentCaptor.getValue().getOriginalCurrencyAmount());

        //Check whether values are preserved on old records
        assertEquals(RecordType.ORDER, mergeArgumentCaptor.getAllValues().get(0).getRecordType());
        assertEquals("123456789", mergeArgumentCaptor.getAllValues().get(0).getMasterId());

    }

    @Test
    public void testTransformWithFatalError() throws Exception {
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        DataSource ds = DataGenerator.getDataSource(entity);
        dataInstance.setDataSource(ds);

        EntityManager mockEm = mock(EntityManager.class);
        transformDriver.setEm(mockEm);

        //The mapping contains a Killjoy that will throw a FATAL exception
        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "bad-mapping.xml", inputStream);
        assertEquals(0, retrieval.getNumRecordsInserted());
        assertFalse(retrieval.isSuccess());
        assertEquals(Killjoy.MANIFESTO, retrieval.getFailureReason());
    }

    @Test
    public void testEntityManagerInjected() throws Exception {
        assertNotNull(transformDriver.getEm());
    }

    @Test
    @Transactional
    public void testTransactions() throws Exception {
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        em.persist(entity);
        DataSource ds = DataGenerator.getDataSource(entity);
        em.persist(ds);
        dataInstance.setDataSource(ds);
        em.persist(dataInstance);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "test-mapping.xml", inputStream);
        em.persist(retrieval);
        //Check there are now 26 new records
        List<Record> recordList = em.createQuery(
                "SELECT r FROM Record r WHERE r.retrieval = :retr", Record.class)
                .setParameter("retr", retrieval)
                .getResultList();
        assertEquals(26, recordList.size());


    }

    @Test
    @Transactional
    public void testRollback() throws Exception {
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        dataInstance.setUrl("http://example.me");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        em.persist(entity);
        DataSource ds = DataGenerator.getDataSource(entity);
        em.persist(ds);
        dataInstance.setDataSource(ds);
        em.persist(dataInstance);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, "bad-mapping.xml", inputStream);
        assertEquals(0, retrieval.getNumRecordsInserted());
        assertEquals(0, retrieval.getRecords().size());
        em.persist(retrieval);
        List<Record> recordList = em.createQuery(
                "SELECT r FROM Record r WHERE r.retrieval = :retr", Record.class)
                .setParameter("retr", retrieval)
                .getResultList();
        assertEquals(0, recordList.size());
        assertNull(dataInstance.getLastProcessedRow());
    }

}