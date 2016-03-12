package org.ametro.app;


import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Locale;

public class ApplicationSettingsProvider {

    private static final String PREFS_NAME = "aMetroPreferences";

    private static final String SELECTED_MAP = "selectedMap";
    private static final String PREFERRED_LANGUAGE = "preferredLanguage";

    private final SharedPreferences settings;

    public ApplicationSettingsProvider(Context context){
        settings = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public File getCurrentMap() {
        String mapFilePath = settings.getString(SELECTED_MAP, null);
        if(mapFilePath==null){
            return null;
        }
        File mapFile = new File(mapFilePath);
        if(!mapFile.exists()){
            setCurrentMap(null);
            return null;
        }
        return mapFile;
    }

    public void setCurrentMap(File mapFile){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SELECTED_MAP, mapFile!=null ? mapFile.getAbsolutePath() : null);
        editor.apply();
    }

    public String getDefaultLanguage(){
        return Locale.getDefault().getLanguage().toLowerCase();
    }

    public String getPreferredMapLanguage(){
        String languageCode = settings.getString(PREFERRED_LANGUAGE, null);
        return languageCode!=null ? languageCode : getDefaultLanguage();
    }
}
