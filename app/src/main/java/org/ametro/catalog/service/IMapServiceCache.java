package org.ametro.catalog.service;

import java.io.File;
import java.io.IOException;

public interface IMapServiceCache {

    void refreshCache() throws ServiceUnavailableException, IOException;

    boolean hasValidCache();

    File getMapCatalogFile();

    File getLocalizationFile();

    void invalidateCache();
}
