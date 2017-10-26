package eu.profinit.opendata.institution.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by dm on 11/28/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONPackageListResult {
    private String id;
    private String name;
    private String title;
    private String url;
    private String state;
    private String metadataCreated;
    private String metadataModified;
    private List<JSONPackageListResource> resources;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMetadataCreated() {
        return metadataCreated;
    }

    public void setMetadataCreated(String metadataCreated) {
        this.metadataCreated = metadataCreated;
    }

    public String getMetadataModified() {
        return metadataModified;
    }

    public void setMetadataModified(String metadataModified) {
        this.metadataModified = metadataModified;
    }

    public List<JSONPackageListResource> getResources() {
        return resources;
    }

    public void setResources(List<JSONPackageListResource> resources) {
        this.resources = resources;
    }
}
