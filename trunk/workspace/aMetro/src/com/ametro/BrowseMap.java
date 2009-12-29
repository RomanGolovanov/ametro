package com.ametro;


import com.ametro.model.Model;
import com.ametro.model.ModelBuilder;
import com.ametro.widgets.MapImageView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;

public class BrowseMap extends Activity {

	private Model mModel; 
	private MapImageView mMapImageView;

	private String mPackageFileName;
	private String mPackageMapName;
	private int mTimeOfDay = 0;

	private Integer mSelectedStationId = null;

	private MenuItem mMainMenuTime;
	private MenuItem mMainMenuStation;

	private static final String PREFERENCE_PACKAGE_FILE_NAME = "PACKAGE_FILE_NAME";
	private static final String PREFERENCE_PACKAGE_MAP_NAME = "PACKAGE_MAP_NAME";

	private final int MAIN_MENU_FIND 		 = 1;
	private final int MAIN_MENU_LIBRARY 	 = 2;
	private final int MAIN_MENU_ROUTES 		 = 3;
	private final int MAIN_MENU_TIME 		 = 4;
	private final int MAIN_MENU_STATION 	 = 5;
	private final int MAIN_MENU_SETTINGS 	 = 6;
	private final int MAIN_MENU_ABOUT 		 = 7;
	private final int MAIN_MENU_EXPERIMENTAL = 8;

	private final int REQUEST_CODE_MAP = 1;


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_FIND, 	0, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, MAIN_MENU_LIBRARY, 	1, R.string.menu_library).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, MAIN_MENU_ROUTES, 	2, R.string.menu_routes).setIcon(android.R.drawable.ic_menu_directions);
		menu.add(0, MAIN_MENU_SETTINGS, 5, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 	6, R.string.menu_about);
		
		menu.add(0, MAIN_MENU_EXPERIMENTAL, 	7, R.string.menu_experimental);
		
		mMainMenuTime = menu.add(0, MAIN_MENU_TIME, 	3, getNextTimeOfDay() ).setIcon(android.R.drawable.ic_menu_rotate);
		mMainMenuStation = menu.add(0, MAIN_MENU_STATION, 	4, R.string.menu_station).setIcon(android.R.drawable.ic_menu_info_details).setEnabled(mSelectedStationId!=null);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_FIND:
			return true;
		case MAIN_MENU_LIBRARY:
			Intent i = new Intent(this,BrowseLibrary.class);
			if(mPackageFileName!=null){
				i.setData(Uri.parse("ametro://" + mPackageFileName ));
			}
			startActivityForResult(i, REQUEST_CODE_MAP);
			return true;
		case MAIN_MENU_ROUTES:
			return true;
		case MAIN_MENU_SETTINGS:
			return true;
		case MAIN_MENU_ABOUT:
			return true;
		case MAIN_MENU_TIME:
			mTimeOfDay++;
			if(mTimeOfDay>2) mTimeOfDay = 0;
			mMainMenuTime.setTitle(getNextTimeOfDay());
			updateTitle();
			return true;
		case MAIN_MENU_STATION:
			return true;
		case MAIN_MENU_EXPERIMENTAL:
			startActivity(new Intent(this,BrowseTileMap.class));
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		loadPreferences();
		if(mPackageFileName==null){
			unloadModel();
			setContentView(R.layout.no_map_loaded);
			startActivityForResult(new Intent(this,BrowseLibrary.class), REQUEST_CODE_MAP);
		}else{
			reloadMap();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		savePreferences();
		super.onStop();
	}

	private void loadPreferences() {
		SharedPreferences preferences = getSharedPreferences("aMetro", 0);
		mPackageFileName = preferences.getString(PREFERENCE_PACKAGE_FILE_NAME, null);
		mPackageMapName = preferences.getString(PREFERENCE_PACKAGE_MAP_NAME, "metro");
	}

	private void savePreferences() {
		SharedPreferences preferences = getSharedPreferences("aMetro", 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_PACKAGE_FILE_NAME, mPackageFileName);
		editor.putString(PREFERENCE_PACKAGE_MAP_NAME, mPackageMapName);
		editor.commit();
	}

	private void clearPreferences() {
		SharedPreferences preferences = getSharedPreferences("aMetro", 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_PACKAGE_FILE_NAME, null);
		editor.putString(PREFERENCE_PACKAGE_MAP_NAME, null);
		editor.commit();
		mPackageFileName = null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK){
			Uri uri = data.getData();
			String path = uri.toString().replace("ametro://", "");
			if(mPackageFileName!=path){
				mPackageFileName = path;
				reloadMap();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void updateTitle()
	{
		if(mModel==null){
			setTitle(R.string.app_name);
		}else{
			setTitle(String.format("%s - %s (%s)", getString(R.string.app_name), mModel.getCityName(), getString(getTimeOfDay()).toLowerCase()) );
		}
	}

	private final Handler mHandler = new Handler();	

	private final Runnable mUpdateContentView = new Runnable() {
		public void run() {
			try {
				mMapImageView = new MapImageView(getApplicationContext(),mModel);
				mMapImageView.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				setProgressBarVisibility(false);
				setContentView(mMapImageView);
				updateTitle();
				mMapImageView.preRender();
				savePreferences();
			} catch (Throwable e) {
				clearPreferences();
				unloadModel();
				setContentView(R.layout.no_map_loaded);
			}
		}
	};    

	private final Runnable mSetNoMapContentView = new Runnable() {
		public void run() {
			clearPreferences();
			unloadModel();
			setContentView(R.layout.no_map_loaded);
		}
	};    

	private void reloadMap() {
		setContentView(R.layout.logo);
		setProgressBarVisibility(true);
		unloadModel();
		Runtime.getRuntime().gc();
		new Thread(){
			public void run() {
				try {
					mModel = ModelBuilder.Create(MapSettings.CATALOG_PATH, mPackageFileName, mPackageMapName);
					mHandler.post(mUpdateContentView);
				} catch (Exception e) {
					Log.e("aMetro", "Failed to load map", e);
					mHandler.post(mSetNoMapContentView);
				}
			}
		}.start();
	}

	private void unloadModel() {
		if(mMapImageView!=null){
			mMapImageView.setModel(null);
		}
		mMapImageView = null;
		mModel = null;
	}


	private int getNextTimeOfDay(){
		switch (mTimeOfDay) {
		case 2:
			return R.string.day;
		case 0:
			return R.string.stress;
		case 1:
			return R.string.night;
		}
		return -1;
	}

	private int getTimeOfDay(){
		switch (mTimeOfDay) {
		case 0:
			return R.string.day;
		case 1:
			return R.string.stress;
		case 2:
			return R.string.night;
		}
		return -1;
	}	
}