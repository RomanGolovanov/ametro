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

import org.ametro.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;

public class MediaUnmountedActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_unmounted);
	}

	protected void onResume() {
		startWatchingExternalStorage();
		super.onResume();
	}
	
	protected void onPause() {
		stopWatchingExternalStorage();
		super.onPause();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mExternalStorageAvailable && mExternalStorageWriteable){
				setResult(RESULT_OK);
			}else{
				setResult(RESULT_CANCELED);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void updateExternalStorageState() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }
	    
	    if((mExternalStorageAvailable && mExternalStorageWriteable)){
	    	setResult(RESULT_OK);
	    	finish();
	    }
	}

	private void startWatchingExternalStorage() {
	    mExternalStorageReceiver = new BroadcastReceiver() {
	        public void onReceive(Context context, Intent intent) {
	            updateExternalStorageState();
	        }
	    };
	    IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	    registerReceiver(mExternalStorageReceiver, filter);
	    updateExternalStorageState();
	}

	private void stopWatchingExternalStorage() {
	    unregisterReceiver(mExternalStorageReceiver);
	}	
	
	private BroadcastReceiver mExternalStorageReceiver;
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;	
	
}
