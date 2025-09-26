package org.ametro.catalog;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.ametro.app.Constants;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;

public class MapCatalogProvider {

    private final Context context;
    private final MapInfoLocalizationProvider localizationProvider;
    private MapCatalog catalog;

    public MapCatalogProvider(Context context, MapInfoLocalizationProvider localizationProvider) {
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
        for (var map : getMapCatalog().getMaps()) {
            if (map.getFileName().equals(mapFileName)) {
                return map;
            }
        }
        throw new Resources.NotFoundException("Not found map file " + mapFileName);
    }

    private MapCatalog loadCatalog() {
        try (var is = context.getAssets().open("map_files/index.json")) {
            var json = FileUtils.readAllText(is);

            var maps = MapCatalogSerializer.deserializeMapInfoArray(json);
            var localizations = localizationProvider.getLocalizationMap();
            var localizedMaps = new MapInfo[maps.length];
            for (var i = 0; i < maps.length; i++) {
                var loc = localizations.get(maps[i].getCityId());
                localizedMaps[i] = new MapInfo(
                        maps[i],
                        loc != null ? loc.getCityName() : "Unknown",
                        loc != null ? loc.getCountryName() : "Unknown",
                        loc != null ? loc.getCountryIsoCode() : ""
                );
            }
            return new MapCatalog(localizedMaps);

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
