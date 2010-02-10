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

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ZoomControls;
import org.ametro.MapSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.model.Model;
import org.ametro.model.ModelBuilder;
import org.ametro.widget.VectorMapView;

import static org.ametro.Constants.LOG_TAG_MAIN;

public class BrowseVectorMap extends Activity {

	private final int MAIN_MENU_FIND = 1;
	private final int MAIN_MENU_LIBRARY = 2;
	private final int MAIN_MENU_ROUTES = 3;
	private final int MAIN_MENU_TIME = 4;
	private final int MAIN_MENU_STATION = 5;
	private final int MAIN_MENU_SETTINGS = 6;
	private final int MAIN_MENU_ABOUT = 7;

	private final float[] ZOOMS = new float[] { 1.5f, 1.0f, 0.8f, 0.6f, 0.4f, 0.3f, 0.2f, 0.1f };
	private final int[] STEPS = new int[] { 15, 10, 8, 6, 4, 3, 2, 1 };
	private final int MIN_ZOOM_LEVEL = 0;
	private final int MAX_ZOOM_LEVEL = 7;
	private final int DEFAULT_ZOOM_LEVEL = 1;
	private int mZoom = DEFAULT_ZOOM_LEVEL;

	private Model mModel;
	private VectorMapView mMapView;
	private ZoomControls mZoomControls;

	private InitTask mInitTask;

	private final static int REQUEST_BROWSE_LIBRARY = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapSettings.checkPrerequisite(this);
		setContentView(R.layout.global_wait);

		Intent intent = getIntent();
		Uri uri = intent != null ? intent.getData() : null;
		if (uri != null) {
			initializeMapView(uri);
		} else {
			MapSettings.loadDefaultMapName(this);
			if (MapSettings.getMapName() == null) {
				onRequestBrowseLibrary(true);
			} else {
				initializeMapView(MapUri.create(MapSettings.getMapName()));
			}
		}

	}

	private void initializeMapView(Uri uri) {
		mInitTask = new InitTask();
		mInitTask.execute(uri);
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
					initializeMapView(uri);
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

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_FIND:
			return true;
		case MAIN_MENU_LIBRARY:
			onRequestBrowseLibrary(false);
			return true;
		case MAIN_MENU_ROUTES:
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onSaveMapState() {
		if (mModel != null && mMapView != null
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
		if (mModel != null && mMapView != null
				&& MapSettings.getMapName() != null) {
			Integer zoom = null;//MapSettings.loadZoom(this);
			if (zoom != null) {
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, "Use saved map zoom " + zoom);
				setZoom(zoom);
			} else {
				zoom = MIN_ZOOM_LEVEL;
				int modelWidth = mModel.getWidth();
				int modelHeight = mModel.getHeight();
				Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
				int width = display.getWidth();  
				int height = display.getHeight();
				while(zoom < MAX_ZOOM_LEVEL){
					int scaledWidth = (int)( modelWidth * ZOOMS[zoom] ); 
					int scaledHeight = (int)( modelHeight * ZOOMS[zoom] ); 
					if(scaledWidth<=width && scaledHeight<=height){
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
				int x = mModel.getWidth() / 2;
				int y = mModel.getHeight() / 2;
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

	private void onUpdateTitle() {
		if (mModel == null) {
			setTitle(R.string.app_name);
		} else {
			setTitle(String.format("%s - %s", getString(R.string.app_name),
					mModel.getCityName()));
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

	private class InitTask extends AsyncTask<Uri, Void, Model> {

		protected void onPreExecute() {
			setContentView(R.layout.global_wait);
			super.onPreExecute();
		}

		protected Model doInBackground(Uri... params) {
			Uri mapUri = params[0];
			try {
				return ModelBuilder.loadModel(MapSettings
						.getMapFileName(mapUri));
			} catch (Exception e) {
				return null;
			}
		}

		protected void onCancelled() {
			setContentView(R.layout.browse_map_empty);
			super.onCancelled();
		}

		protected void onPostExecute(Model result) {
			if (result != null) {
				mModel = result;
				if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO))
					Log.i(LOG_TAG_MAIN, "Loaded model " + mModel.getMapName()
							+ " width size " + mModel.getWidth() + "x"
							+ mModel.getHeight());

				setContentView(R.layout.browse_vector_map_main);

				mMapView = (VectorMapView) findViewById(R.id.browse_vector_map_view);
				mMapView.setModel(mModel);
				mZoomControls = (ZoomControls) findViewById(R.id.browse_vector_map_zoom);
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

				MapSettings.setMapName(mModel.getMapName());
				onRestoreMapState();
				onUpdateTitle();
				mMapView.requestFocus();
			} else {

			}
			super.onPostExecute(result);
		}

	}

}
