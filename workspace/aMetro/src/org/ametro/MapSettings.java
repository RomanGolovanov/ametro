/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.ametro.model.SubwayMap;
import org.ametro.util.FileUtil;
import org.ametro.util.SerializeUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.net.Uri;
import android.preference.PreferenceManager;

public class MapSettings {

    public static long getSourceVersion() {
        return 5;
    }

    public static long getRenderVersion() {
        return 10;
    }

    public static final String PREFERENCE_PACKAGE_FILE_NAME = "PACKAGE_FILE_NAME";
    public static final String PREFERENCE_SCROLL_POSITION = "SCROLL_POSITION";
    public static final String PREFERENCE_ZOOM_LEVEL = "ZOOM_LEVEL";

    public static final String ROOT_PATH = "/sdcard/ametro/";
    public static final String MAPS_PATH = ROOT_PATH + "maps/";
    public static final String CACHE_PATH = ROOT_PATH + "cache/";
    public static final String IMPORT_PATH = ROOT_PATH + "import/";


    public static final String DEFAULT_MAP = "metro";

    public static final String MAPS_LIST = "maps.dat";
    public static final String NO_MEDIA_TAG = ".nomedia";

    public static final String MAP_FILE_TYPE = ".ametro";
    public static final String PMZ_FILE_TYPE = ".pmz";
    public static final String TEMP_FILE_TYPE = ".tmp";
    public static final String CACHE_FILE_TYPE = ".zip";
    public static final String MAP_ENTRY_NAME = "map.dat";
    public static final String DESCRIPTION_ENTRY_NAME = "description.dat";

    private static SubwayMap mCurrentSubwayMap;
    private static String mMapName;

    public static void setModel(SubwayMap subwayMap) {
        mCurrentSubwayMap = subwayMap;
    }

    public static SubwayMap getModel() {
        return mCurrentSubwayMap;
    }

    public static String getMapName() {
        return mMapName;
    }

    public static void checkPrerequisite(Context context) {
        File root = new File(ROOT_PATH);
        File maps = new File(MAPS_PATH);
        File cache = new File(CACHE_PATH);
        if (!root.exists() || !maps.exists() || !cache.exists()) {
            //context.startActivity(new Intent(context,CreatePrerequisites.class));

            createDirectory(MAPS_PATH);
            createDirectory(IMPORT_PATH);
            createDirectory(CACHE_PATH);
            createFile(ROOT_PATH + NO_MEDIA_TAG);

        }
    }

    private static void createFile(String path) {
        try {
            File f = new File(path);
            f.createNewFile();
        } catch (IOException e) {
            // scoop exception
        }
    }

    private static void createDirectory(String path) {
        File f = new File(path);
        f.mkdirs();
    }

    public static String getMapFileName(String mapName) {
        return (MAPS_PATH + mapName + MAP_FILE_TYPE).toLowerCase();
    }

    public static String getTemporaryMapFile(String mapName) {
        return (MAPS_PATH + mapName + TEMP_FILE_TYPE).toLowerCase();
    }

    public static String getTemporaryCacheFile(String mapName) {
        return (CACHE_PATH + mapName + TEMP_FILE_TYPE).toLowerCase();
    }

    public static String getCacheFileName(String mapName) {
        return (CACHE_PATH + mapName + CACHE_FILE_TYPE).toLowerCase();
    }

    public static void loadDefaultMapName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
        mMapName = preferences.getString(PREFERENCE_PACKAGE_FILE_NAME, null);
    }

    public static void saveDefaultMapName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_PACKAGE_FILE_NAME, mMapName);
        editor.commit();
    }

    public static void clearDefaultMapName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(PREFERENCE_PACKAGE_FILE_NAME);
        editor.commit();
        mMapName = null;
    }

    public static void setMapName(String mapName) {
        mMapName = mapName;
    }

    public static void saveScrollPosition(Context context, PointF position) {
        SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
        SharedPreferences.Editor editor = preferences.edit();
        String scrollPosition = "" + position.x + "," + position.y;
        editor.putString(PREFERENCE_SCROLL_POSITION + "_" + MapSettings.getMapName(), scrollPosition);
        editor.commit();

    }

    public static PointF loadScrollPosition(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
        String pref = preferences.getString(PREFERENCE_SCROLL_POSITION + "_" + mMapName, null);
        if (pref != null) {
            return SerializeUtil.parsePointF(pref);
        } else {
            return null;
        }
    }

    public static void clearScrollPosition(Context context, String mapName) {
        if (mapName != null) {
            SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(PREFERENCE_SCROLL_POSITION + "_" + mapName);
            editor.commit();
        }
    }

    public static void saveZoom(Context context, int zoomLevel) {
        SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_ZOOM_LEVEL + "_" + MapSettings.getMapName(), Integer.toString(zoomLevel));
        editor.commit();

    }

    public static Integer loadZoom(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
        String pref = preferences.getString(PREFERENCE_ZOOM_LEVEL + "_" + mMapName, null);
        if (pref != null) {
            return SerializeUtil.parseNullableInteger(pref);
        } else {
            return null;
        }
    }

    public static void clearZoom(Context context, String mapName) {
        if (mapName != null) {
            SharedPreferences preferences = context.getSharedPreferences("aMetro", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(PREFERENCE_ZOOM_LEVEL + "_" + mapName);
            editor.commit();
        }
    }


    public static String getMapFileName(Uri uri) {
        return getMapFileName(MapUri.getMapName(uri));
    }

    public static void refreshMapList() {
        FileUtil.delete(new File(ROOT_PATH + MAPS_LIST));
    }

    public static Locale getLocale(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		final String localeName = settings.getString(context.getString(R.string.pref_locale_key), null);
		return localeName!=null && localeName.length()>0 ? new Locale(localeName) : Locale.ENGLISH;
    }
    
	public static void setupLocale(Context context) {
   		Locale.setDefault(getLocale(context));
	}

	public static boolean isEnabledAddonsImport(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(context.getString(R.string.pref_auto_import_addons_key), false);
	}

	public static boolean isConfigurationChanged(Context context) {
		Locale currentLocale = Locale.getDefault();
		Locale newLocale = getLocale(context);
		if(!currentLocale.equals(newLocale)){
			return true;
		}
		return false;
	}
}
