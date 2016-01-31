package eu.profinit.opendata.institution.mfcr.impl;

import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.institution.mfcr.MFCRHandler;
import eu.profinit.opendata.institution.mfcr.rest.JSONClient;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageList;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageListResource;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.SyslogAppender;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${mfcr.json.orders.identifier}")
    private String orders_identifier;

    @Value("${mfcr.json.invoices.identifier}")
    private String invoices_identifier;

    @Value("${mfcr.mapping.orders}")
    private String order_mapping_file;

    @Value("${mfcr.mapping.invoices}")
    private String invoices_mapping_file;

    private Logger log = LogManager.getLogger(MFCRHandler.class);

    @Override
    protected void updateDataInstances(DataSource ds) {
        switch(ds.getRecordType()) {
            case ORDER: updateOrdersDataInstance(ds); break;
            case INVOICE: updateInvoicesDataInstance(ds); break;
            default: break;
        }
    }

    @Override
    protected String getMappingFileForDataInstance(DataInstance di) {
        switch(di.getDataSource().getRecordType()) {
            case ORDER: return order_mapping_file;
            case INVOICE: return invoices_mapping_file;
            default: return null;
        }
    }

    /**
     * Updates the data intances associated with the specified ORDERS data source.
     * Assumes that the JSON API only returns a single xls(x) resource (true as of Nov 2015)
     * @param ds An ORDERS DataSource
     */
    @Transactional
    private void updateOrdersDataInstance(DataSource ds) {
        log.info("Updating information about data instances containing orders");

        //Load list of resources from the JSON API
        JSONPackageList packageList = jsonClient.getPackageList(orders_identifier);
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


    @Transactional
    private void updateInvoicesDataInstance(DataSource ds) {
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
                Pattern pattern = Pattern.compile("^Uhrazené faktury(?: MF)? za rok (?<year>\\d{4})(?: včetně položky rozpočtu)$");
                Matcher matcher = pattern.matcher(name);
                if(!matcher.find()) continue;

                Integer year = Integer.parseInt(matcher.group("year"));
                if(year < 2015) {
                    // Older instances have a different format
                    continue;
                }

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
                    if(currentYear > year && dataInstance.getLastProcessedDate()
                            .after(new GregorianCalendar(currentYear, Calendar.JANUARY, 1).getTime())) {
                        dataInstance.expire();
                    }

                // Merge/persist the DataInstance
                em.persist(dataInstance);
            }
        }

    }

}