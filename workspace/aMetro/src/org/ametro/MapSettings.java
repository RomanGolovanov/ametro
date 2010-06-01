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

import org.ametro.jni.Natives;
import org.ametro.model.util.CountryLibrary;
import org.ametro.model.util.StationLibrary;
import org.ametro.util.FileUtil;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import static org.ametro.Constants.PREFERENCE_ONLINE_CATALOG_URL;

public class MapSettings {

    public static long getSourceVersion() {
        return 5;
    }

    public static final String DEFAULT_MAP = "metro";

    public static final String MAPS_LIST = "maps.dat";
    
    public static final String NO_MEDIA_TAG = ".nomedia";
    public static final String MAP_FILE_TYPE = ".ametro";
    public static final String PMZ_FILE_TYPE = ".pmz";
    public static final String TEMP_FILE_TYPE = ".tmp.ametro";
    
	private static final File ROOT_PATH = new File(Environment.getExternalStorageDirectory(), "ametro");
	private static final String DEFAULT_ONLINE_CATALOG_URL = "http://sites.google.com/site/ametroupdate/files";
	private static final File LOCAL_CATALOG_PATH = new File(ROOT_PATH, "maps");
	private static final File IMPORT_CATALOG_PATH = new File(ROOT_PATH, "import");
    
	private static final File ONLINE_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.online.xml");
	private static final File LOCAL_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.local.xml");
	private static final File IMPORT_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.import.xml");
	
    public static void checkPrerequisite( Context context ) {
    	Natives.Initialize();
        if (!ROOT_PATH.exists() || !LOCAL_CATALOG_PATH.exists() || !IMPORT_CATALOG_PATH.exists()) {
        	FileUtil.createDirectory(LOCAL_CATALOG_PATH);
        	FileUtil.createDirectory(IMPORT_CATALOG_PATH);
        	FileUtil.createFile(new File(ROOT_PATH, NO_MEDIA_TAG));
        }
        CountryLibrary.setContext(context);
        StationLibrary.setContext(context);
    }

    public static String getMapFileName(String mapName) {
        return new File(LOCAL_CATALOG_PATH, mapName + MAP_FILE_TYPE).getAbsolutePath().toLowerCase();
    }

    public static String getTemporaryMapFile(String mapName) {
        return new File(LOCAL_CATALOG_PATH, mapName + TEMP_FILE_TYPE).getAbsolutePath().toLowerCase();
    }

    public static String getMapFileName(Uri uri) {
        return getMapFileName(MapUri.getMapName(uri));
    }

    public static void refreshMapList() {
        FileUtil.delete(new File(ROOT_PATH, MAPS_LIST));
    }

	public static String getOnlineCatalogUrl(Context context) {
		return context.getSharedPreferences(Constants.PREFERENCE_NAME, 0).getString(PREFERENCE_ONLINE_CATALOG_URL, DEFAULT_ONLINE_CATALOG_URL);
	}

	public static File getLocalCatalog() {
		return LOCAL_CATALOG_PATH;
	}

	public static File getImportCatalog() {
		return IMPORT_CATALOG_PATH;
	}

	public static String getOnlineCatalogStorageUrl() {
		return ONLINE_CATALOG_STORAGE.getAbsolutePath();
	}

	public static File getLocalCatalogStorageUrl() {
		return LOCAL_CATALOG_STORAGE;
	}

	public static File getImportCatalogStorageUrl() {
		return IMPORT_CATALOG_STORAGE;
	}

    public static void refreshLocalCatalogStorage() {
        FileUtil.delete(LOCAL_CATALOG_STORAGE);
    }

	public static String getApplicationRoot() {
		return ROOT_PATH.getAbsolutePath().toLowerCase() + "/";
	}	
}
