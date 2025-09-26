package org.ametro.catalog;

import android.content.Context;

import org.ametro.app.ApplicationSettingsProvider;
import org.ametro.catalog.entities.MapInfoEntityName;
import org.ametro.utils.FileUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class MapInfoLocalizationProvider {

    private final Context context;
    private final ApplicationSettingsProvider applicationSettingsProvider;
    private HashMap<Integer, MapInfoEntityName> localizationMap;

    public MapInfoLocalizationProvider(Context context, ApplicationSettingsProvider applicationSettingsProvider) {
        this.context = context;
        this.applicationSettingsProvider = applicationSettingsProvider;
    }
    public String getCityName( int cityId) {
        return Objects.requireNonNull(getLocalizationMap().get(cityId)).getCityName();
    }

    public String getCountryName( int cityId) {
        return Objects.requireNonNull(getLocalizationMap().get(cityId)).getCountryName();
    }

    public String getCountryIsoCode(int cityId) {
        return Objects.requireNonNull(getLocalizationMap().get(cityId)).getCountryIsoCode();
    }

    public HashMap<Integer, MapInfoEntityName> getLocalizationMap() {
        if (localizationMap != null) {
            return localizationMap;
        }

        try {
            String fileName = String.format("map_files/locales/cities.%s.json", applicationSettingsProvider.getPreferredMapLanguage());
            String json;
            try (InputStream is = context.getAssets().open(fileName)) {
                json = FileUtils.readAllText(is);
            } catch (Exception e) {
                try (InputStream is = context.getAssets().open("map_files/locales/cities.default.json")) {
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
