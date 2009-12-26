package com.ametro;


import com.ametro.model.Model;
import com.ametro.model.ModelBuilder;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class BrowseMap extends Activity {

	private Model mModel; 
	private MapImageView mMapImageView;
	private boolean mIsInitialized = false;

	private String mPackageLibraryPath = "/sdcard/ametro";
	private String mPackageFileName = "Moscow";
	private String mPackageMapName = "metro";
	private int mTimeOfDay = 0;

	private Integer mSelectedStationId = null;

	private MenuItem mMainMenuTime;
	private MenuItem mMainMenuStation;

	private final int MAIN_MENU_FIND 		= 1;
	private final int MAIN_MENU_LIBRARY 	= 2;
	private final int MAIN_MENU_ROUTES 		= 3;
	private final int MAIN_MENU_TIME 		= 4;
	private final int MAIN_MENU_STATION 	= 5;
	private final int MAIN_MENU_SETTINGS 	= 6;
	private final int MAIN_MENU_ABOUT 		= 7;

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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_FIND, 	0, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, MAIN_MENU_LIBRARY, 	1, R.string.menu_library).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, MAIN_MENU_ROUTES, 	2, R.string.menu_routes).setIcon(android.R.drawable.ic_menu_directions);
		menu.add(0, MAIN_MENU_SETTINGS, 5, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 	6, R.string.menu_about);
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
			startActivity(new Intent(this,BrowseLibrary.class));
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
		}		
		return super.onOptionsItemSelected(item);
	}
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!mIsInitialized){
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			setContentView(R.layout.logo);
			setProgressBarVisibility(true);
			mMapLoader.start();
			mIsInitialized = true;
		}
	}

	@Override
	protected void onStop() {
		mIsInitialized = false;
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void updateTitle()
	{
		if(mModel==null){
			setTitle(R.string.app_name);

		}else{
			setTitle(
				String.format("%s - %s (%s)",
					getString(R.string.app_name), 
					mModel.getCityName(), 
					getString(getTimeOfDay()).toLowerCase())
					);
			
		}
	}

	private void handleException(Exception e) {
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		finish();
	}

	private final Handler mHandler = new Handler();	

	private final Runnable mUpdateContentView = new Runnable() {
		public void run() {
			mMapImageView = new MapImageView(getApplicationContext(), mModel);
			mMapImageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
			setProgressBarVisibility(false);
			setContentView(mMapImageView);
			updateTitle();
		}
	};    

	private final Thread mMapLoader = new Thread(){
		public void run() {
			try {
				//mModel = ModelBuilder.Create("/sdcard/ametro/spb.pmz", "Metro.map");
				mModel = ModelBuilder.Create(mPackageLibraryPath, mPackageFileName, mPackageMapName);
				mIsInitialized = true;
				mHandler.post(mUpdateContentView);
			} catch (Exception e) {
				Log.e("aMetro", "Failed to load map", e);
				handleException(e);
			}
		}
	};



}