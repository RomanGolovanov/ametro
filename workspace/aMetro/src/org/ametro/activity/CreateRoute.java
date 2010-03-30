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

import static org.ametro.Constants.STATION_FROM_ID;
import static org.ametro.Constants.STATION_TO_ID;

import org.ametro.Constants;
import org.ametro.R;
import org.ametro.adapter.StationListAdapter;
import org.ametro.model.MapView;
import org.ametro.model.Route;
import org.ametro.model.StationView;
import org.ametro.util.DateUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class CreateRoute extends Activity implements OnClickListener,
		AnimationListener {

	private MapView mMapView;

	private AutoCompleteTextView mFromText;
	private AutoCompleteTextView mToText;

	private Button mCreateButton;
	private Button mFavoritesButton;

	private ImageButton mFromButton;
	private ImageButton mToButton;
	
	private CreateRouteTask mCreateRouteTask;
	
	private View mPanel;
	private Animation mPanelAnimation;

	private boolean mExitPending;

	private final static int REQUEST_STATION_FROM = 1; 
	private final static int REQUEST_STATION_TO = 2;
	private final static int REQUEST_ROUTE = 3;

    private final int MAIN_MENU_SWAP = 1;
    private final int MAIN_MENU_FAVORITES = 2;
	
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MAIN_MENU_SWAP, 0, R.string.menu_swap).setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, MAIN_MENU_FAVORITES, 1, R.string.menu_favorites).setIcon(R.drawable.star_48);
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
    	final Point[] routes = BrowseVectorMap.Instance.getFavoriteRoutes();
    	menu.findItem(MAIN_MENU_FAVORITES).setEnabled(!(routes== null || routes.length == 0));
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MAIN_MENU_SWAP:
            swapStations();
            return true;
        case MAIN_MENU_FAVORITES:
   			startActivityForResult(new Intent(this,FavoriteRouteList.class), REQUEST_ROUTE);
        }		
		return super.onOptionsItemSelected(item);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_route);

		mExitPending = false;

		mPanel = (View) findViewById(R.id.create_route_panel);

		mCreateButton = (Button) findViewById(R.id.create_route_create_button);
		mFavoritesButton = (Button) findViewById(R.id.create_route_favorites_button);
		mFromButton = (ImageButton) findViewById(R.id.create_route_from_button);
		mToButton = (ImageButton) findViewById(R.id.create_route_to_button);
		
		
		mCreateButton.setOnClickListener(this);
		mFavoritesButton.setOnClickListener(this);
		mFromButton.setOnClickListener(this);
		mToButton.setOnClickListener(this);

		mFromText = (AutoCompleteTextView) findViewById(R.id.create_route_from_text);
		mToText = (AutoCompleteTextView) findViewById(R.id.create_route_to_text);
		
		mMapView = BrowseVectorMap.Instance.getMapView();
		
		StationListAdapter fromAdapter = new StationListAdapter(this, mMapView.stations, mMapView); 
		StationListAdapter toAdapter = new StationListAdapter(this, mMapView.stations, mMapView); 
		
		fromAdapter.setTextColor(Color.BLACK);
		toAdapter.setTextColor(Color.BLACK);
		
		mFromText.setAdapter(fromAdapter);
		mToText.setAdapter(toAdapter);
		

		final Route route = BrowseVectorMap.Instance.getNavigationRoute();
		if(route!=null){
			StationView fromStation = route.getStationFrom();
			StationView toStation = route.getStationTo();
			mFromText.setText( StationListAdapter.getStationName(mMapView, fromStation) );
			mToText.setText( StationListAdapter.getStationName(mMapView, toStation) );
			mFromText.setSelectAllOnFocus(true);
			mToText.setSelectAllOnFocus(true);
		}else{
			final StationView station = BrowseVectorMap.Instance.getCurrentStation();
			if(station!=null){
				mFromText.setText( StationListAdapter.getStationName(mMapView, station) );
				mFromText.setSelectAllOnFocus(true);
			}
		}
		
	}

	protected void onStop() {
		if (mCreateRouteTask != null) {
			mCreateRouteTask.cancel(true);
			mCreateRouteTask = null;
		}
		super.onStop();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_STATION_FROM:
			if(resultCode == RESULT_OK){
				int id = data.getIntExtra(Constants.STATION_ID, -1);
				if(id!=-1){
					StationView station = mMapView.stations[id];
					mFromText.setText( StationListAdapter.getStationName(mMapView, station) );				
				}
			}
			break;
		case REQUEST_STATION_TO:
			if(resultCode == RESULT_OK){
				int id = data.getIntExtra(Constants.STATION_ID, -1);
				if(id!=-1){
					StationView station = mMapView.stations[id];
					mToText.setText( StationListAdapter.getStationName(mMapView, station) );				
				}
			}
			break;
		case REQUEST_ROUTE:
			if(resultCode == RESULT_OK){
				int fromId = data.getIntExtra(STATION_FROM_ID, -1);
				int toId = data.getIntExtra(STATION_TO_ID, -1);
				if(fromId!=-1 && toId!=-1){
					StationView from = mMapView.stations[fromId];
					mFromText.setText( StationListAdapter.getStationName(mMapView, from) );				
					StationView to = mMapView.stations[toId];
					mToText.setText( StationListAdapter.getStationName(mMapView, to) );
					
					createRoute(from, to);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onClick(View v) {
		if(v==mFromButton){
			Intent data = new Intent(this, SelectStation.class);
			StationView from = getStationByName(mFromText.getText().toString());
			if(from!=null){
				data.putExtra(Constants.STATION_ID, from.id);
			}
			startActivityForResult(data, REQUEST_STATION_FROM);
		}
		if(v==mToButton){
			Intent data = new Intent(this, SelectStation.class);
			StationView to = getStationByName(mToText.getText().toString());
			if(to!=null){
				data.putExtra(Constants.STATION_ID, to.id);
			}
			startActivityForResult(data, REQUEST_STATION_TO);
		}
		if (v == mCreateButton) {
			StationView from = getStationByName(mFromText.getText().toString());
			StationView to = getStationByName(mToText.getText().toString());
			createRoute(from, to);
		}
		if (v == mFavoritesButton){
   			startActivityForResult(new Intent(this,FavoriteRouteList.class), REQUEST_ROUTE);
		}
	}

	private void createRoute(StationView from, StationView to) {
		if (from != null && to != null) {
			mCreateRouteTask = new CreateRouteTask();
			mCreateRouteTask.execute(from, to);
		}
	}

	private void swapStations() {
		Editable swap = mFromText.getText();
		mFromText.setText(mToText.getText());
		mToText.setText(swap);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finishActivity();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onAnimationEnd(Animation anim) {
		if (anim == mPanelAnimation && mExitPending) {
			finish();
		}
	}

	public void onAnimationRepeat(Animation anim) {
	}

	public void onAnimationStart(Animation anim) {
	}

	public class CreateRouteTask extends
			AsyncTask<StationView, Void, Route> {

		private MapView mMap;
		private ProgressDialog mWaitDialog;

		protected Route doInBackground(
				StationView... stations) {
			StationView from = stations[0];
			StationView to = stations[1];
			Route route = new Route(mMap, from.id, to.id);
			route.findRoute();
			return route;
		}

		protected void onCancelled() {
			mWaitDialog.dismiss();
			super.onCancelled();
		}

		protected void onPreExecute() {
			mMap = CreateRoute.this.mMapView;
			closePanel();
			mWaitDialog = ProgressDialog.show(CreateRoute.this,
					getString(R.string.create_route_wait_title),
					getString(R.string.create_route_wait_text), true);
			super.onPreExecute();
		}

		protected void onPostExecute(Route result) {
			super.onPostExecute(result);
			mWaitDialog.dismiss();
			if (result.hasRoute()) {
				BrowseVectorMap.Instance.setNavigationRoute(result);
				String msg = getString(R.string.msg_route_time) + " " + DateUtil.getTimeHHMM(result.getTime());
				Toast.makeText(BrowseVectorMap.Instance, msg, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(BrowseVectorMap.Instance, getString(R.string.msg_route_not_found),
						Toast.LENGTH_SHORT).show();
				BrowseVectorMap.Instance.setNavigationRoute(null);
			}
			finishActivity();
		}
	}

	private StationView getStationByName(String text) {
		int startBracket = text.indexOf('(');
		int endBracked = text.indexOf(')');
		if (startBracket != -1 && endBracked != -1 && startBracket < endBracked) {
			String stationName = text.substring(0, startBracket - 1);
			String lineName = text.substring(startBracket + 1, endBracked);
			StationView station = mMapView.getStationView(lineName, stationName);
			return station;
		}
		return null;
	}

	private void closePanel() {
		mPanelAnimation = new TranslateAnimation(0, 0, 0, -mPanel.getHeight());
		mPanelAnimation.setAnimationListener(this);
		mPanelAnimation.setDuration(350);
		mPanel.startAnimation(mPanelAnimation);
		mPanel.setVisibility(View.INVISIBLE);
	}

	private void finishActivity() {
		if (mPanel.getVisibility() == View.INVISIBLE) {
			CreateRoute.this.finish();
		} else {
			mExitPending = true;
			closePanel();
		}
	}

}
