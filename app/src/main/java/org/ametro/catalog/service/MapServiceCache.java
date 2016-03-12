package org.ametro.catalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.ametro.model.serialization.CommonTypes;
import org.ametro.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class MapServiceCache implements IMapServiceCache {

    private final IServiceTransport serviceTransport;
    private final URI serviceUrl;
    private final String languageCode;
    private final File cacheDirectory;
    private final long ticksToExpiration;
    private static final ObjectReader reader;

    static {
        reader = new ObjectMapper().reader();
    }

    public MapServiceCache(IServiceTransport serviceTransport, URI serviceUrl, File workingDirectory, String languageCode, long ticksToExpiration) {
        this.serviceTransport = serviceTransport;
        this.serviceUrl = serviceUrl;
        this.languageCode = languageCode;
        this.cacheDirectory = new File(workingDirectory, "cache");
        this.ticksToExpiration = ticksToExpiration;
    }

    @Override
    public void refreshCache() throws ServiceUnavailableException, IOException {

        downloadFile("locales/locales.json", "locales.json");
        JsonNode json = reader.readTree(FileUtils.readAllText(new File(cacheDirectory, "locales.json")));
        for (String code : CommonTypes.asStringArray(json)) {
            String fileName = String.format("cities.%s.json", code);
            downloadFile("locales/" + fileName, fileName);
        }
        downloadFile("locales/cities.default.json", "cities.default.json");

        downloadFile("index.json", "index.json");
    }

    @Override
    public boolean hasValidCache() {
        return getMapCatalogFile().exists() &&
            (System.currentTimeMillis() - getMapCatalogFile().lastModified() < ticksToExpiration);
    }


    @Override
    public File getMapCatalogFile() {
        return new File(cacheDirectory, "index.json");
    }

    @Override
    public File getLocalizationFile() {
        File languageFile = new File(cacheDirectory, String.format("cities.%s.json", this.languageCode));
        return languageFile.exists() ? languageFile : new File(cacheDirectory, "cities.default.json");
    }

    @Override
    public void invalidateCache() {
        if(getMapCatalogFile().exists()){
            FileUtils.safeDelete(getMapCatalogFile());
        }
        if(getLocalizationFile().exists()){
            FileUtils.safeDelete(getLocalizationFile());
        }
    }

    private void downloadFile(String remoteFileName, String localFileName) throws ServiceUnavailableException, IOException {
        File fileToStore = new File(cacheDirectory, localFileName);
        if(!fileToStore.getParentFile().exists()){
            if(!fileToStore.getParentFile().mkdirs()){
                throw new IOException("Cannot create directory " + fileToStore.getParentFile());
            }
        }
        String content;
        try {
            content = serviceTransport.httpGet(serviceUrl.resolve(remoteFileName));
        } catch (Exception ex) {
            throw new ServiceUnavailableException(ex);
        }
        FileUtils.writeAllText(fileToStore, content);
    }
}