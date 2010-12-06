/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.ui;

import static org.ametro.app.Constants.STATION_FROM_ID;
import static org.ametro.app.Constants.STATION_TO_ID;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.model.Model;
import org.ametro.model.SchemeView;
import org.ametro.model.SchemeView.TransportCollection;
import org.ametro.model.StationView;
import org.ametro.model.route.RouteBuilder;
import org.ametro.model.route.RouteContainer;
import org.ametro.model.route.RouteParameters;
import org.ametro.ui.adapters.StationListAdapter;
import org.ametro.util.CollectionUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class RouteCreateActivity extends Activity implements OnClickListener,
		AnimationListener {

	private Model mModel;
	private SchemeView mMapView;

	private int mDelayMode = 0;
	private TransportCollection mTransports;
	
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
    private final int MAIN_MENU_TRANSPORTS = 3;
    private final int MAIN_MENU_TIME = 4;
	
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MAIN_MENU_SWAP, Menu.NONE, R.string.menu_swap).setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, MAIN_MENU_FAVORITES, Menu.NONE, R.string.menu_favorites).setIcon(R.drawable.icon_btn_star);
        menu.add(0, MAIN_MENU_TRANSPORTS, Menu.NONE, R.string.menu_transports).setIcon(android.R.drawable.ic_menu_agenda);
        menu.add(0, MAIN_MENU_TIME, Menu.NONE, R.string.menu_time).setIcon(android.R.drawable.ic_menu_today);
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
    	final Point[] routes = MapViewActivity.Instance.getFavoriteRoutes();
    	menu.findItem(MAIN_MENU_FAVORITES).setEnabled(!(routes== null || routes.length == 0));
    	menu.findItem(MAIN_MENU_TIME).setEnabled(mModel.delays!=null && mModel.delays.length>0);
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MAIN_MENU_SWAP:
            swapStations();
   			break;
        case MAIN_MENU_FAVORITES:
   			startActivityForResult(new Intent(this,FavoriteRouteListActivity.class), REQUEST_ROUTE);
   			break;
        case MAIN_MENU_TRANSPORTS:
			showSelectTransportDialog();
   			break;
        case MAIN_MENU_TIME:
        	showSelectTimeDialog();
   			break;
        }	
        	
		return super.onOptionsItemSelected(item);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_create);

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
		
		mMapView = MapViewActivity.Instance.getMapView();
		mModel = mMapView.owner;
		if(mModel.delays!=null && mModel.delays.length>0){
			mDelayMode = 0;
		}else{
			mDelayMode = -1;
		}

		mTransports = mMapView.getTransportCollection(this);
		
		StationView[] stations = mMapView.getStationArray(false);
		
		StationListAdapter fromAdapter = new StationListAdapter(this, stations, mMapView); 
		StationListAdapter toAdapter = new StationListAdapter(this, stations, mMapView); 
		
		fromAdapter.setTextColor(Color.BLACK);
		toAdapter.setTextColor(Color.BLACK);
		
		mFromText.setAdapter(fromAdapter);
		mToText.setAdapter(toAdapter);
		
		mFromText.setSelectAllOnFocus(true);
		mToText.setSelectAllOnFocus(true);

		final RouteContainer routes = MapViewActivity.Instance.getNavigationRoute();
		if(routes!=null){
			StationView fromStation = mMapView.findViewByStationId( routes.getStationFromId() );
			StationView toStation = mMapView.findViewByStationId( routes.getStationToId() );
			if(fromStation!=null && toStation!=null){
				mFromText.setText( StationListAdapter.getStationName(mMapView, fromStation) );
				mToText.setText( StationListAdapter.getStationName(mMapView, toStation) );
			}
		}else{
			final StationView station = MapViewActivity.Instance.getCurrentStation();
			if(station!=null && mMapView.hasConnections(station)){
				mFromText.setText( StationListAdapter.getStationName(mMapView, station) );
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
			Intent data = new Intent(this, StationListActivity.class);
			StationView from = getStationByName(mFromText.getText().toString());
			if(from!=null){
				data.putExtra(Constants.STATION_ID, from.id);
			}
			startActivityForResult(data, REQUEST_STATION_FROM);
		}
		if(v==mToButton){
			Intent data = new Intent(this, StationListActivity.class);
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
   			startActivityForResult(new Intent(this,FavoriteRouteListActivity.class), REQUEST_ROUTE);
		}
	}

	private void showSelectTimeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.msg_time_of_day);
		builder.setIcon(android.R.drawable.ic_menu_today);

		final int[] delays = mModel.delays;
		final String[] names = CollectionUtil.join(
				Model.getLocalizedStrings(mModel, delays), 
				new String[]{getText(R.string.create_route_without_time).toString()}
				);  
		final int noDelay = names.length-1;
		
		builder.setCancelable(true);
		builder.setSingleChoiceItems(names, mDelayMode!=-1 ? mDelayMode : noDelay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(which!=noDelay){
					mDelayMode = which;
				}else{
					mDelayMode = -1;
				}
				dialog.dismiss();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	private void showSelectTransportDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.msg_select_transports);
		final TransportCollection coll = new TransportCollection(mTransports);
		
		builder.setMultiChoiceItems(coll.getNames() , coll.getStates(), new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				coll.setState(which, isChecked);
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mTransports = coll;
			}
		});
		builder.setNegativeButton(android.R.string.cancel,null);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	private void createRoute(StationView from, StationView to) {
		if (from != null && to != null && mMapView.hasConnections(from) && mMapView.hasConnections(to)) {
			mCreateRouteTask = new CreateRouteTask();
			mCreateRouteTask.execute(from.stationId, to.stationId);
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

	public class CreateRouteTask extends AsyncTask<Integer, Void, RouteContainer> {

		private ProgressDialog mWaitDialog;

		protected RouteContainer doInBackground(Integer... stations) {
			int len = stations.length;
			int from = stations[0];
			int to = stations[len-1];
			int[] include = new int[len-2];
			for(int i = 1;i<len-1;i++){
				include[i] = stations[i];
			}
			int[] exclude = new int[0];
			int[] transports = mTransports.getCheckedTransports();
			
			RouteParameters routeParameters = new RouteParameters(from, to, include, exclude, RouteBuilder.ROUTE_OPTION_ALL, transports, mDelayMode);
			return RouteBuilder.createRoutes(mMapView.owner, routeParameters);
		}

		protected void onCancelled() {
			mWaitDialog.dismiss();
			super.onCancelled();
		}

		protected void onPreExecute() {
			closePanel();
			mWaitDialog = ProgressDialog.show(RouteCreateActivity.this,
					getString(R.string.create_route_wait_title),
					getString(R.string.create_route_wait_text), true);
			super.onPreExecute();
		}

		protected void onPostExecute(RouteContainer result) {
			super.onPostExecute(result);
			mWaitDialog.dismiss();
			if (result.hasRoutes()) {
				MapViewActivity.Instance.setNavigationRoute(result);
//				long secs = result.getDefaultRoute().getLength();
//				secs = ( secs/60 + (secs%60 == 0 ? 0 : 1) ) * 60;
//				Date date = new Date(secs * 1000);
//				String msg = getString(R.string.msg_route_time) + " " + String.format(getString(R.string.route_time_format), DateUtil.getDateUTC(date, "HH"), DateUtil.getDateUTC(date, "mm"));
//				Toast.makeText(MapViewActivity.Instance, msg, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MapViewActivity.Instance, getString(R.string.msg_route_not_found), Toast.LENGTH_SHORT).show();
				MapViewActivity.Instance.setNavigationRoute(null);
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
			StationView station = mMapView.getStationViewByDisplayName(lineName, stationName);
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
			RouteCreateActivity.this.finish();
		} else {
			mExitPending = true;
			closePanel();
		}
	}

}
