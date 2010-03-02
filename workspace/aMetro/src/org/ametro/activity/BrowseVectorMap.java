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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.ametro.MapSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.model.City;
import org.ametro.model.Deserializer;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwaySegment;
import org.ametro.model.SubwayStation;
import org.ametro.widget.VectorMapView;
import org.ametro.widget.BaseMapView.OnMapEventListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
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
		if(src == mNavigateListButton){

		}
		if(src == mNavigateListButton){
			startActivity(new Intent(this, SearchStation.class));
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Instance = this;

		MapSettings.checkPrerequisite(this);
		MapSettings.setupLocale(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL); 
		setContentView(R.layout.global_wait);

		
		Intent intent = getIntent();
		Uri uri = intent != null ? intent.getData() : null;
		if (uri != null) {
			onInitializeMapView(uri);
		} else {
			MapSettings.loadDefaultMapName(this);
			if (MapSettings.getMapName() == null) {
				onRequestBrowseLibrary(true);
			} else {
				onInitializeMapView(MapUri.create(MapSettings.getMapName()));
			}
		}
	}

	protected void onPause() {
		onSaveMapState();
		super.onPause();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_BROWSE_LIBRARY:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				if (uri != null) {
					onInitializeMapView(uri);
				}
			}
			break;
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
			startActivity(new Intent(this, Settings.class));
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


	/*package*/ SubwayStation getCurrentStation()
	{
		return mCurrentStation;	
	}

	/*package*/ void setCurrentStation(SubwayStation station){
		if(mNavigationStations.contains(station)){
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
				mNavigationStations = stations;
				mNavigationRoute = null;
				mCurrentStation = stations.get(0);
				showNavigationControls();
			}else{
				hideNavigationControls();
				mNavigationStations = null;
				mNavigationRoute = null;
				mCurrentStation = null;
			}
			mMapView.setModelSelection(stations, null);
			mMapView.postInvalidate();
		}
	}

	/*package*/ ArrayList<SubwaySegment> getNavigationRoute()
	{
		return mNavigationRoute;
	}

	/*package*/ void setNavigationRoute(ArrayList<SubwaySegment> segments){
		boolean refreshNeeded = (segments != mNavigationRoute) || (segments == null && mNavigationRoute!=null);
		if(refreshNeeded){
			if(segments!=null && segments.size()>0){
				mNavigationRoute = segments;
				mNavigationStations = getStationsByRoute(segments);
				mCurrentStation = mNavigationStations.get(0);
				showNavigationControls();
			}else{
				hideNavigationControls();
				mNavigationStations = null;
				mNavigationRoute = null;
				mCurrentStation = null;
			}
			mMapView.setModelSelection(mNavigationStations, mNavigationRoute);
			mMapView.postInvalidate();
		}
	}

	private void onSaveMapState() {
		if (mSubwayMap != null && mMapView != null
				&& MapSettings.getMapName() != null) {
			PointF pos = mMapView.getModelScrollCenter();
			int zoom = mZoom;
			if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
				Log.i(LOG_TAG_MAIN, "Saved map zoom " + zoom);
			if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
				Log.i(LOG_TAG_MAIN, "Save map position at " + pos.x + "x" + pos.y);
			MapSettings.saveScrollPosition(this, pos);
			MapSettings.saveZoom(this, zoom);
		}
	}

	private void onRestoreMapState() {
		if (mSubwayMap != null && mMapView != null
				&& MapSettings.getMapName() != null) {
			Integer zoom = MapSettings.loadZoom(this);
			if (zoom != null) {
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, "Use saved map zoom " + zoom);
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
					Log.i(LOG_TAG_MAIN, "Use minimal map zoom " + zoom);
				setZoom(zoom);
			}
			PointF pos = MapSettings.loadScrollPosition(this);
			if (pos != null) {
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, "Use saved map position at " + pos.x
							+ "x" + pos.y);
				mMapView.setModelScrollCenter(pos);
			} else {
				int x = mSubwayMap.width / 2;
				int y = mSubwayMap.height / 2;
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, "Use default map position at " + x
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
		if (MapSettings.getMapName() != null) {
			browseLibrary.setData(MapUri.create(MapSettings.getMapName()));
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
			Log.i(LOG_TAG_MAIN, "Loaded subwayMap " + mSubwayMap.mapName
					+ " width size " + mSubwayMap.width + "x"
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

		mNavigatePreviousButton.setOnClickListener(this);
		mNavigateNextButton.setOnClickListener(this);
		mNavigateClearButton.setOnClickListener(this);
		mNavigateListButton.setOnClickListener(this);

		hideNavigationControls();

		MapSettings.setMapName(mSubwayMap.mapName);
		MapSettings.setModel(mSubwayMap);
		onRestoreMapState();
		onUpdateTitle();
		bindMapEvents();
		mMapView.requestFocus();
		MapSettings.saveDefaultMapName(BrowseVectorMap.this);
	}

	private void bindMapEvents() {
		mMapView.setOnMapEventListener(new OnMapEventListener() {
			public void onShortClick(int x, int y) {
				if (mZoomControls.getVisibility() != View.VISIBLE) {
					showZoom();
				}
				delayZoom();
				//setSelectedStations(null);
				//Toast.makeText(BrowseVectorMap.this, "click", 200).show();
			}

			public void onMove(int newx, int newy, int oldx, int oldy) {
				if (mZoomControls.getVisibility() != View.VISIBLE) {
					showZoom();
				}
				delayZoom();
				//Toast.makeText(BrowseVectorMap.this, "move " + (newx-oldy) + "x" + (newy-oldy) , 200).show();
			}

			public void onLongClick(int x, int y) {
			}
		});

		mZoomControls
		.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				BrowseVectorMap.this.onZoomIn();
			}
		});
		mZoomControls
		.setOnZoomOutClickListener(new View.OnClickListener() {
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

	private void onUpdateTitle() {
		if (mSubwayMap == null) {
			setTitle(R.string.app_name);
		} else {
			setTitle(String.format("%s - %s", getString(R.string.app_name),
					mSubwayMap.cityName));
		}
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
					Log.e(LOG_TAG_MAIN, "Failed model loading", e);
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
				MapSettings.clearDefaultMapName(BrowseVectorMap.this);

				if (mError != null) {
					Toast.makeText(BrowseVectorMap.this,
							"Error map loading: " + mError.toString(),
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


	private ArrayList<SubwayStation> getStationsByRoute(ArrayList<SubwaySegment> segments) {
		HashSet<SubwayStation> set = new HashSet<SubwayStation>();
		for(SubwaySegment segment : segments){
			SubwayStation from = mSubwayMap.stations[segment.fromStationId];
			SubwayStation to = mSubwayMap.stations[segment.toStationId];
			if(!set.contains(from)){
				set.add(from);
			}
			if(!set.contains(to)){
				set.add(to);
			}
		}
		return new ArrayList<SubwayStation>(set);
	}

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

	private Handler mPrivateHandler = new Handler();
	private Handler mScrollHandler = new Handler();

	private InitTask mInitTask;

	private final static int REQUEST_BROWSE_LIBRARY = 1;

	private ArrayList<SubwayStation> mNavigationStations;
	private ArrayList<SubwaySegment> mNavigationRoute;
	private SubwayStation mCurrentStation;


}
