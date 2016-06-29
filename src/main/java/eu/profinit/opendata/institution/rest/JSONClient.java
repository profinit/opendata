package eu.profinit.opendata.institution.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import javax.annotation.PostConstruct;
import java.net.URI;

/**
 * Created by dm on 11/28/15.
 */
@Component
public class JSONClient {

    private RestTemplate restTemplate;

    private Logger log = LogManager.getLogger(JSONClient.class);

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    public JSONPackageList getPackageList(String apiUrl, String packagesPath, String packageListIdentifier) {
        try {
            URI uri = URI.create(apiUrl + packagesPath + "?id=" + packageListIdentifier);
            log.debug("Downloading package list from " + uri.toString());
            return restTemplate.getForObject(uri, JSONPackageList.class);
        } catch (RestClientException e) {
            log.error("Could not retreive package list", e);
            return null;
        }
    }

    public JSONPackageListMOCR getPackageListMOCR(String apiUrl, String packagesPath, String packageListIdentifier) {
        try {
            URI uri = URI.create(apiUrl + packagesPath + "?id=" + packageListIdentifier);
            log.debug("Downloading package list from " + uri.toString());
            return restTemplate.getForObject(uri, JSONPackageListMOCR.class);
        } catch (RestClientException e) {
            log.error("Could not retreive package list", e);
            return null;
        }
    }

}
