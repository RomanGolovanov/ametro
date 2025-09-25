package org.ametro.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.ametro.catalog.MapCatalogManager;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;

import java.util.Locale;

public class ApplicationSettingsProvider {

    private static final String PREFS_NAME = "aMetroPreferences";

    private static final String SELECTED_MAP = "selectedMap";
    private static final String PREFERRED_LANGUAGE = "preferredLanguage";

    private final SharedPreferences settings;
    private final MapCatalogManager catalog;


    public ApplicationSettingsProvider(Context context, MapCatalogManager catalog) {
        this.settings = context.getSharedPreferences(PREFS_NAME, 0);
        this.catalog = catalog;
    }

    /**
     * Returns the currently selected map, or null if none is set or not found in the catalog.
     */
    public MapInfo getCurrentMap() {
        var mapFileName = settings.getString(SELECTED_MAP, null);
        if (mapFileName == null) {
            return null;
        }
        MapInfo map = catalog.findMapByName(mapFileName);
        if (map == null) {
            setCurrentMap(null);
            return null;
        }
        return map;
    }

    /**
     * Save the currently selected map (by file name).
     */
    public void setCurrentMap(MapInfo map) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SELECTED_MAP, map != null ? map.getFileName() : null);
        editor.apply();
    }

    public String getDefaultLanguage() {
        return Locale.getDefault().getLanguage().toLowerCase();
    }

    public String getPreferredMapLanguage() {
        String languageCode = settings.getString(PREFERRED_LANGUAGE, null);
        return languageCode != null ? languageCode : getDefaultLanguage();
    }
}
