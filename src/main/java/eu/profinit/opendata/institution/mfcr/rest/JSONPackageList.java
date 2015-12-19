package eu.profinit.opendata.institution.mfcr.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by dm on 11/28/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONPackageList {
    private String help;
    private boolean success;
    private JSONPackageListResult result;

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public JSONPackageListResult getResult() {
        return result;
    }

    public void setResult(JSONPackageListResult result) {
        this.result = result;
    }
}
