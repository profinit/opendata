package eu.profinit.opendata.institution.rest;

import java.util.List;

/**
 * Created by dm on 6/29/16.
 */
public class JSONPackageListMOCR {
    private String help;
    private boolean success;
    private List<JSONPackageListResult> result;

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

    public List<JSONPackageListResult> getResult() {
        return result;
    }

    public void setResult(List<JSONPackageListResult> result) {
        this.result = result;
    }
}
