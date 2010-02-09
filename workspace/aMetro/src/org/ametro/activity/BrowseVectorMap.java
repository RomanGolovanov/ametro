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

import org.ametro.MapSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.model.Model;
import org.ametro.model.ModelBuilder;
import org.ametro.widget.VectorMapView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ZoomControls;

public class BrowseVectorMap extends Activity {

	
	private final int MAIN_MENU_FIND 		 = 1;
	private final int MAIN_MENU_LIBRARY 	 = 2;
	private final int MAIN_MENU_ROUTES 		 = 3;
	private final int MAIN_MENU_TIME 		 = 4;
	private final int MAIN_MENU_STATION 	 = 5;
	private final int MAIN_MENU_SETTINGS 	 = 6;
	private final int MAIN_MENU_ABOUT 		 = 7;
	
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
		Uri uri = intent!= null ? intent.getData() : null;
		if(uri!=null){
			initializeMapView(uri);
		}else{
			MapSettings.loadDefaultMapName(this);
			if(MapSettings.getMapName()==null){
				onRequestBrowseLibrary(true);
			}else{
				initializeMapView(MapUri.create(MapSettings.getMapName()));
			}
		}
		
	}

	private void initializeMapView(Uri uri) {
		mInitTask = new InitTask();
		mInitTask.execute(uri);
	}

	protected void onPause() {
		onSaveScroll();
		super.onPause();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_BROWSE_LIBRARY:
			if(resultCode == RESULT_OK){
				Uri uri = data.getData();
				if(uri!=null){
					initializeMapView(uri);
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

		menu.add(0, MAIN_MENU_TIME, 	3, R.string.menu_time ).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, MAIN_MENU_STATION, 	4, R.string.menu_station).setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
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

	private void onSaveScroll() {
		if(mModel!=null && mMapView!=null && MapSettings.getMapName()!=null){
			//MapSettings.saveScrollPosition(this, mMapView.getScrollCenter());
		}
	}

	private void onRestoreScroll() {
		if(mModel!=null && mMapView!=null && MapSettings.getMapName()!=null){
			Point position  = MapSettings.loadScrollPosition(this);
			if(position == null){
				position = new Point(mModel.getWidth()/2 , mModel.getHeight()/2);
			}			
			//mMapView.setScrollCenter(position.x, position.y);
		}
	}

	private void onRequestBrowseLibrary(boolean setNoMapLoadingView) {
		if(setNoMapLoadingView){
			setContentView(R.layout.browse_map_empty);
		}
		Intent browseLibrary = new Intent(this, BrowseLibrary.class);
		if(MapSettings.getMapName()!=null){
			browseLibrary.setData(MapUri.create(MapSettings.getMapName() ));
		}
		startActivityForResult(browseLibrary, REQUEST_BROWSE_LIBRARY);
	}

	private void onUpdateTitle()
	{
		if(mModel==null){
			setTitle(R.string.app_name);
		}else{
			setTitle(String.format("%s - %s", getString(R.string.app_name), mModel.getCityName() ) );
		}
	}

	private final float[] ZOOMS = new float[] {2.0f, 1.0f, 0.8f, 0.6f, 0.4f, 0.2f };
	private final int[]   STEPS = new int[] {20, 10, 8, 6, 4, 2};
	private final int MIN_ZOOM_LEVEL = 0;
	private final int START_ZOOM_LEVEL = 1;
	private final int MAX_ZOOM_LEVEL = 5;
	private int mZoom = START_ZOOM_LEVEL;
	
	private void onZoomIn(){
		mZoom = Math.max(mZoom-1, MIN_ZOOM_LEVEL);
		mZoomControls.setIsZoomInEnabled(mZoom > MIN_ZOOM_LEVEL);
		mZoomControls.setIsZoomOutEnabled(mZoom < MAX_ZOOM_LEVEL);
		mMapView.setScale(ZOOMS[mZoom], STEPS[mZoom]);
	}
	
	private void onZoomOut(){
		mZoom = Math.min(mZoom+1, MAX_ZOOM_LEVEL);
		mZoomControls.setIsZoomInEnabled(mZoom > MIN_ZOOM_LEVEL);
		mZoomControls.setIsZoomOutEnabled(mZoom < MAX_ZOOM_LEVEL);
		mMapView.setScale(ZOOMS[mZoom], STEPS[mZoom]);
	}
	
	private class InitTask extends AsyncTask<Uri, Void, Model>
	{

		@Override
		protected void onPreExecute() {
			setContentView(R.layout.global_wait);
			super.onPreExecute();
		}
		
		@Override
		protected Model doInBackground(Uri... params) {
			Uri mapUri = params[0];
			try {
				return ModelBuilder.loadModel(MapSettings.getMapFileName(mapUri));
			} catch (Exception e) {
				return null;
			}
		}
		
		@Override
		protected void onCancelled() {
			setContentView(R.layout.browse_map_empty);
			super.onCancelled();
		}
		
		@Override
		protected void onPostExecute(Model result) {
			if(result != null){
				mModel = result;
				setContentView(R.layout.browse_vector_map_main);

				mMapView = (VectorMapView)findViewById(R.id.browse_vector_map_view);
				mMapView.setModel(mModel);
				
				mZoomControls = (ZoomControls)findViewById(R.id.browse_vector_map_zoom);
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
				
				onRestoreScroll();
				onUpdateTitle();
			}else{
				
			}
			super.onPostExecute(result);
		}
		
	}
	
	
}
