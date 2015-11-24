package eu.profinit.opendata.business;

import eu.profinit.opendata.model.DataInstance;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by dm on 11/24/15.
 */
public class DownloadService {
    public InputStream downloadDataFile(DataInstance dataInstance) throws IOException {
        URL url  = new URL(dataInstance.getUrl());
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        return Channels.newInputStream(rbc);
    }
}
