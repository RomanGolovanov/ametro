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
package org.ametro.ui.dialog;

import java.util.List;

import org.ametro.R;
import org.ametro.app.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocationSearchDialog extends Activity implements LocationListener, OnClickListener {

	public static final String LOCATION = "LOCATION";

	private Location mLocation;

	private List<String> mProviders;

	private LocationManager mLocationManager;
	
	private TextView mText;
	private ProgressBar mProgress;
	private Button mCancelButton;
	private Button mSettingsButton;
	
	private static final int REQUEST_ENABLE_LOCATION_SERVICES = 1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.location_search);
		
		mText = (TextView)findViewById(R.id.text);
		mProgress = (ProgressBar)findViewById(R.id.progressbar);
		
		mCancelButton = (Button)findViewById(R.id.btn_cancel);
		mCancelButton.setOnClickListener(this);
		
		mSettingsButton = (Button)findViewById(R.id.btn_settings);
		mSettingsButton.setOnClickListener(this);
		
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		updateMode();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_LOCATION_SERVICES:
			updateMode();
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void updateMode(){
		if(!isLocationProvidersEnabled()){
			mProgress.setVisibility(View.GONE);
			mText.setText(getText(R.string.msg_location_need_enable_providers));
			mSettingsButton.setVisibility(View.VISIBLE);
		}else{
			mProgress.setVisibility(View.VISIBLE);
			mText.setText(getText(R.string.locate_wait_text));
			mSettingsButton.setVisibility(View.GONE);
		}
	}
	
	protected void onResume() {
		bindLocationProviders();
		super.onResume();
	}

	protected void onPause() {
		unbindLocationProviders();
		super.onPause();
	}

	public void onLocationChanged(Location location) {
		if (location != null) {
			if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
				Log.d(Constants.LOG_TAG_MAIN,
						"Received location change from provider "
						+ location.getProvider().toUpperCase());
			}
			if (mLocation == null) {
				if (mLocation == null) {
					mLocation = new Location(location);
				}
				finishWithResult();
			}
		}
	}

	private boolean isLocationProvidersEnabled() {
		mProviders = mLocationManager.getProviders(true);
		return mProviders!=null && mProviders.size() > 0;
	}	

	private void bindLocationProviders() {
		mProviders = mLocationManager.getAllProviders();
		for (String provider : mProviders) {
			if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
				Log.d(Constants.LOG_TAG_MAIN, "Register listener for location provider " + provider);
			}
			mLocationManager.requestLocationUpdates(provider, 0, 0, this);
		}
	}

	private void unbindLocationProviders() {
		if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)) {
			Log.d(Constants.LOG_TAG_MAIN, "Remove listeners for location providers");
		}		
		mLocationManager.removeUpdates(this);
	}

	private void finishWithResult(){
		if (mLocation != null) {
			Intent data = new Intent();
			data.putExtra(LOCATION, mLocation);
			setResult(RESULT_OK, data);
		} else {
			setResult(RESULT_OK);
		}
		finish();
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle bundle) {
	}

	public void onClick(View v) {
		if(v == mCancelButton){
			setResult(RESULT_CANCELED);
			finish();
		}
		if(v == mSettingsButton){
			startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_ENABLE_LOCATION_SERVICES); 
		}
	}

}
