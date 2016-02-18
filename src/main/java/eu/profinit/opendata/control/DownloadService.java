package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataInstance;

import java.io.IOException;
import java.io.InputStream;

/**
 * A service for downloading data files from public remote URLs.
 */
public interface DownloadService {
    /**
     * Downloads the file residing at the URL of the specified DataInstance
     * @param dataInstance The DataInstance holding the URL of the data file.
     * @return An InputStream with the contents of the downloaded file
     * @throws IOException The data file could not be downloaded
     */
    InputStream downloadDataFile(DataInstance dataInstance) throws IOException;

    /**
     * Downloads the file residing at the specified URL.
     * @param url The remote address  of the data file.
     * @return An InputStream with the contents of the downloaded file
     * @throws IOException The data file could not be downloaded
     */
    InputStream downloadDataFile(String url) throws IOException;
}
