package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.control.DownloadService;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.*;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by dm on 12/2/15.
 */
@Component
public class TransformDriverImpl implements TransformDriver {

    @Autowired
    private WorkbookProcessor workbookProcessor;

    @Autowired
    private ResourceLoader resourceLoader;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DownloadService downloadService;

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd__HH_mm_ss.SSS").withZone(ZoneId.systemDefault());

    // The default value is only used for testing, it's overwritten in doRetrieval
    private Logger log = LogManager.getLogger(TransformDriverImpl.class);

    @Override
    public Retrieval doRetrieval(DataInstance dataInstance) {
        return doRetrieval(dataInstance, dataInstance.getMappingFile(), null);
    }

    @Override
    public Retrieval doRetrieval(DataInstance dataInstance, String mappingFile, InputStream inputStream) {

        ThreadContext.put("TIMESTAMP", formatter.format(Instant.now()));
        log = LogManager.getLogger("transform");
        log.info("Starting retrieval on data instance " + dataInstance.getDataInstanceId());

        Retrieval retrieval = new Retrieval();
        retrieval.setDataInstance(dataInstance);
        retrieval.setDate(Timestamp.from(Instant.now()));
        retrieval.setRecords(new ArrayList<>());

        Integer oldLastProcessedRow = dataInstance.getLastProcessedRow();

        try {
            if(inputStream == null) {
                log.info("Downloading data file from " + dataInstance.getUrl());
                inputStream = downloadService.downloadDataFile(dataInstance);
            }

            Workbook workbook = openXLSFile(inputStream, dataInstance);
            Mapping mapping = loadMapping(mappingFile);
            workbookProcessor.processWorkbook(workbook, mapping, retrieval, log);

            log.info("Whole workbook procesed successfully");
            retrieval.setSuccess(true);
            em.merge(retrieval.getDataInstance()); // Save last processed row
        }
        catch (Exception e) {
            log.error("An irrecoverable error occurred while performing transformation", e);
            retrieval.setSuccess(false);
            retrieval.setFailureReason(e.getMessage());
            retrieval.setNumRecordsInserted(0);
            retrieval.setRecords(new ArrayList<>());
            dataInstance.setLastProcessedRow(oldLastProcessedRow);
        }

        ThreadContext.clearAll();
        return retrieval;
    }

    @Override
    public Workbook openXLSFile(InputStream inputStream, DataInstance dataInstance) throws IOException {
        log.debug("Opening file with format " + dataInstance.getFormat());
        if(dataInstance.getFormat().equals("xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            return new XSSFWorkbook(inputStream);
        }
    }


    @Override
    public Mapping loadMapping(String mappingFile) throws JAXBException, IOException {
        Resource resource = resourceLoader.getResource(mappingFile);
        JAXBContext jaxbContext = JAXBContext.newInstance(Mapping.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        return (Mapping) u.unmarshal(resource.getURL().openStream());
    }

    //Test
    @Override
    public void setEm(EntityManager em) {
        this.em = em;
        if(this.workbookProcessor.getEm() == null) {
            this.workbookProcessor.setEm(em);
        }
    }

    @Override
    public EntityManager getEm() {
        return em;
    }
}
