package eu.profinit.opendata.business;

import eu.profinit.opendata.model.DataInstance;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by DM on 19. 11. 2015.
 */
public class Downloader {

    public InputStream downloadDataFile(DataInstance dataInstance) throws IOException {
        URL url  = new URL(dataInstance.getUrl());
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        return Channels.newInputStream(rbc);
    }
}

