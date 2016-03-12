package org.ametro.catalog.service;

import java.io.IOException;
import java.net.URI;

public interface IServiceTransport {
    String httpGet(URI url) throws IOException;
}
