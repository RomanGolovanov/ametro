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

import static org.ametro.Constants.PREFERENCE_AUTO_UPDATE_INDEX;
import static org.ametro.Constants.PREFERENCE_AUTO_UPDATE_MAPS;
import static org.ametro.Constants.PREFERENCE_DEBUG;
import static org.ametro.Constants.PREFERENCE_LOCALE;
import static org.ametro.Constants.PREFERENCE_ONLINE_CATALOG_URL;

import java.io.File;
import java.util.Locale;

import org.ametro.jni.Natives;
import org.ametro.model.TransportType;
import org.ametro.model.util.CountryLibrary;
import org.ametro.model.util.StationLibrary;
import org.ametro.util.FileUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

public class GlobalSettings {

    public static long getSourceVersion() {
        return 5;
    }

    public static final String DEFAULT_MAP = "metro";

    public static final String MAPS_LIST = "maps.dat";
    
    public static final String NO_MEDIA_TAG = ".nomedia";
    public static final String MAP_FILE_TYPE = ".ametro";
    public static final String PMZ_FILE_TYPE = ".pmz";

    public static final String IMPORT_FILE_TYPE = ".import.ametro";
    public static final String DOWNLOAD_FILE_TYPE = ".download.ametro";
    
	private static final File ROOT_PATH = new File(Environment.getExternalStorageDirectory(), "ametro");
	
	private static final String CATALOG_VERSION = "0.2.4.r247";
	
	private static final String ONLINE_CATALOG_URL_TEMPLATE = "http://dl.dropbox.com/u/8171021/%s/catalog.xml";
	private static final File LOCAL_CATALOG_PATH = new File(ROOT_PATH, "maps");
	private static final File IMPORT_CATALOG_PATH = new File(ROOT_PATH, "import");
	private static final File TEMP_CATALOG_PATH = new File(ROOT_PATH,"temp");
    
	private static final File ONLINE_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.online.xml");
	private static final File LOCAL_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.local.xml");
	private static final File IMPORT_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.import.xml");
	
	private static Context mContext;
	private static String mDefaultLocale;
	
    public static void initialize( Context context ) {
    	mContext = context;
    	mDefaultLocale = Locale.getDefault().getLanguage();
    	Natives.Initialize();
    	FileUtil.touchDirectory(ROOT_PATH);
    	FileUtil.touchDirectory(LOCAL_CATALOG_PATH);
    	FileUtil.touchDirectory(IMPORT_CATALOG_PATH);
    	FileUtil.touchDirectory(TEMP_CATALOG_PATH);
    	FileUtil.touchFile(new File(ROOT_PATH, NO_MEDIA_TAG));
        CountryLibrary.setContext(context);
        StationLibrary.setContext(context);
    }

	public static String getLanguage(){
		String code = PreferenceManager.getDefaultSharedPreferences(mContext).getString(PREFERENCE_LOCALE, "auto");
		if("auto".equalsIgnoreCase(code)){
			return mDefaultLocale;
		}else{
			return code;
		}
	}
	
    public static String getMapFileName(String mapName) {
        return new File(LOCAL_CATALOG_PATH, mapName + MAP_FILE_TYPE).getAbsolutePath().toLowerCase();
    }

    public static String getTemporaryImportMapFile(String mapName) {
        return new File(TEMP_CATALOG_PATH, mapName + IMPORT_FILE_TYPE).getAbsolutePath().toLowerCase();
    }

    public static String getTemporaryDownloadMapFile(String mapName) {
        return new File(TEMP_CATALOG_PATH, mapName + DOWNLOAD_FILE_TYPE).getAbsolutePath().toLowerCase();
    }

    public static String getMapFileName(Uri uri) {
        return getMapFileName(MapUri.getMapName(uri));
    }

    public static void refreshMapList() {
        FileUtil.delete(new File(ROOT_PATH, MAPS_LIST));
    }

	public static String getOnlineCatalogUrl() {
		//return PreferenceManager.getDefaultSharedPreferences(mContext).getString(PREFERENCE_ONLINE_CATALOG_URL, DEFAULT_ONLINE_CATALOG_URL);
		return String.format(ONLINE_CATALOG_URL_TEMPLATE, CATALOG_VERSION);
	}

	public static boolean isDebugMessagesEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PREFERENCE_DEBUG, false);
	}

	public static boolean isAutoUpdateIndexEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PREFERENCE_AUTO_UPDATE_INDEX, false);
	}

	public static boolean isAutoUpdateMapsEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PREFERENCE_AUTO_UPDATE_MAPS, false);
	}
	
	public static void setOnlineCatalogUrl(String url) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = prefs.edit();
		editor.putString(PREFERENCE_ONLINE_CATALOG_URL, url);
		editor.commit();
	}
	
	public static void invalidate() {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//		String currentValue = prefs.getString(PREFERENCE_ONLINE_CATALOG_URL, null);
//		if(StringUtil.isNullOrEmpty(currentValue)){
//			Editor editor = prefs.edit();
//			editor.putString(PREFERENCE_ONLINE_CATALOG_URL, DEFAULT_ONLINE_CATALOG_URL);
//			editor.commit();
//		}
	}	
	
	public static File getLocalCatalog() {
		return LOCAL_CATALOG_PATH;
	}

	public static File getImportCatalog() {
		return IMPORT_CATALOG_PATH;
	}

	public static File getOnlineCatalogStorageUrl() {
		return ONLINE_CATALOG_STORAGE;
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
	
	public static int getTransportTypeBlackIconId(int transportTypeId){
		switch(transportTypeId){
			case TransportType.METRO_ID : return R.drawable.icon_b_metro;
			case TransportType.TRAM_ID : return R.drawable.icon_b_tram;
			case TransportType.BUS_ID : return R.drawable.icon_b_bus;
			case TransportType.TRAIN_ID : return R.drawable.icon_b_train;
			case TransportType.WATER_BUS_ID : return R.drawable.icon_b_water_bus;
			case TransportType.TROLLEYBUS_ID : return R.drawable.icon_b_trolleybus;		
		}
		return R.drawable.icon_b_unknown;
	}	
	
	public static int getTransportTypeWhiteIconId(int transportTypeId){
		switch(transportTypeId){
			case TransportType.METRO_ID : return R.drawable.icon_w_metro;
			case TransportType.TRAM_ID : return R.drawable.icon_w_tram;
			case TransportType.BUS_ID : return R.drawable.icon_w_bus;
			case TransportType.TRAIN_ID : return R.drawable.icon_w_train;
			case TransportType.WATER_BUS_ID : return R.drawable.icon_w_water_bus;
			case TransportType.TROLLEYBUS_ID : return R.drawable.icon_w_trolleybus;		
		}
		return R.drawable.icon_w_unknown;
	}		
}
