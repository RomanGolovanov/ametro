package io.github.romangolovanov.apps.ametro.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class ApplicationSettingsProvider {

    private static final String PREFS_NAME = "aMetroPreferences";
    private static final String SELECTED_MAP = "selectedMap";
    private static final String PREFERRED_LANGUAGE = "preferredLanguage";

    private final SharedPreferences settings;

    public ApplicationSettingsProvider(Context context) {
        this.settings = context.getSharedPreferences(PREFS_NAME, 0);
    }

    /**
     * Returns the currently selected map, or null if none is set or not found in the catalog.
     */
    public String getCurrentMapFileName() {
        return settings.getString(SELECTED_MAP, null);
    }

    /**
     * Save the currently selected map (by file name).
     */
    public void setCurrentMap(String mapFileName) {
        var editor = settings.edit();
        editor.putString(SELECTED_MAP, mapFileName);
        editor.apply();
    }

    public String getDefaultLanguage() {
        return Locale.getDefault().getLanguage().toLowerCase();
    }

    public String getPreferredMapLanguage() {
        var languageCode = settings.getString(PREFERRED_LANGUAGE, null);
        return languageCode != null ? languageCode : getDefaultLanguage();
    }
}
