package org.ametro.catalog;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.ametro.app.Constants;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.catalog.localization.MapInfoLocalizationProvider;
import org.ametro.catalog.serialization.MapCatalogSerializer;
import org.ametro.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MapCatalogManager {

    private final Context context;
    private final MapInfoLocalizationProvider localizationProvider;
    private MapCatalog catalog;

    public MapCatalogManager(Context context, MapInfoLocalizationProvider localizationProvider) {
        this.context = context;
        this.localizationProvider = localizationProvider;
    }

    public MapCatalog getMapCatalog() {
        if (catalog != null) {
            return catalog;
        }
        return catalog = loadCatalog();
    }

    public MapInfo findMapByName(String mapFileName) {
        for (MapInfo map : getMapCatalog().getMaps()) {
            if (map.getFileName().equals(mapFileName)) {
                return map;
            }
        }
        throw new Resources.NotFoundException("Not found map file " + mapFileName);
    }

    private MapCatalog loadCatalog() {
        try (InputStream is = context.getAssets().open("map_files/index.json")) {
            String json = FileUtils.readAllText(is);
            return localizationProvider.createCatalog(
                    MapCatalogSerializer.deserializeMapInfoArray(json));
        } catch (Exception ex) {
            Log.e(Constants.LOG, "Cannot read map catalog from assets", ex);
            return new MapCatalog(new MapInfo[0]);
        }
    }

    public InputStream openMapAssetStream(MapInfo map)  {
        try {
            return context.getAssets().open("map_files/" + map.getFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
