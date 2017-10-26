package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.poi.ss.usermodel.Workbook;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A component for initiating the extraction of a DataInstance (a Retrieval). The TransformDriver will download the
 * physical data file, open it, load its mapping and pass it to the WorkbookProcessor to do the actual work.
 * @see WorkbookProcessor
 * @see DataInstance
 */
public interface TransformDriver {
    /**
     * Initiates the extraction process on the specified DataInstance. This includes downloading the data file from
     * a remote host.
     * @param dataInstance The data instance to be processed.
     * @return Retrieval object containing the extraction metadata.
     * @see TransformDriver#doRetrieval(DataInstance, String, InputStream)
     */
    Retrieval doRetrieval(DataInstance dataInstance);

    /**
     * Initiates the extraction process on the data specified by the inputStream using the specified mapping file. This
     * method assumes the download has already been performed. Opens the file as an Apache POI Workbook, loads its
     * mapping, creates a new Retrieval object for the extraction, passes the file to the WorkbookProcessor and handles
     * any exceptions thrown during the extraction process.
     * @param dataInstance The data instance to be processed. Its URL and mapping file attriutes are ignored.
     * @param mappingFile The mapping file to be used for the extraction.
     * @param inputStream An InputStream containing data from an opened Excel file.
     * @return Retrieval object containing the extraction metadata.
     * @see WorkbookProcessor
     */
    Retrieval doRetrieval(DataInstance dataInstance, String mappingFile, InputStream inputStream);

    /**
     * Opens an xls(x) file with Apache POI and returns a Workbook object.
     * @param inputStream The input stream containing the file data.
     * @param dataInstance The data instance specifying the format of the file.
     * @return An opened Workbook
     * @throws IOException
     */
    Workbook openXLSFile(InputStream inputStream, DataInstance dataInstance) throws IOException;

    /**
     * Loads an XML mapping file.
     * @param mappingFile The path to the mapping file. Must be on the classpath.
     * @return The Mapping loaded from the XML file.
     * @throws JAXBException
     * @throws IOException
     */
    Mapping loadMapping(String mappingFile) throws JAXBException, IOException;

    // Testing purposes
    void setEm(EntityManager em);
    EntityManager getEm();

}
