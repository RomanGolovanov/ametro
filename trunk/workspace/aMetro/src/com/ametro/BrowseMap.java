package com.ametro;


import com.ametro.model.Model;
import com.ametro.model.ModelBuilder;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class BrowseMap extends Activity {

	private Model mModel; 
	private MapImageView mMapImageView;
	private boolean mIsInitialized = false;
	
	private String mPackageFileName = "/sdcard/ametro/moscow.pmz";
	private String mPackageMapName = "Metro.map";

	private final Handler mHandler = new Handler();	


	
	private final Runnable mUpdateContentView = new Runnable() {
		public void run() {
			mMapImageView = new MapImageView(getApplicationContext(), mModel);
			mMapImageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
			setProgressBarVisibility(false);
			setContentView(mMapImageView);
		}
	};    

	private final Thread mMapLoader = new Thread(){
		public void run() {
			try {
				//mModel = ModelBuilder.Create("/sdcard/ametro/spb.pmz", "Metro.map");
				mModel = ModelBuilder.Create(mPackageFileName, mPackageMapName);
				Thread.sleep(5000);
				mIsInitialized = true;
				mHandler.post(mUpdateContentView);
			} catch (Exception e) {
				Log.e("aMetro", "Failed to load map", e);
				handleException(e);
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!mIsInitialized){
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			setContentView(R.layout.logo);
			setProgressBarVisibility(true);
			mMapLoader.start();
			mIsInitialized = true;
		}
	}

	@Override
	protected void onStop() {
		mIsInitialized = false;
		super.onStop();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void handleException(Exception e) {
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		finish();
	}


}