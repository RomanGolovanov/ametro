package com.ametro;

import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ametro.model.IProgressUpdate;
import com.ametro.model.TransportMap;
import com.ametro.model.TransportMapBuilder;
import com.ametro.model.TileManager;

public class CreateMapCache extends Activity implements IProgressUpdate {
	
	private TextView mCachingMapText;
	private int mProgress;
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
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
	
	private void clearScroll(String mapName) {
		if(mapName!=null){
			SharedPreferences preferences = getSharedPreferences("aMetro",0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.remove(BrowseTileMap.PREFERENCE_SCROLL_POSITION + "_" + mapName);
			editor.commit();
		}
	}

	
	private final Runnable mReturnOk = new Runnable() {
		public void run() {
			setResult(RESULT_OK, getIntent());
			finish();
		}
	};

	private final Runnable mHandleException = new Runnable() {
		public void run() {
			Toast.makeText(CreateMapCache.this, "Map caching error: " + failReason.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			setResult(RESULT_OK, getIntent());
			finish();
		}
	};

	private final Thread mCreateCache = new Thread() {
		public void run() {
			Uri uri = getIntent().getData();
			String mapName = MapUri.getMapName(uri);
			try {
				TransportMap map = TransportMapBuilder.Create(MapSettings.CATALOG_PATH, mapName, MapSettings.DEFAULT_MAP);
				TileManager.recreate(map, CreateMapCache.this);
				clearScroll(mapName);
			} catch (IOException e) {
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
