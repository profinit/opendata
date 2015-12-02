package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import eu.profinit.opendata.transform.jaxb.RecordProperty;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Created by dm on 12/2/15.
 */
@Component
public class TransformDriver {

    @PersistenceContext(unitName = "postgres")
    private EntityManager em;

    private Logger log = Logger.getLogger(TransformDriver.class);

    public Retrieval doRetrieval(DataInstance dataInstance, InputStream inputStream, String mappingFile) {
        Retrieval retrieval = new Retrieval();
        retrieval.setDataInstance(dataInstance);
        retrieval.setDate(Timestamp.from(Instant.now()));

        em.getTransaction().begin();
        try {
            Workbook workbook = openXLSFile(inputStream, dataInstance);
            Mapping mapping = loadMapping(mappingFile);
            processWorkbook(workbook, mapping, retrieval);
            em.getTransaction().commit();
        }
        catch (IOException | JAXBException e) {
            log.error("Couldn't process downloaded file", e);
            em.getTransaction().rollback();
            retrieval.setSuccess(false);
            retrieval.setFailureReason(e.getMessage());
        }
        catch (TransformException e) {
            log.error("An irrecoverable error occurred while performing transformation", e);
            em.getTransaction().rollback();
            retrieval.setSuccess(false);
            retrieval.setFailureReason(e.getMessage());
        }

        return retrieval;
    }

    private Workbook openXLSFile(InputStream inputStream, DataInstance dataInstance) throws IOException {
        if(dataInstance.getFormat().equals("xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            return new XSSFWorkbook(inputStream);
        }
    }

    private Mapping loadMapping(String mappingFile) throws JAXBException, IOException {
        ClassPathResource cpr = new ClassPathResource(mappingFile);
        JAXBContext jaxbContext = JAXBContext.newInstance(Mapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        JAXBElement<?> mappingJAXBElement = (JAXBElement<?>) u.unmarshal(cpr.getFile());
        return (Mapping) mappingJAXBElement.getValue();
    }

    //TODO: This could turn ugly if workbooks have nonstandard structure
    private void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval) throws TransformException {
        Sheet sheet = workbook.getSheetAt(0);  //TODO: Mapping file should specify how to handle multiple sheets
        int start_row_num = mapping.getHeaderRow().intValue() + 1; //TODO: This won't work for incrementally updated files

        for(int i = start_row_num; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            //Create the Record
            Record record = new Record();
            record.setRetrieval(retrieval);
            record.setAuthority(retrieval.getDataInstance().getDataSource().getEntity());
            boolean recordIsGood = true;

            //Loop through RecordProperties of the mapping
            for(RecordProperty recordProperty : mapping.getProperty()) {
                //For each property, either set the corresponding fixed value by resolving a string
                if(recordProperty.getValue() != null) {

                }
                else {
                    //Or instantiate and call the corresponding converter with a hashmap of arguments
                }


                //Catch exceptions and act according to severity
                //TODO: Write me!

                //Finally: save the Record if everything went OK
                if(recordIsGood) {
                    retrieval.setNumRecordsInserted(retrieval.getNumRecordsInserted() + 1);
                    em.persist(record);
                }
            }


        }
    }


}
