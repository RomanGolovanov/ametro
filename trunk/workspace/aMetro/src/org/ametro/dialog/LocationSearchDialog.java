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
package org.ametro.dialog;

import java.util.List;

import org.ametro.Constants;
import org.ametro.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LocationSearchDialog extends Activity implements LocationListener {

	public static final String LOCATION = "LOCATION";
	public static final int WAIT_DELAY = 10000;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_location_main);
		mPrivateHandler.postDelayed(mReturnResult, WAIT_DELAY);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	protected void onResume() {
		mProviders = mLocationManager.getAllProviders();
		for (String provider : mProviders) {
			if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.INFO)) {
				Log.i(Constants.LOG_TAG_MAIN,
						"Register listener for location provider " + provider);
			}
			mLocationManager.requestLocationUpdates(provider, 0, 0, this);
		}
		super.onResume();
	}

	protected void onPause() {
		if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.INFO)) {
			Log.i(Constants.LOG_TAG_MAIN,
					"Remove listeners for location providers");
		}		
		mLocationManager.removeUpdates(this);
		super.onPause();
	}

	private List<String> mProviders;

	private LocationManager mLocationManager;
	private Location mLocation;

	private Object mMutex = new Object();
	private Handler mPrivateHandler = new Handler();
	private Runnable mReturnResult = new Runnable() {
		public void run() {
			if (mLocation != null) {
				Intent data = new Intent();
				data.putExtra(LOCATION, mLocation);
				setResult(RESULT_OK, data);
			} else {
				setResult(RESULT_CANCELED);
			}
			finish();
		}
	};

	public void onLocationChanged(Location location) {
		if (location != null) {
			if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.INFO)) {
				Log.i(Constants.LOG_TAG_MAIN,
						"Received location change from provider "
								+ location.getProvider().toUpperCase());
			}
			if (mLocation == null) {
				synchronized (mMutex) {
					if (mLocation == null) {
						mLocation = new Location(location);
					}
					mPrivateHandler.post(mReturnResult);
				}
			}
		}
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle bundle) {
	}

}
