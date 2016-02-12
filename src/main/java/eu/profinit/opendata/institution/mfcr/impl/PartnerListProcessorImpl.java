package eu.profinit.opendata.institution.mfcr.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.control.DownloadService;
import eu.profinit.opendata.institution.mfcr.MFCRHandler;
import eu.profinit.opendata.institution.mfcr.PartnerListProcessor;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageListResource;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.Periodicity;
import eu.profinit.opendata.query.PartnerQueryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Created by dm on 2/12/16.
 */
@Component
public class PartnerListProcessorImpl implements PartnerListProcessor {

    @PersistenceContext
    private EntityManager em;

    private Logger log = LogManager.getLogger(PartnerListProcessor.class);

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private PartnerQueryService partnerQueryService;


    @Override
    public void processPartnerListDataInstance(DataSource ds, JSONPackageListResource resource) {
        log.info("Will download and process list of partners. This will take a few minutes.");
        Optional<DataInstance> oldPartnerInstance = ds.getDataInstances().stream()
                .filter(i -> i.getDescription().contains("Seznam partnerů")).findFirst();

        DataInstance toProcess = new DataInstance();
        if(oldPartnerInstance.isPresent()) {
            toProcess = oldPartnerInstance.get();
            toProcess.setUrl(resource.getUrl());
            em.merge(toProcess);
        }
        else {
            toProcess.setDataSource(ds);
            toProcess.setUrl(resource.getUrl());
            toProcess.setFormat("xlsx");
            toProcess.setPeriodicity(Periodicity.APERIODIC);
            ds.getDataInstances().add(toProcess);
            em.persist(toProcess);
        }

        Timestamp lpd = toProcess.getLastProcessedDate();
        if(lpd == null || Util.hasEnoughTimeElapsed(lpd, Duration.ofDays(30))) {
            try {
                InputStream is = downloadService.downloadDataFile(toProcess.getUrl());
                log.debug("Got partner list. Extracting entities");
                processListOfPartners(ds, is);
            } catch (IOException e) {
                log.error("Couldn't download or process list of partners", e);
            }
        }
        toProcess.setLastProcessedDate(Timestamp.from(Instant.now()));
        log.info("List of partners has been processed");
        em.merge(toProcess);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = {IOException.class, RuntimeException.class})
    public void processListOfPartners(DataSource ds, InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        for(int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if(Util.isRowEmpty(row)) continue;

            String partnerCode = row.getCell(0).getStringCellValue();
            String ico = row.getCell(1).getStringCellValue();
            String name = row.getCell(2).getStringCellValue();

            log.trace("Processing entity " + name);
            Entity partner = partnerQueryService.findOrCreateEntity(name, ico, null);
            partnerQueryService.findOrCreatePartnerListEntry(ds.getEntity(), partner, partnerCode);
        }
    }

    @Override
    public void setEm(EntityManager em) {
        this.em = em;
    }
}
