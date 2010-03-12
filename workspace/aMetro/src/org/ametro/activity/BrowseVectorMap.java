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

package org.ametro.activity;

import static org.ametro.Constants.LOG_TAG_MAIN;
import static org.ametro.MapSettings.PREFERENCE_FAVORITE_ROUTES;
import static org.ametro.MapSettings.PREFERENCE_PACKAGE_FILE_NAME;
import static org.ametro.MapSettings.PREFERENCE_SCROLL_POSITION;
import static org.ametro.MapSettings.PREFERENCE_ZOOM_LEVEL;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.ametro.Constants;
import org.ametro.MapSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.model.City;
import org.ametro.model.Deserializer;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayRoute;
import org.ametro.model.SubwaySegment;
import org.ametro.model.SubwayStation;
import org.ametro.util.DateUtil;
import org.ametro.util.SerializeUtil;
import org.ametro.util.StringUtil;
import org.ametro.widget.VectorMapView;
import org.ametro.widget.BaseMapView.OnMapEventListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class BrowseVectorMap extends Activity implements OnClickListener {

	public void onClick(View src) {
		if(src == mNavigatePreviousButton){
			navigatePreviuosStation();
		}
		if(src == mNavigateNextButton){
			navigateNextStation();
		}
		if(src == mNavigateClearButton){
			setNavigationStations(null);
		}
		if(src == mNavigateListButton && mRoute!=null){
			startActivity(new Intent(this, BrowseRoute.class));
		}
		if(src == mNavigateListButton && mRoute == null && mNavigationStations!=null){
			startActivity(new Intent(this, SearchStation.class));
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Instance != null){
			mSubwayMap = Instance.mSubwayMap;
			mMapName = Instance.mMapName;
		}
		Instance = this;
		Instance.mDefaultLocale = Locale.getDefault();

		MapSettings.checkPrerequisite();
		setupLocale();

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL); 
		setContentView(R.layout.global_wait);

		if(mSubwayMap!=null){
			
			onShowMap(mSubwayMap);
		}else{
			Intent intent = getIntent();
			Uri uri = intent != null ? intent.getData() : null;
			if (uri != null) {
				onInitializeMapView(uri);
			} else {
				loadDefaultMapName();
				if (mMapName == null) {
					onRequestBrowseLibrary(true);
				} else {
					onInitializeMapView(MapUri.create(mMapName));
				}
			}
		}
	}

	protected void onPause() {
		onSaveMapState();
		super.onPause();
	}
	
	protected void onDestroy() {
		//Instance = null;
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_BROWSE_LIBRARY:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				if (uri != null) {
					String mapName = MapUri.getMapName(uri);
					if(!mapName.equalsIgnoreCase(getMapName())){
						onInitializeMapView(uri);
					}
				}
			}
			break; 
		case REQUEST_SETTINGS:
			if(isConfigurationChanged()){
				setupLocale();
				if (mMapName != null) {
					onInitializeMapView(MapUri.create(mMapName));
				}			
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_FIND, 0, R.string.menu_search).setIcon(
				android.R.drawable.ic_menu_search);
		menu.add(0, MAIN_MENU_LIBRARY, 1, R.string.menu_library).setIcon(
				android.R.drawable.ic_menu_mapmode);
		menu.add(0, MAIN_MENU_ROUTES, 2, R.string.menu_routes).setIcon(
				android.R.drawable.ic_menu_directions);
		menu.add(0, MAIN_MENU_SETTINGS, 5, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 6, R.string.menu_about);

		menu.add(0, MAIN_MENU_TIME, 3, R.string.menu_time).setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(0, MAIN_MENU_STATION, 4, R.string.menu_station).setIcon(
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_STATION).setEnabled(mCurrentStation!=null);
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_FIND:
			onSearchRequested();
			return true;
		case MAIN_MENU_LIBRARY:
			onRequestBrowseLibrary(false);
			return true;
		case MAIN_MENU_ROUTES:
			startActivity(new Intent(this, CreateRoute.class));
			return true;
		case MAIN_MENU_SETTINGS:
			startActivityForResult(new Intent(this, Settings.class), REQUEST_SETTINGS);
			return true;
		case MAIN_MENU_ABOUT:
			startActivity(new Intent(this, About.class));
			return true;
		case MAIN_MENU_TIME:
			return true;
		case MAIN_MENU_STATION:
			startActivity(new Intent(this, BrowseStation.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public Locale getLocale(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		final String localeName = settings.getString(getString(R.string.pref_locale_key), null);
		return localeName!=null && localeName.length()>0 ? new Locale(localeName) : mDefaultLocale;
    }
    
	public void setupLocale() {
   		Locale.setDefault(getLocale());
	}

	public boolean isEnabledAddonsImport() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(getString(R.string.pref_auto_import_addons_key), false);
	}

	public boolean isConfigurationChanged() {
		Locale currentLocale = Locale.getDefault();
		Locale newLocale = getLocale();
		if(!currentLocale.equals(newLocale)){
			return true;
		}
		return false;
	}
	
    public void loadDefaultMapName(){
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        mMapName = preferences.getString(PREFERENCE_PACKAGE_FILE_NAME, null);
    }

    public void saveDefaultMapName() {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_PACKAGE_FILE_NAME, mMapName);
        editor.commit();
    }

    public void clearDefaultMapName() {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(PREFERENCE_PACKAGE_FILE_NAME);
        editor.commit();
        mMapName = null;
    }

    public void addFavoriteRoute(int fromId, int toId)
    {
    	ArrayList<Point> routes = new ArrayList<Point>( Arrays.asList(getFavoriteRoutes()) );
    	Point r = new Point(fromId, toId);
    	if(!routes.contains(r)){
    		routes.add(r);
    		setFavoriteRoutes((Point[]) routes.toArray(new Point[routes.size()]));
    	}
    }

    public void removeFavoriteRoute(int fromId, int toId)
    {
    	ArrayList<Point> routes = new ArrayList<Point>( Arrays.asList(getFavoriteRoutes()) );
    	Point r = new Point(fromId, toId);
    	if(routes.contains(r)){
    		routes.remove(r);
    		setFavoriteRoutes((Point[]) routes.toArray(new Point[routes.size()]));
    	}
    }

    public boolean isFavoriteRoute(int fromId, int toId)
    {
    	ArrayList<Point> routes = new ArrayList<Point>( Arrays.asList(getFavoriteRoutes()) );
    	Point r = new Point(fromId, toId);
    	return routes.contains(r);
    }
    
    public void clearFavoriteRoutes()
    {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_FAVORITE_ROUTES + "_" + mMapName, "");
        editor.commit();
    }

    public void setFavoriteRoutes(Point[] routes)
    {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for(Point p : routes){
        	sb.append(StringUtil.formatPoint(p));
        	idx++;
        	if(idx<routes.length){
        		sb.append(",");
        	}
        }
        String routesBudle = sb.toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_FAVORITE_ROUTES + "_" + mMapName, routesBudle);
        editor.commit();
    }
    
    public Point[] getFavoriteRoutes()
    {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        String routes = preferences.getString(PREFERENCE_FAVORITE_ROUTES + "_" + mMapName, "");
        return SerializeUtil.parsePointArray(routes);
    }
    
    public void saveScrollPosition(PointF position) {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        String scrollPosition = StringUtil.formatPointF(position);
        editor.putString(PREFERENCE_SCROLL_POSITION + "_" + mMapName, scrollPosition);
        editor.commit();
		if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(LOG_TAG_MAIN, getString(R.string.log_save_map_position) + scrollPosition);
		}
    }

    public PointF loadScrollPosition() {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        String pref = preferences.getString(PREFERENCE_SCROLL_POSITION + "_" + mMapName, null);
        if (pref != null) {
            return SerializeUtil.parsePointF(pref);
        } else {
            return null;
        }
    }

    public void clearScrollPosition(String mapName) {
        if (mapName != null) {
            SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(PREFERENCE_SCROLL_POSITION + "_" + mapName);
            editor.commit();
        }
    }

    public void saveZoom(int zoomLevel) {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_ZOOM_LEVEL + "_" + mMapName, Integer.toString(zoomLevel));
        editor.commit();
		if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(LOG_TAG_MAIN, getString(R.string.log_save_map_zoom) + zoomLevel);
		}
    }

    public Integer loadZoom() {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        String pref = preferences.getString(PREFERENCE_ZOOM_LEVEL + "_" + mMapName, null);
        if (pref != null) {
            return SerializeUtil.parseNullableInteger(pref);
        } else {
            return null;
        }
    }

    public void clearZoom(String mapName) {
        if (mapName != null) {
            SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(PREFERENCE_ZOOM_LEVEL + "_" + mapName);
            editor.commit();
        }
    }

	public SubwayMap getSubwayMap() {
		return mSubwayMap;
	}

	public String getMapName() {
		return mMapName;
	}

	/*package*/ SubwayStation getCurrentStation()
	{
		return mCurrentStation;	
	}

	/*package*/ void setCurrentStation(SubwayStation station){
		if(station!=null && mNavigationStations.contains(station)){
			mCurrentStation = station;
			int idx = mNavigationStations.indexOf(mCurrentStation);
			mNavigatePreviousButton.setEnabled( idx != 0 );
			mNavigateNextButton.setEnabled( idx != (mNavigationStations.size()-1) );
			if(mNavigationStations.size()>1){
				mNavigateListButton.setVisibility(View.VISIBLE);
				mNavigatePreviousButton.setVisibility(View.VISIBLE);
				mNavigateNextButton.setVisibility(View.VISIBLE);
			}else{
				mNavigateListButton.setVisibility(View.GONE);
				mNavigatePreviousButton.setVisibility(View.INVISIBLE);
				mNavigateNextButton.setVisibility(View.INVISIBLE);
			}
			mScrollHandler.post(mUpdateUI);
		}else{
			mCurrentStation = null;
		}
	}

	/*package*/ ArrayList<SubwayStation> getNavigationStations()
	{
		return mNavigationStations;
	}

	/*package*/ void setNavigationStations(ArrayList<SubwayStation> stations){
		boolean refreshNeeded = (stations != mNavigationStations) || (stations == null && mNavigationStations!=null);
		if(refreshNeeded){
			if(stations!=null){
				mRoute = null;
				mNavigationSegments = null;
				mNavigationStations = stations;
				mCurrentStation = stations.get(0);
				showNavigationControls();
			}else{
				hideNavigationControls();
				mRoute = null;
				mNavigationStations = null;
				mNavigationSegments = null;
				mCurrentStation = null;
			}
			mMapView.setModelSelection(stations, null);
			mMapView.postInvalidate();
		}
	}

	/*package*/ SubwayRoute getNavigationRoute()
	{
		return mRoute;
	}

	/*package*/ void setNavigationRoute(SubwayRoute route){
		boolean refreshNeeded = (route != mRoute) || (route == null && mRoute!=null) || (route!=null && mRoute == null);
		if(refreshNeeded){
			mRoute = route;
			if(route!=null){
				mNavigationSegments = route.getSegments();
				mNavigationStations = route.getStations();
				setCurrentStation( mNavigationStations.get(0) );
				showNavigationControls();
			}else{
				hideNavigationControls();
				mNavigationStations = null;
				mNavigationSegments = null;
				setCurrentStation(null);
			}
			mMapView.setModelSelection(mNavigationStations, mNavigationSegments);
			mMapView.postInvalidate();
		}
	}

	private void onSaveMapState() {
		if (mSubwayMap != null && mMapView != null && mMapName != null) {
			PointF pos = mMapView.getModelScrollCenter();
			int zoom = mZoom;
			saveScrollPosition(pos);
			saveZoom(zoom);
		}
	}

	private void onRestoreMapState() {
		if (mSubwayMap != null && mMapView != null
				&& mMapName != null) {
			Integer zoom = loadZoom();
			if (zoom != null) {
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, getString(R.string.log_restore_map_zoom) + zoom);
				setZoom(zoom);
			} else {
				zoom = MIN_ZOOM_LEVEL;
				int modelWidth = mSubwayMap.width;
				int modelHeight = mSubwayMap.height;
				Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
				int width = display.getWidth();
				int height = display.getHeight();
				while (zoom < MAX_ZOOM_LEVEL) {
					int scaledWidth = (int) (modelWidth * ZOOMS[zoom]);
					int scaledHeight = (int) (modelHeight * ZOOMS[zoom]);
					if (scaledWidth <= width && scaledHeight <= height) {
						break;
					}
					zoom++;
				}
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, getString(R.string.log_default_map_zoom) + zoom);
				setZoom(zoom);
			}
			PointF pos = loadScrollPosition();
			if (pos != null) {
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, getString(R.string.log_restore_map_position) + pos.x
							+ "x" + pos.y);
				mMapView.setModelScrollCenter(pos);
			} else {
				int x = mSubwayMap.width / 2;
				int y = mSubwayMap.height / 2;
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, getString(R.string.log_default_map_position) + x
							+ "x" + y);
				mMapView.setModelScrollCenter(new PointF(x, y));
			}

		}
	}

	private void onRequestBrowseLibrary(boolean setNoMapLoadingView) {
		if (setNoMapLoadingView) {
			setContentView(R.layout.browse_map_empty);
		}
		Intent browseLibrary = new Intent(this, BrowseLibrary.class);
		if (mMapName != null) {
			browseLibrary.setData(MapUri.create(mMapName));
		}
		startActivityForResult(browseLibrary, REQUEST_BROWSE_LIBRARY);
	}

	private void onInitializeMapView(Uri uri) {
		mInitTask = new InitTask();
		mInitTask.execute(uri);
	}


	private void onShowMap(SubwayMap subwayMap) {
		mSubwayMap = subwayMap;
		if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
			Log.i(LOG_TAG_MAIN, getString(R.string.log_loaded_subway_map) + mSubwayMap.mapName
					+ getString(R.string.log_with_size) + mSubwayMap.width + "x"
					+ mSubwayMap.height);

		setContentView(R.layout.browse_vector_map_main);

		mMapView = (VectorMapView) findViewById(R.id.browse_vector_map_view);
		mMapView.setModel(mSubwayMap);
		mZoomControls = (ZoomControls) findViewById(R.id.browse_vector_map_zoom);
		mZoomControls.setVisibility(View.INVISIBLE);

		mNavigationPanelTop = (View)findViewById(R.id.browse_vector_map_panel_top);
		mNavigationPanelBottom = (View)findViewById(R.id.browse_vector_map_panel_bottom);

		mNavigatePreviousButton = (ImageButton)findViewById(R.id.browse_vector_map_button_prev);
		mNavigateNextButton = (ImageButton)findViewById(R.id.browse_vector_map_button_next);
		mNavigateClearButton = (ImageButton)findViewById(R.id.browse_vector_map_button_clear);
		mNavigateListButton = (ImageButton)findViewById(R.id.browse_vector_map_button_list);

		mNavigateTimeText = (TextView)findViewById(R.id.browse_vector_map_time);

		mNavigatePreviousButton.setOnClickListener(this);
		mNavigateNextButton.setOnClickListener(this);
		mNavigateClearButton.setOnClickListener(this);
		mNavigateListButton.setOnClickListener(this);

		hideNavigationControls();

		mMapName = mSubwayMap.mapName;
		onRestoreMapState();
		bindMapEvents();
		mMapView.requestFocus();
		saveDefaultMapName();
	}

	private void bindMapEvents() {
		mMapView.setOnMapEventListener(new OnMapEventListener() {
			public void onShortClick(int x, int y) {
				if (mZoomControls.getVisibility() != View.VISIBLE) {
					showZoom();
				}
				delayZoom();
			}

			public void onMove(int newx, int newy, int oldx, int oldy) {
				if (mZoomControls.getVisibility() != View.VISIBLE) {
					showZoom();
				}
				delayZoom();
			}

			public void onLongClick(int x, int y) {
			}
		});

		mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				BrowseVectorMap.this.onZoomIn();
			}
		});
		mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				BrowseVectorMap.this.onZoomOut();
			}
		});

		mZoomControlRunnable = new Runnable() {
			public void run() {
				if (!mZoomControls.hasFocus()) {
					hideZoom();
				} else {
					delayZoom();
				}
			}
		};

	}

	private void onZoomIn() {
		setZoom(mZoom - 1);
	}

	private void onZoomOut() {
		setZoom(mZoom + 1);
	}

	private void setZoom(int zoom) {
		mZoom = Math.min(Math.max(zoom, MIN_ZOOM_LEVEL), MAX_ZOOM_LEVEL);
		mZoomControls.setIsZoomInEnabled(mZoom > MIN_ZOOM_LEVEL);
		mZoomControls.setIsZoomOutEnabled(mZoom < MAX_ZOOM_LEVEL);
		mMapView.setScale(ZOOMS[mZoom], STEPS[mZoom]);
	}

	private void delayZoom() {
		mPrivateHandler.removeCallbacks(mZoomControlRunnable);
		mPrivateHandler.postDelayed(mZoomControlRunnable, ZOOM_CONTROLS_TIMEOUT);
	}

	public void showZoom() {
		fadeZoom(View.VISIBLE, 0.0f, 1.0f);
	}

	public void hideZoom() {
		fadeZoom(View.INVISIBLE, 1.0f, 0.0f);
	}

	private void fadeZoom(int visibility, float startAlpha, float endAlpha) {
		AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
		anim.setDuration(500);
		mZoomControls.startAnimation(anim);
		mZoomControls.setVisibility(visibility);
	}

	private void hideNavigationControls() {
		mNavigationPanelBottom.setVisibility(View.INVISIBLE);
		mNavigationPanelTop.setVisibility(View.INVISIBLE);
	}

	private void showNavigationControls() {
		mNavigationPanelBottom.setVisibility(View.VISIBLE);
		mNavigationPanelTop.setVisibility(View.VISIBLE);
		if(mRoute!=null){
			long time = mRoute.getStationDelay(mNavigationStations.get(mNavigationStations.size()-1));
			mNavigateTimeText.setText(DateUtil.getTimeHHMM(time));
			mNavigateTimeText.setVisibility(View.VISIBLE);
		}else{
			mNavigateTimeText.setText("");
			mNavigateTimeText.setVisibility(View.INVISIBLE);
		}

	}

	private void navigateNextStation(){
		if(mNavigationStations!=null && mNavigationStations.size()>0){
			if(mCurrentStation == null){
				setCurrentStation(mNavigationStations.get(0));
			}else{
				int idx = mNavigationStations.indexOf(mCurrentStation) + 1;
				if(idx < mNavigationStations.size()){
					setCurrentStation(mNavigationStations.get(idx));
				}
			}
		}
	}

	private void navigatePreviuosStation(){
		if(mNavigationStations!=null && mNavigationStations.size()>0){
			if(mCurrentStation == null){
				setCurrentStation(mNavigationStations.get(mNavigationStations.size()-1));
			}else{
				int idx = mNavigationStations.indexOf(mCurrentStation) - 1;
				if(idx >= 0){
					setCurrentStation(mNavigationStations.get(idx));
				}
			}
		}
	}

	private class InitTask extends AsyncTask<Uri, Void, SubwayMap> {

		Throwable mError;

		protected void onPreExecute() {
			mError = null;
			setContentView(R.layout.global_wait);
			super.onPreExecute();
		}

		protected SubwayMap doInBackground(Uri... params) {
			try {
				Uri mapUri = params[0];
				City city = Deserializer.deserialize(new FileInputStream(MapSettings.getMapFileName(mapUri)));
				if (city != null) {
					return city.subwayMap;
				} else {
					return null;
				}
			} catch (Exception e) {
				mError = e;
				if (Log.isLoggable(LOG_TAG_MAIN, Log.ERROR))
					Log.e(LOG_TAG_MAIN, getString(R.string.log_failed_map_loading), e);
				return null;
			}
		}

		protected void onCancelled() {
			setContentView(R.layout.browse_map_empty);
			super.onCancelled();
		}

		protected void onPostExecute(SubwayMap result) {
			if (result != null) {
				onShowMap(result);
			} else {
				clearDefaultMapName();
				if (mError != null) {
					Toast.makeText(BrowseVectorMap.this,
							getString(R.string.msg_error_map_loading) + mError.toString(),
							Toast.LENGTH_SHORT).show();
				}

				onRequestBrowseLibrary(true);

			}
			super.onPostExecute(result);
		}

	}

	private final Runnable mUpdateUI = new Runnable() {
		public void run() {
			final Point point = mCurrentStation.point;
			mMapView.scrollModelCenterTo(point.x, point.y);
			mMapView.postInvalidate();
		}
	};


	static BrowseVectorMap Instance;

	private final int MAIN_MENU_FIND = 1;
	private final int MAIN_MENU_LIBRARY = 2;
	private final int MAIN_MENU_ROUTES = 3;
	private final int MAIN_MENU_TIME = 4;
	private final int MAIN_MENU_STATION = 5;
	private final int MAIN_MENU_SETTINGS = 6;
	private final int MAIN_MENU_ABOUT = 7;

	private final float[] ZOOMS = new float[]{1.5f, 1.0f, 0.8f, 0.6f, 0.4f, 0.3f, 0.2f, 0.1f};
	private final int[] STEPS = new int[]{15, 10, 8, 6, 4, 3, 2, 1};

	private final int MIN_ZOOM_LEVEL = 0;
	private final int MAX_ZOOM_LEVEL = 7;
	private final int DEFAULT_ZOOM_LEVEL = 1;
	private final int ZOOM_CONTROLS_TIMEOUT = 2000;
	private int mZoom = DEFAULT_ZOOM_LEVEL;

	private String mMapName;
	private SubwayMap mSubwayMap;

	private VectorMapView mMapView;

	private ZoomControls mZoomControls;
	private Runnable mZoomControlRunnable;

	private View mNavigationPanelTop;
	private View mNavigationPanelBottom;
	private ImageButton mNavigatePreviousButton;
	private ImageButton mNavigateNextButton;
	private ImageButton mNavigateClearButton;
	private ImageButton mNavigateListButton;
	private TextView mNavigateTimeText;

	private SubwayRoute mRoute;

	private Handler mPrivateHandler = new Handler();
	private Handler mScrollHandler = new Handler();

	private InitTask mInitTask;

	private final static int REQUEST_BROWSE_LIBRARY = 1;
	private final static int REQUEST_SETTINGS = 2;

	private ArrayList<SubwayStation> mNavigationStations;
	private ArrayList<SubwaySegment> mNavigationSegments;
	private SubwayStation mCurrentStation;

	private Locale mDefaultLocale;


}
