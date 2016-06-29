package eu.profinit.opendata.institution.mocr.impl;

import eu.profinit.opendata.control.DownloadService;
import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.institution.mocr.MOCRHandler;
import eu.profinit.opendata.institution.rest.JSONClient;
import eu.profinit.opendata.institution.rest.JSONPackageList;
import eu.profinit.opendata.institution.rest.JSONPackageListMOCR;
import eu.profinit.opendata.institution.rest.JSONPackageListResource;
import eu.profinit.opendata.model.*;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementation of the MO handler. Uses the ministry's REST API to keep track
 * of DataInstances. Nothing needs to be done manually for this ministry, unless
 * file formats change.
 */
@Component
public class MOCRHandlerImpl extends GenericDataSourceHandler implements MOCRHandler {

    @Autowired
    private JSONClient jsonClient;

    @Autowired
    private DownloadService downloadService;

    @Value("${mocr.json.invoices.identifier}")
    private String invoices_identifier;

    @Value("${mocr.json.contracts.identifier}")
    private String contracts_identifier;

    @Value("${mocr.mapping.invoices}")
    private String invoices_mapping_file;

    @Value("${mocr.mapping.contracts}")
    private String contracts_mapping_file;

    @Value("${mocr.json.api.url}")
    private String json_api_url;

    @Value("${mocr.json.packages.url}")
    private String packages_path;


    private final Logger log = LogManager.getLogger(MOCRHandler.class);

    @Override
    public void updateDataInstances(DataSource ds) {
        switch (ds.getRecordType()) {
            case INVOICE:
                updateInvoicesDataInstance(ds);
                break;
            case CONTRACT:
                updateContractsDataInstance(ds);
                break;
            default:
                break;
        }
    }

    /**
     * Updates the data intances associated with the MO orders or contracts data
     * source. Assumes that the JSON API only returns a single xls(x) resource.
     * If the URL changes, a new DataInstance is created and old ones are
     * expired.
     *
     * @param ds The MO contracts DataSource
     */
    public void updateContractsDataInstance(DataSource ds) {

        //Load list of resources from the JSON API
        JSONPackageListMOCR packageList = jsonClient.getPackageListMOCR(json_api_url, packages_path, this.contracts_identifier);
        if (packageList == null) {
            log.warn("JSONClient returned null package list. Exiting.");
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().get(0).getResources();

        for (JSONPackageListResource resource : resourceList) {

            //Check if we found an xls resource
            if (resource.getFormat().equals("excel")) {
                String name = resource.getName();

                Pattern pattern = Pattern.compile("^Smlouvy uzavřené na TENDERMARKET (?<year>\\d{4})$");
                Matcher matcher = pattern.matcher(name);
                if (!matcher.find()) {
                    continue;
                }

                Integer year = Integer.parseInt(matcher.group("year"));
                DataInstance dataInstance = new DataInstance();

                String newUrl = resource.getUrl();
                log.debug("Received metadata for xls(x) resource at " + newUrl);

                // Check if we already have a data instance with the same given id - if yes, simply update the URL
                // If not, create a new one
                Optional<DataInstance> sameIds = currentInstances.stream()
                        .filter(i -> i.getAuthorityId().equals(resource.getId())).findFirst();
                if (sameIds.isPresent()) {
                    dataInstance = sameIds.get();
                    dataInstance.setUrl(resource.getUrl());
                } else {

                    dataInstance.setMappingFile(contracts_mapping_file);
                    dataInstance.setDataSource(ds);
                    dataInstance.setUrl(resource.getUrl());
                    dataInstance.setAuthorityId(resource.getId());
                    dataInstance.setFormat("xlsx");
                    dataInstance.setDescription(resource.getName());
                    dataInstance.setPeriodicity(Periodicity.MONTHLY);
                    dataInstance.setIncremental(false);
                    ds.getDataInstances().add(dataInstance);
                    em.persist(dataInstance);
                }

                //We are not expiring data instances from earlier years since they may still be revised in the future.

                em.merge(dataInstance);
            }
        }
    }

    /**
     * Updates the invoice DataInstances from the JSON API. There should be one
     * for every year since 2010, with files from 2015 onwards having a
     * different format (and a different mapping file). Also finds the partner
     * list used to extract entities for the older format and initiates its
     * processing. Old files that have been updated and processed after their
     * year has ended are expired.
     *
     * @param ds The MF invoices DataSources
     */
    public void updateInvoicesDataInstance(DataSource ds) {
        log.info("Updating information about data instances containing invoices");

        JSONPackageListMOCR packageList = jsonClient.getPackageListMOCR(json_api_url, packages_path, invoices_identifier);
        if (packageList == null) {
            log.warn("JSONClient returned null package list. Exiting.");
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().get(0).getResources();

        for (JSONPackageListResource resource : resourceList) {

            // Check if we found an xls resource
            if (resource.getFormat().equals("excel")) {
                // Check for "uhrazene faktury" and "za rok {YYYY}" and not "privatizace"
                String name = resource.getName();

                Pattern pattern = Pattern.compile("^Uhrazené faktury za rok (?<year>\\d{4})$");
                Matcher matcher = pattern.matcher(name);
                if (!matcher.find()) {
                    continue;
                }

                Integer year = Integer.parseInt(matcher.group("year"));
                DataInstance dataInstance = new DataInstance();

                // Check if we already have a data instance with the same given id - if yes, simply update the URL
                // If not, create a new one
                Optional<DataInstance> sameIds = currentInstances.stream()
                        .filter(i -> i.getAuthorityId().equals(resource.getId())).findFirst();
                if (sameIds.isPresent()) {
                    dataInstance = sameIds.get();
                    dataInstance.setUrl(resource.getUrl());
                } else {

                    dataInstance.setMappingFile(invoices_mapping_file);

                    dataInstance.setDataSource(ds);
                    dataInstance.setUrl(resource.getUrl());
                    dataInstance.setAuthorityId(resource.getId());
                    dataInstance.setFormat("xlsx");
                    dataInstance.setDescription(resource.getName());
                    dataInstance.setPeriodicity(Periodicity.MONTHLY);
                    dataInstance.setIncremental(false);
                    ds.getDataInstances().add(dataInstance);
                    em.persist(dataInstance);
                }

                em.merge(dataInstance);

            }
        }

    }

}
