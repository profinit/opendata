package eu.profinit.opendata.test;

import eu.profinit.opendata.model.*;
import eu.profinit.opendata.test.converter.Killjoy;
import eu.profinit.opendata.transform.TransformDriver;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.jaxb.Mapping;
import junit.framework.TestCase;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.Instant;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 * Created by dm on 12/10/15.
 */
public class TransformDriverTest extends TestCase {

    private ApplicationContext applicationContext;

    public TransformDriverTest() {
        applicationContext = new ClassPathXmlApplicationContext("beans.xml");
    }

    public void testLoadMapping() throws Exception {
        TransformDriver transformDriver = applicationContext.getBean(TransformDriver.class);
        Mapping mapping = transformDriver.loadMapping("test-mapping.xml");
        assertNotNull(mapping);
        assertEquals(1, mapping.getHeaderRow().intValue());
        assertEquals("test-mapping", mapping.getName());
    }

    public void testOpenWorkbook() throws Exception {
        TransformDriver transformDriver = applicationContext.getBean(TransformDriver.class);
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();
        Workbook workbook = transformDriver.openXLSFile(inputStream, dataInstance);

        assertNotNull(workbook);
        Sheet sheet = workbook.getSheetAt(0);
        assertEquals(0, sheet.getFirstRowNum());
        int nonEmptyRows = 0;
        for(int i = 0; i < sheet.getLastRowNum(); i++) {
            if(!transformDriver.isRowEmpty(sheet.getRow(i))) {
                nonEmptyRows++;
            }
        }

        assertEquals(31, nonEmptyRows);
    }

    public void testValidateRecord() throws Exception {
        TransformDriver transformDriver = applicationContext.getBean(TransformDriver.class);
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

    public void testTransform() throws Exception {
        TransformDriver transformDriver = applicationContext.getBean(TransformDriver.class);
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        DataSource ds = DataGenerator.getDataSource(entity);
        dataInstance.setDataSource(ds);

        EntityManager mockEm = mock(EntityManager.class);
        transformDriver.setEm(mockEm);
        EntityTransaction mockTransaction = mock(EntityTransaction.class);
        when(mockEm.getTransaction()).thenReturn(mockTransaction);

        ArgumentCaptor<Record> persistArgumentCaptor = ArgumentCaptor.forClass(Record.class);
        ArgumentCaptor<Record> mergeArgumentCaptor = ArgumentCaptor.forClass(Record.class);

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, inputStream, "test-mapping.xml");
        verify(mockEm, times(1)).merge(mergeArgumentCaptor.capture()); //Retriever will return 1 old record
        verify(mockEm, times(26)).persist(persistArgumentCaptor.capture()); //26 remaining good rows in the test data instance
        assertEquals(27, retrieval.getNumRecordsInserted());
        assertEquals(2, retrieval.getNumBadRecords()); //Two bad rows in the test DI

        //Check whether the fixed value is set properly
        assertEquals(RecordType.ORDER, persistArgumentCaptor.getValue().getRecordType());
        assertEquals(490.0, persistArgumentCaptor.getValue().getOriginalCurrencyAmount());

        //Check whether values are preserved on old records
        assertEquals(RecordType.ORDER, mergeArgumentCaptor.getValue().getRecordType());
        assertEquals("123456789", mergeArgumentCaptor.getValue().getMasterId());
    }

    public void testTransformWithFatalError() throws Exception {
        TransformDriver transformDriver = applicationContext.getBean(TransformDriver.class);
        DataInstance dataInstance = new DataInstance();
        dataInstance.setFormat("xls");
        InputStream inputStream = new ClassPathResource("test-orders.xls").getInputStream();

        Entity entity = DataGenerator.getTestMinistry();
        DataSource ds = DataGenerator.getDataSource(entity);
        dataInstance.setDataSource(ds);

        EntityManager mockEm = mock(EntityManager.class);
        transformDriver.setEm(mockEm);
        EntityTransaction mockTransaction = mock(EntityTransaction.class);
        when(mockEm.getTransaction()).thenReturn(mockTransaction);

        //The mapping contains a Killjoy that will throw a FATAL exception
        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, inputStream, "bad-mapping.xml");
        assertEquals(0, retrieval.getNumRecordsInserted());
        assertFalse(retrieval.isSuccess());
        assertEquals(Killjoy.MANIFESTO, retrieval.getFailureReason());
        verify(mockTransaction, times(1)).rollback();
    }

}