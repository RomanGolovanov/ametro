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

import static org.ametro.Constants.IMPORT_FILE_TYPE;
import static org.ametro.Constants.DOWNLOAD_FILE_TYPE;
import static org.ametro.Constants.LOCAL_CATALOG_PATH;
import static org.ametro.Constants.MAP_FILE_TYPE;
import static org.ametro.Constants.PREFERENCE_AUTO_UPDATE_INDEX;
import static org.ametro.Constants.PREFERENCE_AUTO_UPDATE_MAPS;
import static org.ametro.Constants.PREFERENCE_DEBUG;
import static org.ametro.Constants.PREFERENCE_LOCALE;
import static org.ametro.Constants.TEMP_CATALOG_PATH;

import java.io.File;
import java.util.Locale;

import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.model.TransportType;

import android.content.Context;
import android.preference.PreferenceManager;

public class GlobalSettings {

	private static String mDefaultLocale = Locale.getDefault().getLanguage();
	
	
    public static String getObsoleteLocalCatalogMapFileName(String mapName) {
        return new File(LOCAL_CATALOG_PATH, mapName + MAP_FILE_TYPE).getAbsolutePath().toLowerCase();
    }
    
    public static String getObsoleteTemporaryImportMapFile(String mapName) {
        return new File(TEMP_CATALOG_PATH, mapName + IMPORT_FILE_TYPE).getAbsolutePath().toLowerCase();
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

	public static boolean isDebugMessagesEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_DEBUG, false);
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
