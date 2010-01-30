package com.ametro.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ametro.MapSettings;
import com.ametro.MapUri;
import com.ametro.R;
import com.ametro.libs.IProgressUpdate;
import com.ametro.model.MapBuilder;
import com.ametro.model.Model;
import com.ametro.model.ModelTileManager;

public class RenderMap extends Activity implements IProgressUpdate {
	
	private TextView mCachingMapText;
	private int mProgress;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_caching);
		mCachingMapText = (TextView)findViewById(R.id.cachingMapText);
		mCreateCache.start();
	}

	private final Handler mHandler = new Handler();
	private Throwable failReason = null;

	private final Runnable mUpdateProgress = new Runnable() {
		public void run() {
			String text = String.format("Create map image (%s%% completed)", 
					Integer.toString(mProgress) );
			mCachingMapText.setText(text);
		}
	};
	
	private final Runnable mReturnOk = new Runnable() {
		public void run() {
			setResult(RESULT_OK, getIntent());
			finish();
		}
	};

	private final Runnable mHandleException = new Runnable() {
		public void run() {
			Toast.makeText(RenderMap.this, "Map loading error: " + failReason.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED, getIntent());
			finish();
		}
	};

	private final Thread mCreateCache = new Thread() {
		public void run() {
			Uri uri = getIntent().getData();
			String mapName = MapUri.getMapName(uri);
			try {
				Model map = MapBuilder.loadModel(MapSettings.getMapFileName(mapName));
				ModelTileManager.recreate(map, RenderMap.this);
				MapSettings.clearScrollPosition(RenderMap.this, mapName);
			} catch (Exception e) {
				Log.e("aMetro","Failed creating map cache for " + mapName, e);
				failReason = e;
				mHandler.post(mHandleException);
			}			
			mHandler.post(mReturnOk);
		}
	};

	@Override
	public void update(int percent) {
		mProgress = percent;
		mHandler.post(mUpdateProgress);
	}

}
