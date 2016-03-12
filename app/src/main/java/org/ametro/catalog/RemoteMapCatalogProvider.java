package org.ametro.catalog;

import android.util.Log;

import org.ametro.app.Constants;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.catalog.localization.MapInfoLocalizationProvider;
import org.ametro.catalog.serialization.MapCatalogSerializer;
import org.ametro.catalog.service.IMapServiceCache;
import org.ametro.utils.FileUtils;

import java.net.URI;

public class RemoteMapCatalogProvider {

    private final IMapServiceCache cache;
    private final MapInfoLocalizationProvider localizationProvider;
    private final URI serviceUri;

    public RemoteMapCatalogProvider(URI serviceUri, IMapServiceCache cache, MapInfoLocalizationProvider localizationProvider)
    {
        this.serviceUri = serviceUri;
        this.cache = cache;
        this.localizationProvider = localizationProvider;
    }

    public MapCatalog getMapCatalog(boolean forceUpdate)
    {
        if(!forceUpdate && cache.hasValidCache()){
            return loadCatalog();
        }
        try {
            cache.refreshCache();
        } catch (Exception ex) {
            Log.e(Constants.LOG, String.format("Cannot refresh remote map catalog cache due exception: %s", ex.toString()));
        }
        return loadCatalog();
    }


    private MapCatalog loadCatalog() {
        try{
            return localizationProvider.createCatalog(
                    MapCatalogSerializer.deserializeMapInfoArray(
                            FileUtils.readAllText(cache.getMapCatalogFile())));
        }catch(Exception ex)
        {
            Log.e(Constants.LOG, String.format("Cannot read remote map catalog cache due exception: %s", ex.toString()));
            cache.invalidateCache();
            return null;
        }
    }

    public URI getMapFileUrl(MapInfo map) {
        return serviceUri.resolve(map.getFileName());
    }
}
