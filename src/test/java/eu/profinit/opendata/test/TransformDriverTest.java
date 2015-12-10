package eu.profinit.opendata.test;

import eu.profinit.opendata.model.*;
import eu.profinit.opendata.transform.TransformDriver;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.jaxb.Mapping;
import junit.framework.TestCase;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.InputStream;
import java.sql.Date;
import java.time.Instant;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
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

        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, inputStream, "test-mapping.xml");
        assertEquals(27, retrieval.getNumRecordsInserted());
        assertEquals(2, retrieval.getNumBadRecords());

        //Check individual records too
    }

    public void testTransformWithFatalError() {

    }

}