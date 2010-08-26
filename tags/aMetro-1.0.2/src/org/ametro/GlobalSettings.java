/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro;

import static org.ametro.Constants.DOWNLOAD_FILE_TYPE;
import static org.ametro.Constants.IMPORT_FILE_TYPE;
import static org.ametro.Constants.LOCAL_CATALOG_PATH;
import static org.ametro.Constants.MAP_FILE_TYPE;
import static org.ametro.Constants.PREFERENCE_AUTO_UPDATE_INDEX;
import static org.ametro.Constants.PREFERENCE_AUTO_UPDATE_MAPS;
import static org.ametro.Constants.PREFERENCE_AUTO_UPDATE_ON_SHOW;
import static org.ametro.Constants.PREFERENCE_DEBUG;
import static org.ametro.Constants.PREFERENCE_ENABLE_COUNTRY_ICONS;
import static org.ametro.Constants.PREFERENCE_ENABLE_LOCATION;
import static org.ametro.Constants.PREFERENCE_IS_EULA_ACCEPTED;
import static org.ametro.Constants.PREFERENCE_LOCALE;
import static org.ametro.Constants.PREFERENCE_ONLINE_CATALOG_UPDATE_DATE;
import static org.ametro.Constants.PREFERENCE_PACKAGE_FILE_NAME;
import static org.ametro.Constants.PREFERENCE_CHANGE_LOW_SHOWED;
import static org.ametro.Constants.PREFERENCE_PMZ_IMPORT;
import static org.ametro.Constants.TEMP_CATALOG_PATH;

import java.io.File;
import java.util.Locale;

import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.util.StringUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class GlobalSettings {

	public static class MapPath{
		public String FilePath;
		public String ViewName;
		
		public String getSystemMapName() {
			return FilePath==null ? null : FilePath.substring(FilePath.lastIndexOf('/')+1);
		}
		
		public MapPath(String path){
			if(path!=null){
				try{
					String[] parts = StringUtil.parseStringArray(path);
					FilePath = parts[0];
					ViewName = parts[1];
				}catch(Throwable e){
				}
			}
		}
	}
	
	private static String mDefaultLocale = Locale.getDefault().getLanguage();

	public static void clearCurrentMap(Context context){
		SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove(PREFERENCE_PACKAGE_FILE_NAME);
		editor.commit();
	}
	
	public static void setCurrentMap(Context context, String file, String view){
		SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_PACKAGE_FILE_NAME, file + "," + view);
		editor.commit();
	}

	public static MapPath getCurrentMap(Context context){
		SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		return new MapPath(preferences.getString(PREFERENCE_PACKAGE_FILE_NAME, null));
	}
	
	public static String getLanguage(Context context){
		String code = PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_LOCALE, "auto");
		if("auto".equalsIgnoreCase(code)){
			return mDefaultLocale;
		}else{
			return code;
		}
	}
	
    public static String getLocalCatalogMapFileName(String systemName) {
        return new File(LOCAL_CATALOG_PATH, systemName).getAbsolutePath().toLowerCase();
    }

    public static String getTemporaryImportMapFile(String systemName) {
        return new File(TEMP_CATALOG_PATH, systemName.replace(MAP_FILE_TYPE, IMPORT_FILE_TYPE)).getAbsolutePath().toLowerCase();
    }

    public static String getTemporaryDownloadMapFile(String systemName) {
        return new File(TEMP_CATALOG_PATH, systemName.replace(MAP_FILE_TYPE, DOWNLOAD_FILE_TYPE)).getAbsolutePath().toLowerCase();
    }

	public static boolean isImportEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_PMZ_IMPORT, false);
	}

	public static boolean isCountryIconsEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_ENABLE_COUNTRY_ICONS, true);
	}

	public static boolean isLocateUserEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_ENABLE_LOCATION, false);
	}

	public static void setCountryIconsEnabled(Context context, boolean enabled) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean(PREFERENCE_ENABLE_COUNTRY_ICONS, enabled);
		editor.commit();
	}

	public static long getUpdateDate(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFERENCE_ONLINE_CATALOG_UPDATE_DATE, 0);
	}

	public static void setUpdateDate(Context context, long timestamp) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putLong(PREFERENCE_ONLINE_CATALOG_UPDATE_DATE, timestamp);
		editor.commit();
	}
	
	public static boolean isAcceptedEULA(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_IS_EULA_ACCEPTED, false);
	}
	
	public static void setAcceptedEULA(Context context, boolean accepted) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean(PREFERENCE_IS_EULA_ACCEPTED, accepted);
		editor.commit();
	}

	public static boolean isChangeLogShowed(Context context) {
		try{
			PackageManager manager = context.getPackageManager();
			PackageInfo info;
			info = manager.getPackageInfo(context.getPackageName(), 0);
			String versionName = info.versionName;
			return versionName.equals( PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_CHANGE_LOW_SHOWED, null) );
		}catch(Exception ex)
		{
			
		}
		return true;
	}
	
	public static void setChangeLogShowed(Context context) {
		try{
			PackageManager manager = context.getPackageManager();
			PackageInfo info;
			info = manager.getPackageInfo(context.getPackageName(), 0);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putString(PREFERENCE_CHANGE_LOW_SHOWED, info.versionName);
			editor.commit();
		}catch(Exception ex)
		{
			
		}
	}	
	
	public static long getUpdatePeriod(Context context) {
		String value = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_auto_update_period_key), null);
		if("weekly".equalsIgnoreCase(value)){
			return 604800;
		}
		if("monthly".equalsIgnoreCase(value)){
			return 2592000; 
		}
		if("debug".equalsIgnoreCase(value)){
			return 0;
		}
		return 86400;
	}		

	public static boolean isUpdateOnlyByWifi(Context context) {
		String value = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_auto_update_networks_key), null);
		if("any".equalsIgnoreCase(value)){
			return false;
		}
		return true;
	}		
		
	public static boolean isDebugMessagesEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_DEBUG, false);
	}

	public static boolean isAutoUpdateIndexEveryHourEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_AUTO_UPDATE_ON_SHOW, false);
	}

	public static boolean isAutoUpdateIndexEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_AUTO_UPDATE_INDEX, false);
	}

	public static boolean isAutoUpdateMapsEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_AUTO_UPDATE_MAPS, false);
	}

    public static void refreshCatalogStorage() {
    	ApplicationEx.getInstance().getCatalogStorage().requestCatalog(CatalogStorage.LOCAL, true);
    	ApplicationEx.getInstance().getCatalogStorage().requestCatalog(CatalogStorage.IMPORT, true);
    }

	public static File getTemporaryDownloadIconFile() {
		return new File(Constants.TEMP_CATALOG_PATH, "icons.zip");
	}

}
