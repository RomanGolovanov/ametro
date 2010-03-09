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

import org.ametro.R;
import org.ametro.adapter.StationListAdapter;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayRoute;
import org.ametro.model.SubwayStation;
import org.ametro.util.DateUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
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

	private SubwayMap mSubwayMap;

	private AutoCompleteTextView mFromText;
	private AutoCompleteTextView mToText;

	private Button mSwapButton;
	private Button mCreateButton;

	private ImageButton mFromButton;
	private ImageButton mToButton;
	
	private CreateRouteTask mCreateRouteTask;

	private View mPanel;
	private Animation mPanelAnimation;

	private boolean mExitPending;

	private final static int REQUEST_STATION_FROM = 1; 
	private final static int REQUEST_STATION_TO = 2; 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_route);

		mExitPending = false;

		mPanel = (View) findViewById(R.id.create_route_panel);

		mSwapButton = (Button) findViewById(R.id.create_route_swap_button);
		mCreateButton = (Button) findViewById(R.id.create_route_create_button);

		mFromButton = (ImageButton) findViewById(R.id.create_route_from_button);
		mToButton = (ImageButton) findViewById(R.id.create_route_to_button);
		
		mSwapButton.setOnClickListener(this);
		mCreateButton.setOnClickListener(this);
		mFromButton.setOnClickListener(this);
		mToButton.setOnClickListener(this);

		mFromText = (AutoCompleteTextView) findViewById(R.id.create_route_from_text);
		mToText = (AutoCompleteTextView) findViewById(R.id.create_route_to_text);

		mSubwayMap = BrowseVectorMap.Instance.getSubwayMap();
		
		mFromText.setAdapter(new StationListAdapter(this, mSubwayMap.stations, mSubwayMap));
		mToText.setAdapter(new StationListAdapter(this, mSubwayMap.stations, mSubwayMap));
		

		final SubwayRoute route = BrowseVectorMap.Instance.getNavigationRoute();
		if(route!=null){
			SubwayStation fromStation = route.getStationFrom();
			SubwayStation toStation = route.getStationTo();
			mFromText.setText( StationListAdapter.getStationName(mSubwayMap, fromStation) );
			mToText.setText( StationListAdapter.getStationName(mSubwayMap, toStation) );
			mFromText.setSelectAllOnFocus(true);
			mToText.setSelectAllOnFocus(true);
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
				int id = data.getIntExtra(SelectStation.STATION_ID, -1);
				if(id!=-1){
					SubwayStation station = mSubwayMap.stations[id];
					mFromText.setText( StationListAdapter.getStationName(mSubwayMap, station) );				
				}
			}
			break;
		case REQUEST_STATION_TO:
			if(resultCode == RESULT_OK){
				int id = data.getIntExtra(SelectStation.STATION_ID, -1);
				if(id!=-1){
					SubwayStation station = mSubwayMap.stations[id];
					mToText.setText( StationListAdapter.getStationName(mSubwayMap, station) );				
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onClick(View v) {
		if(v==mFromButton){
			Intent data = new Intent(this, SelectStation.class);
			SubwayStation from = getStationByName(mFromText.getText().toString());
			if(from!=null){
				data.putExtra(SelectStation.STATION_ID, from.id);
			}
			startActivityForResult(data, REQUEST_STATION_FROM);
		}
		if(v==mToButton){
			Intent data = new Intent(this, SelectStation.class);
			SubwayStation to = getStationByName(mToText.getText().toString());
			if(to!=null){
				data.putExtra(SelectStation.STATION_ID, to.id);
			}
			startActivityForResult(data, REQUEST_STATION_TO);
		}
		if (v == mSwapButton) {
			Editable swap = mFromText.getText();
			mFromText.setText(mToText.getText());
			mToText.setText(swap);
			mSwapButton.requestFocus();
		}
		if (v == mCreateButton) {
			SubwayStation from = getStationByName(mFromText.getText().toString());
			SubwayStation to = getStationByName(mToText.getText().toString());
			if (from != null && to != null) {
				mCreateRouteTask = new CreateRouteTask();
				mCreateRouteTask.execute(from, to);
			}
		}

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
			AsyncTask<SubwayStation, Void, SubwayRoute> {

		private SubwayMap mMap;
		private ProgressDialog mWaitDialog;

		protected SubwayRoute doInBackground(
				SubwayStation... stations) {
			SubwayStation from = stations[0];
			SubwayStation to = stations[1];
			SubwayRoute route = new SubwayRoute(mMap, from.id, to.id);
			route.findRoute();
			return route;
		}

		protected void onCancelled() {
			mWaitDialog.dismiss();
			super.onCancelled();
		}

		protected void onPreExecute() {
			mMap = CreateRoute.this.mSubwayMap;
			closePanel();
			mWaitDialog = ProgressDialog.show(CreateRoute.this,
					getString(R.string.create_route_wait_title),
					getString(R.string.create_route_wait_text), true);
			super.onPreExecute();
		}

		protected void onPostExecute(SubwayRoute result) {
			super.onPostExecute(result);
			mWaitDialog.dismiss();
			if (result.hasRoute()) {
				BrowseVectorMap.Instance.setNavigationRoute(result);
				String msg = getString(R.string.msg_route_time) + DateUtil.getLongTime(result.getTime());
				Toast.makeText(BrowseVectorMap.Instance, msg, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(BrowseVectorMap.Instance, getString(R.string.msg_route_not_found),
						Toast.LENGTH_SHORT).show();
				BrowseVectorMap.Instance.setNavigationRoute(null);
			}
			finishActivity();
		}

	}

	private SubwayStation getStationByName(String text) {
		int startBracket = text.indexOf('(');
		int endBracked = text.indexOf(')');
		if (startBracket != -1 && endBracked != -1 && startBracket < endBracked) {
			String stationName = text.substring(0, startBracket - 1);
			String lineName = text.substring(startBracket + 1, endBracked);
			SubwayStation station = mSubwayMap
					.getStation(lineName, stationName);
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
