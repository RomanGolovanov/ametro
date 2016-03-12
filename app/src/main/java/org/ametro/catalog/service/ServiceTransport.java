package org.ametro.catalog.service;

import org.ametro.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class ServiceTransport implements IServiceTransport {

    @Override
    public String httpGet(URI uri) throws IOException {
        URL url = new URL(uri.toASCIIString());
        return FileUtils.readAllText(url.openStream());
    }
}
