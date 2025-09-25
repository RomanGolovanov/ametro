package org.ametro.catalog.localization;

import android.content.Context;

import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.catalog.entities.MapInfoEntity;
import org.ametro.catalog.entities.MapInfoEntityName;
import org.ametro.catalog.serialization.MapCatalogSerializer;
import org.ametro.utils.FileUtils;

import java.io.InputStream;
import java.util.HashMap;

public class MapInfoLocalizationProvider {

    private final Context context;
    private final String languageCode;
    private HashMap<Integer, MapInfoEntityName> localizationMap;

    public MapInfoLocalizationProvider(Context context, String languageCode) {
        this.context = context;
        this.languageCode = languageCode;
    }

    public String getCityName(int cityId) {
        return getLocalizationMap().get(cityId).getCityName();
    }

    public String getCountryName(int cityId) {
        return getLocalizationMap().get(cityId).getCountryName();
    }

    public String getCountryIsoCode(int cityId) {
        return getLocalizationMap().get(cityId).getCountryIsoCode();
    }

    public MapCatalog createCatalog(MapInfoEntity[] maps) {
        HashMap<Integer, MapInfoEntityName> localizations = getLocalizationMap();
        MapInfo[] localizedMaps = new MapInfo[maps.length];
        for (int i = 0; i < maps.length; i++) {
            MapInfoEntityName loc = localizations.get(maps[i].getCityId());
            localizedMaps[i] = new MapInfo(
                    maps[i],
                    loc != null ? loc.getCityName() : "Unknown",
                    loc != null ? loc.getCountryName() : "Unknown",
                    loc != null ? loc.getCountryIsoCode() : ""
            );
        }
        return new MapCatalog(localizedMaps);
    }

    private HashMap<Integer, MapInfoEntityName> getLocalizationMap() {
        if (localizationMap != null) {
            return localizationMap;
        }

        try {
            String fileName = String.format("map_files/locales/cities.%s.json", languageCode);
            String json;
            try (InputStream is = context.getAssets().open(fileName)) {
                json = FileUtils.readAllText(is);
            } catch (Exception e) {
                try (InputStream is = context.getAssets().open("map_files/cities.default.json")) {
                    json = FileUtils.readAllText(is);
                }
            }

            MapInfoEntityName[] entities = MapCatalogSerializer.deserializeLocalization(json);
            HashMap<Integer, MapInfoEntityName> map = new HashMap<>();
            for (MapInfoEntityName entity : entities) {
                map.put(entity.getCityId(), entity);
            }
            localizationMap = map;
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Localization cannot be read", e);
        }
    }
}
