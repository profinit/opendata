package eu.profinit.opendata.control;

import eu.profinit.opendata.model.DataInstance;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dm on 12/19/15.
 */
public interface DownloadService {
    InputStream downloadDataFile(DataInstance dataInstance) throws IOException;
    InputStream downloadDataFile(String url) throws IOException;
}
