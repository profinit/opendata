package eu.profinit.opendata.institution.rest.util;

import org.springframework.util.Assert;

import eu.profinit.opendata.institution.rest.JSONPackageListResource;

public class JSONPackageListResourceUtil {
    
    private static final String NEW_XLS_FORMAT_EXTENSION = "xlsx";
    private static final String OLD_XLS_FORMAT_EXTENSION = "xls";

    private JSONPackageListResourceUtil() {}
    
    /**
     * Checks if given resource is excel xls or xlsx file
     * @param resource
     * @return
     */
    public static boolean isXLSResource(JSONPackageListResource resource) {
        String format = resource.getFormat();
        Assert.notNull(format, "Resource format is null!");
        return format.equalsIgnoreCase(OLD_XLS_FORMAT_EXTENSION) 
                || format.equalsIgnoreCase(NEW_XLS_FORMAT_EXTENSION);
    }
    
}
