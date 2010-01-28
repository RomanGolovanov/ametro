package com.ametro;

import java.io.File;

import com.ametro.activity.CreatePrerequisites;
import com.ametro.libs.Helpers;
import com.ametro.model.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;

public class MapSettings {

	public static final String PREFERENCE_PACKAGE_FILE_NAME = "PACKAGE_FILE_NAME";
	public static final String PREFERENCE_SCROLL_POSITION = "SCROLL_POSITION";
	
	public static final String ROOT_PATH = "/sdcard/ametro/";
	public static final String MAPS_PATH = ROOT_PATH + "maps/";
	public static final String CACHE_PATH = ROOT_PATH + "cache/";
	public static final String IMPORT_PATH = ROOT_PATH + "import/";

	public static final int TILE_WIDTH = 100;
	public static final int TILE_HEIGHT = 100;

	public static final String DEFAULT_MAP = "metro";
	
	public static final String CACHE_DESCRIPTION = ".description";
	public static final String MAPS_LIST = ".maps";
	public static final String NO_MEDIA_TAG = ".nomedia";
	
	public static final String MAP_FILE_TYPE = ".ametro";
	public static final String PMZ_FILE_TYPE = ".pmz";
	public static final String CACHE_FILE_TYPE = ".zip";
	public static final String MAP_ENTRY_NAME = "map.dat";
	public static final String DESCRIPTION_ENTRY_NAME = "description.txt";
	
	private static Model mCurrentModel;
	private static String mMapName;

	public static void setModel(Model model){
		mCurrentModel = model;
	}
	
	public static Model getModel(){
		return mCurrentModel;
	}
	
	public static String getMapName(){
		return mMapName;
	}
	
	public static void checkPrerequisite(Context context){
		File root = new File(ROOT_PATH);
		File maps = new File(MAPS_PATH);
		File cache = new File(CACHE_PATH);
		if( !root.exists() || !maps.exists() || !cache.exists() ){
			context.startActivity(new Intent(context,CreatePrerequisites.class));
		}
	}
	
	public static String getMapFileName(String mapName) {
		return (MapSettings.MAPS_PATH + mapName + MAP_FILE_TYPE).toLowerCase();
	}

	public static String getCacheFileName(String mapName){
		return (MapSettings.CACHE_PATH + mapName + CACHE_FILE_TYPE).toLowerCase();
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

	public static void saveScrollPosition(Context context, Point position){
		SharedPreferences preferences = context.getSharedPreferences("aMetro",0);
		SharedPreferences.Editor editor = preferences.edit();
		String scrollPosition =  "" + position.x + "," + position.y;
		editor.putString(PREFERENCE_SCROLL_POSITION + "_" + MapSettings.getMapName(), scrollPosition  );
		editor.commit();
		
	}
	
	public static Point loadScrollPosition(Context context) {
		SharedPreferences preferences = context.getSharedPreferences("aMetro",0);
		String pref = preferences.getString(PREFERENCE_SCROLL_POSITION + "_" + mMapName, null);
		if(pref!=null){
			return Helpers.parsePoint(pref);
		}else{
			return null;
		}
	}	
	
	public static void clearScrollPosition(Context context, String mapName) {
		if(mapName!=null){
			SharedPreferences preferences = context.getSharedPreferences("aMetro",0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.remove(PREFERENCE_SCROLL_POSITION + "_" + mapName);
			editor.commit();
		}
	}
	
}
