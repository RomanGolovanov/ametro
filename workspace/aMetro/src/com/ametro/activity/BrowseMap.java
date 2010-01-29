package com.ametro.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ametro.MapSettings;
import com.ametro.MapUri;
import com.ametro.model.ModelTileManager;
import com.ametro.R;
import com.ametro.widget.TileImageView;

public class BrowseMap extends Activity implements TileImageView.IDataProvider{

	TileImageView mTileImageView;
	ModelTileManager mTileManager;
	
	//private String mMapName;

	private int mTimeOfDay = 0;
	private Integer mSelectedStationId = null;

	private MenuItem mMainMenuTime;
	private MenuItem mMainMenuStation;


	private final int MAIN_MENU_FIND 		 = 1;
	private final int MAIN_MENU_LIBRARY 	 = 2;
	private final int MAIN_MENU_ROUTES 		 = 3;
	private final int MAIN_MENU_TIME 		 = 4;
	private final int MAIN_MENU_STATION 	 = 5;
	private final int MAIN_MENU_SETTINGS 	 = 6;
	private final int MAIN_MENU_ABOUT 		 = 7;
	private final int MAIN_MENU_EXPERIMENTAL = 8;

	private final static int REQUEST_BROWSE_LIBRARY = 1;
	private static final int REQUEST_RENDER_MAP = 2;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapSettings.checkPrerequisite(this);
		setContentView(R.layout.no_map_loaded);
		
		Intent intent = getIntent();
		Uri uri = intent!= null ? intent.getData() : null;
		if(uri!=null){
			initializeMapView(uri, true);
		}else{
			MapSettings.loadDefaultMapName(this);
			if(MapSettings.getMapName()==null){
				setContentView(R.layout.no_map_loaded);
				Intent browseLibraryIntent = new Intent(this,BrowseLibrary.class);
				startActivityForResult(browseLibraryIntent,REQUEST_BROWSE_LIBRARY);
			}else{
				initializeMapView(MapUri.create(MapSettings.getMapName()), true);
			}
		}
	}

	@Override
	protected void onPause() {
		saveScroll();
		super.onPause();
	}
	

	private void handleConfigurationException(Exception e) {
		MapSettings.clearDefaultMapName(this);
		Toast.makeText(this, "Configuration error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		setContentView(R.layout.no_map_loaded);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_BROWSE_LIBRARY:
		case REQUEST_RENDER_MAP:
			if(resultCode == RESULT_OK){
				Uri uri = data.getData();
				if(uri!=null){
					initializeMapView(uri, requestCode!=REQUEST_RENDER_MAP);
				}
			}
			if(resultCode == RESULT_CANCELED && requestCode == REQUEST_RENDER_MAP){
				MapSettings.clearDefaultMapName(this);
			}
			if(resultCode == RESULT_CANCELED && requestCode == REQUEST_BROWSE_LIBRARY){
				String mapName = MapSettings.getMapName();
				if(!ModelTileManager.isExist(mapName, 0)){
					initializeMapView(MapUri.create(MapSettings.getMapName()), true);
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

			if(MapSettings.getMapName()!=null){
				i.setData(MapUri.create(MapSettings.getMapName() ));
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
			requestCreateMapCache(MapUri.create(MapSettings.getMapName()));
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}	

	private void initializeMapView(Uri uri, boolean allowCreateMapCache)  {
		String mapName = MapUri.getMapName(uri);
		if(!ModelTileManager.isExist(mapName, 0)){
			if(allowCreateMapCache){
				requestCreateMapCache(uri);
			}
		}else{
			try{
				try {
					mTileManager = ModelTileManager.load(uri);
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
				ViewGroup layout = new FrameLayout(this);
				layout.addView(mTileImageView);
				setContentView(layout);
				MapSettings.setMapName(mapName);
				mTileImageView.setDataProvider(this);
				updateTitle();
				restoreScroll();
				MapSettings.saveDefaultMapName(this);
			} catch (Exception e) {
				handleConfigurationException(e);
			}
		}
	}

	private void saveScroll() {
		if(mTileImageView!=null && mTileManager!=null && MapSettings.getMapName()!=null){
			MapSettings.saveScrollPosition(this, mTileImageView.getScrollCenter());
		}
	}

	private void restoreScroll() {
		if(mTileImageView!=null && mTileManager!=null && MapSettings.getMapName()!=null){
			Point position  = MapSettings.loadScrollPosition(this);

			if(position == null){
				Point size = mTileManager.getContentSize();
				position = new Point(size.x/2 , size.y/2);
				
			}			
			mTileImageView.setScrollCenter(position.x, position.y);
		}
	}


	private void requestCreateMapCache(Uri uri) {
		Intent createMapCache = new Intent(this, RenderMap.class);
		createMapCache.setData(uri);
		startActivityForResult(createMapCache, REQUEST_RENDER_MAP);
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
