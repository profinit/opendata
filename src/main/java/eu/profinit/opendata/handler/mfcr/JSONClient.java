package eu.profinit.opendata.handler.mfcr;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;

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

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    public JSONPackageList getPackageList(String packageListIdentifier) {
        try {
            URI uri = URI.create(json_api_url + packages_path + "?id=" + packageListIdentifier);
            return restTemplate.getForObject(uri, JSONPackageList.class);
        } catch (RestClientException e) {
            Logger.getLogger(JSONClient.class).error("Could not retreive MFCR package list", e);
            return null;
        }
    }

}
