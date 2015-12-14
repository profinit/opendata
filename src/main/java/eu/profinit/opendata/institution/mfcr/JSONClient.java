package eu.profinit.opendata.institution.mfcr;

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

    @Value("${mfcr.json.api.url}")
    private String json_api_url;

    @Value("${mfcr.json.packages.url}")
    private String packages_path;

    private RestTemplate restTemplate;

    private Logger log = LogManager.getLogger(JSONClient.class);

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    public JSONPackageList getPackageList(String packageListIdentifier) {
        try {
            URI uri = URI.create(json_api_url + packages_path + "?id=" + packageListIdentifier);
            log.debug("Downloading package list from " + uri.toString());
            return restTemplate.getForObject(uri, JSONPackageList.class);
        } catch (RestClientException e) {
            log.error("Could not retreive MFCR package list", e);
            return null;
        }
    }

}
