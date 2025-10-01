package io.github.romangolovanov.apps.ametro.catalog;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;

import io.github.romangolovanov.apps.ametro.app.Constants;
import io.github.romangolovanov.apps.ametro.catalog.entities.MapCatalog;
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo;
import io.github.romangolovanov.apps.ametro.model.serialization.FileAssetsMapProvider;
import io.github.romangolovanov.apps.ametro.model.serialization.GlobalIdentifierProvider;
import io.github.romangolovanov.apps.ametro.model.serialization.MapProvider;
import io.github.romangolovanov.apps.ametro.model.serialization.ZipArchiveMapProvider;
import io.github.romangolovanov.apps.ametro.utils.FileUtils;

public class MapCatalogProvider {

    private final Context context;
    private final MapInfoLocalizationProvider localizationProvider;
    private MapCatalog catalog;
    private final GlobalIdentifierProvider identifierProvider = new GlobalIdentifierProvider();

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

    public MapProvider getMapProvider(MapInfo mapInfo) throws IOException {
        String fileAssetsPath = "map_files/" + mapInfo.getFileName();
        var assets = context.getAssets();

        // 1. Try if it's a zip file directly in assets
        try {
            try (var is = assets.open(fileAssetsPath)) {
                return new ZipArchiveMapProvider(identifierProvider, is);
            }
        } catch (IOException ignored) {
            // not a zip, continue
        }

        // 2. Otherwise try as a folder (strip ".zip" suffix if present)
        var folderPath = fileAssetsPath.endsWith(".zip")
                ? fileAssetsPath.substring(0, fileAssetsPath.length() - 4)
                : fileAssetsPath;

        try {
            String[] entries = assets.list(folderPath);
            if (entries != null && entries.length > 0) {
                return new FileAssetsMapProvider(identifierProvider, assets, folderPath);
            }
        } catch (IOException ignored) {
            // continue to error
        }

        throw new IOException("Map " + mapInfo.getFileName() + " assets not found");
    }
}
