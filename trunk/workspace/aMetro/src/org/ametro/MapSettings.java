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

import org.ametro.util.FileUtil;

import android.net.Uri;

public class MapSettings {

    public static long getSourceVersion() {
        return 5;
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
    
    public static void checkPrerequisite() {
        File root = new File(ROOT_PATH);
        File maps = new File(MAPS_PATH);
        File cache = new File(CACHE_PATH);
        if (!root.exists() || !maps.exists() || !cache.exists()) {
        	FileUtil.createDirectory(MAPS_PATH);
        	FileUtil.createDirectory(IMPORT_PATH);
        	FileUtil.createDirectory(CACHE_PATH);
        	FileUtil.createFile(ROOT_PATH + NO_MEDIA_TAG);
        }
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

    public static String getMapFileName(Uri uri) {
        return getMapFileName(MapUri.getMapName(uri));
    }

    public static void refreshMapList() {
        FileUtil.delete(new File(ROOT_PATH + MAPS_LIST));
    }

}
