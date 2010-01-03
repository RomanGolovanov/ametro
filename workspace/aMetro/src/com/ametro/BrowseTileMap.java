package com.ametro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ametro.libs.Helpers;
import com.ametro.model.TileManager;
import com.ametro.widgets.TileImageView;

public class BrowseTileMap extends Activity implements TileImageView.IDataProvider{

	TileImageView mTileImageView;
	TileManager mTileManager;

	private String mMapName;

	private int mTimeOfDay = 0;
	private Integer mSelectedStationId = null;

	private MenuItem mMainMenuTime;
	private MenuItem mMainMenuStation;

	private static final String PREFERENCE_PACKAGE_FILE_NAME = "PACKAGE_FILE_NAME";
	static final String PREFERENCE_SCROLL_POSITION = "SCROLL_POSITION";

	private final int MAIN_MENU_FIND 		 = 1;
	private final int MAIN_MENU_LIBRARY 	 = 2;
	private final int MAIN_MENU_ROUTES 		 = 3;
	private final int MAIN_MENU_TIME 		 = 4;
	private final int MAIN_MENU_STATION 	 = 5;
	private final int MAIN_MENU_SETTINGS 	 = 6;
	private final int MAIN_MENU_ABOUT 		 = 7;
	private final int MAIN_MENU_EXPERIMENTAL = 8;

	private final static int REQUEST_BROWSE_LIBRARY = 1;
	private static final int REQUEST_CREATE_MAP_CACHE = 2;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_map_loaded);
		Intent intent = getIntent();
		Uri uri = intent!= null ? intent.getData() : null;
		if(uri!=null){
			initializeMapView(uri, true);
		}else{
			loadPreferences();
			if(mMapName==null){
				setContentView(R.layout.no_map_loaded);
				Intent browseLibraryIntent = new Intent(this,BrowseLibrary.class);
				startActivityForResult(browseLibraryIntent,REQUEST_BROWSE_LIBRARY);
			}else{
				initializeMapView(MapUri.create(mMapName), true);
			}
		}
	}

	@Override
	protected void onPause() {
		saveScroll();
		super.onPause();
	}
	

	private void handleConfigurationException(Exception e) {
		clearPreferences();
		Toast.makeText(this, "Configuration error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		setContentView(R.layout.no_map_loaded);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_BROWSE_LIBRARY:
		case REQUEST_CREATE_MAP_CACHE:
			if(resultCode == RESULT_OK){
				Uri uri = data.getData();
				if(uri!=null){
					initializeMapView(uri, requestCode!=REQUEST_CREATE_MAP_CACHE);
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

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

			if(mMapName!=null){
				i.setData(MapUri.create(mMapName ));
			}
			startActivityForResult(i, REQUEST_BROWSE_LIBRARY);
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
			requestCreateMapCache(MapUri.create(mMapName));
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}	

	private void initializeMapView(Uri uri, boolean allowCreateMapCache)  {
		String mapName = MapUri.getMapName(uri);
		if(!TileManager.isExist(mapName, 0)){
			if(allowCreateMapCache){
				requestCreateMapCache(uri);
			}
		}else{
			try{
				try {
					mTileManager = TileManager.load(uri);
				} catch (Exception e) {
					if(allowCreateMapCache){
						requestCreateMapCache(uri);
						return;
					}else{
						throw e;
					}
				}
				mTileImageView = new TileImageView(this);
				mTileImageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				FrameLayout layout = new FrameLayout(this);
				layout.addView(mTileImageView);
				setContentView(layout);
				mMapName = mapName;
				mTileImageView.setDataProvider(this);
				updateTitle();
				restoreScroll();
				savePreferences();
			} catch (Exception e) {
				handleConfigurationException(e);
			}
		}
	}

	private void saveScroll() {
		if(mTileImageView!=null && mTileManager!=null && mMapName!=null){
			Point pos = mTileImageView.getScrollCenter();
			SharedPreferences preferences = getSharedPreferences("aMetro",0);
			SharedPreferences.Editor editor = preferences.edit();
			String scrollPosition =  "" + pos.x + "," + pos.y;
			editor.putString(PREFERENCE_SCROLL_POSITION + "_" + mMapName, scrollPosition  );
			editor.commit();
		}
	}

	private void restoreScroll() {
		if(mTileImageView!=null && mTileManager!=null && mMapName!=null){
			Point pos;
			SharedPreferences preferences = getSharedPreferences("aMetro",0);
			String pref = preferences.getString(PREFERENCE_SCROLL_POSITION + "_" + mMapName, null);
			if(pref!=null){
				pos = Helpers.parsePoint(pref);
			}else{
				Point size = mTileManager.getContentSize();
				pos = new Point(size.x/2 , size.y/2);
				
			}
			mTileImageView.setScrollCenter(pos.x, pos.y);
		}
	}

	private void requestCreateMapCache(Uri uri) {
		Intent createMapCache = new Intent(this, CreateMapCache.class);
		createMapCache.setData(uri);
		startActivityForResult(createMapCache, REQUEST_CREATE_MAP_CACHE);
	}

	private void loadPreferences() {
		SharedPreferences preferences = getSharedPreferences("aMetro", 0);
		mMapName = preferences.getString(PREFERENCE_PACKAGE_FILE_NAME, null);
	}

	private void savePreferences() {
		SharedPreferences preferences = getSharedPreferences("aMetro", 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_PACKAGE_FILE_NAME, mMapName);
		editor.commit();
	}

	private void clearPreferences() {
		SharedPreferences preferences = getSharedPreferences("aMetro", 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_PACKAGE_FILE_NAME, null);
		editor.commit();
		mMapName = null;
	}

	private void updateTitle()
	{
		if(mTileManager==null){
			setTitle(R.string.app_name);
		}else{
			setTitle(String.format("%s - %s (%s)", getString(R.string.app_name), mTileManager.getCityName(), getString(getTimeOfDay()).toLowerCase()) );
		}
	}

	private void updateSelectedStation(Integer stationId){
		mSelectedStationId = stationId;
		if(mMainMenuStation!=null){
			mMainMenuStation.setEnabled(mSelectedStationId!=null);
		}
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

	/// TILE RENDERER INTERFACE

	@Override
	public Bitmap getTile(Rect rect) {
		return mTileManager.getTile(rect);
	}

	@Override
	public Bitmap getLoadingTile() {
		return BitmapFactory.decodeResource(getResources(), R.drawable.tile);
	}

	@Override
	public Point getContentSize() {
		return mTileManager.getContentSize();
	}

}
