package eu.profinit.opendata.institution.mfcr.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.control.DownloadService;
import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.institution.mfcr.MFCRHandler;
import eu.profinit.opendata.institution.mfcr.PartnerListProcessor;
import eu.profinit.opendata.institution.mfcr.rest.JSONClient;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageList;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageListResource;
import eu.profinit.opendata.model.*;
import eu.profinit.opendata.query.PartnerQueryService;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.SyslogAppender;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dm on 11/28/15.
 */
@Component
public class MFCRHandlerImpl extends GenericDataSourceHandler implements MFCRHandler {

    @Autowired
    private JSONClient jsonClient;

    @Autowired
    private PartnerListProcessor partnerListProcessor;

    @Autowired
    private DownloadService downloadService;

    @Value("${mfcr.json.orders.identifier}")
    private String orders_identifier;

    @Value("${mfcr.json.invoices.identifier}")
    private String invoices_identifier;

    @Value("${mfcr.json.contracts.identifier}")
    private String contracts_identifier;

    @Value("${mfcr.mapping.orders}")
    private String order_mapping_file;

    @Value("${mfcr.mapping.invoices}")
    private String invoices_mapping_file;

    @Value("${mfcr.mapping.old.invoices}")
    private String old_invoices_mapping_file;

    @Value("${mfcr.mapping.contracts}")
    private String contracts_mapping_file;

    private Logger log = LogManager.getLogger(MFCRHandler.class);

    @Override
    public void updateDataInstances(DataSource ds) {
        switch(ds.getRecordType()) {
            case ORDER: updateOrdersOrContractsDataInstance(ds, orders_identifier, order_mapping_file); break;
            case INVOICE: updateInvoicesDataInstance(ds); break;
            case CONTRACT: updateOrdersOrContractsDataInstance(ds, contracts_identifier, contracts_mapping_file); break;
            default: break;
        }
    }

    @Override
    public void setPartnerListProcessor(PartnerListProcessor plp) {
        this.partnerListProcessor = plp;
    }

    /**
     * Updates the data intances associated with the specified ORDERS data source.
     * Assumes that the JSON API only returns a single xls(x) resource (true as of Nov 2015)
     * @param ds An ORDERS DataSource
     */
    public void updateOrdersOrContractsDataInstance(DataSource ds, String identifier, String mappingFile) {
        log.info("Updating information about data instances containing orders");

        //Load list of resources from the JSON API
        JSONPackageList packageList = jsonClient.getPackageList(identifier);
        if(packageList == null) {
            log.warn("JSONClient returned null package list. Exiting.");
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().getResources();

        for(JSONPackageListResource resource : resourceList) {

            //Check if we found an xls resource
            if(resource.getFormat().equals("xls") || resource.getFormat().equals("xlsx")) {

                //Ignore if there is a data instance with the same URL
                String newUrl = resource.getUrl();
                log.debug("Received metadata for xls(x) resource at " + newUrl);

                if(currentInstances.stream()
                        .filter(i -> i.getUrl().toLowerCase().equals(newUrl.toLowerCase())).count() == 0) {

                    //All current data instances must be marked as expired
                    for(DataInstance i : currentInstances) {
                        log.debug("Marking existing DataInstance " + i.getDataInstanceId() + " as expired.");
                        i.expire();
                        em.merge(i);
                    }

                    //Recreate a new active data instance
                    DataInstance di = new DataInstance();
                    di.setDataSource(ds);
                    di.setFormat(resource.getFormat());
                    di.setPeriodicity(Periodicity.MONTHLY);
                    di.setUrl(resource.getUrl());
                    di.setDescription(resource.getName());
                    di.setMappingFile(mappingFile);

                    if(identifier.equals(contracts_identifier)) {
                        di.setIncremental(false);
                    }

                    ds.getDataInstances().add(di);
                    log.trace("Persisting new DataInstance");
                    em.persist(di);

                }
                else {
                    log.debug("Resource with given URL already exists as a DataInstance, nothing to do.");
                }
            }
        }
    }

    public void updateInvoicesDataInstance(DataSource ds) {
        log.info("Updating information about data instances containing invoices");

        JSONPackageList packageList = jsonClient.getPackageList(invoices_identifier);
        if(packageList == null) {
            log.warn("JSONClient returned null package list. Exiting.");
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().getResources();

        for(JSONPackageListResource resource : resourceList) {

            // Check if we found an xls resource
            if (resource.getFormat().equals("xls") || resource.getFormat().equals("xlsx")) {
                // Check for "uhrazene faktury" and "za rok {YYYY}" and not "privatizace"
                String name = resource.getName();

                if(name.contains("Seznam partnerů")) {
                    processPartnerListDataInstance(ds, resource);
                }

                Pattern pattern = Pattern.compile("^Uhrazené faktury(?: MF)? za rok (?<year>\\d{4})(?: včetně položky rozpočtu)?$");
                Matcher matcher = pattern.matcher(name);
                if(!matcher.find()) continue;

                Integer year = Integer.parseInt(matcher.group("year"));
                DataInstance dataInstance = new DataInstance();

                // Check if we already have a data instance with the same given id - if yes, simply update the URL
                // If not, create a new one
                Optional<DataInstance> sameIds = currentInstances.stream()
                        .filter(i -> i.getAuthorityId().equals(resource.getId())).findFirst();
                if(sameIds.isPresent()) {
                    dataInstance = sameIds.get();
                    dataInstance.setUrl(resource.getUrl());
                }
                else {
                    if(year < 2015) {
                        dataInstance.setMappingFile(old_invoices_mapping_file);
                    }
                    else {
                        dataInstance.setMappingFile(invoices_mapping_file);
                    }

                    dataInstance.setDataSource(ds);
                    dataInstance.setUrl(resource.getUrl());
                    dataInstance.setAuthorityId(resource.getId());
                    dataInstance.setFormat(resource.getFormat());
                    dataInstance.setDescription(resource.getName());
                    dataInstance.setPeriodicity(Periodicity.MONTHLY);
                    ds.getDataInstances().add(dataInstance);
                }

                // Get the year the data instance is holding data from - if in the past and has already been processed
                // after its year has ended, expire it
                Integer currentYear = new GregorianCalendar().get(Calendar.YEAR);
                Timestamp lpd = dataInstance.getLastProcessedDate();
                if(currentYear > year && lpd != null
                        && lpd.after(new GregorianCalendar(currentYear, Calendar.JANUARY, 1).getTime())) {
                    dataInstance.expire();
                }

                // Merge/persist the DataInstance
                em.persist(dataInstance);
            }
        }

    }

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
                partnerListProcessor.processListOfPartners(ds, is);
            } catch (IOException e) {
                log.error("Couldn't download or process list of partners", e);
            }
        }
        toProcess.setLastProcessedDate(Timestamp.from(Instant.now()));
        log.info("List of partners has been processed");
        em.merge(toProcess);
    }

}