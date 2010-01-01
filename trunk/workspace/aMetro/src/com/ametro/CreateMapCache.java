package com.ametro;

import java.io.IOException;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.ametro.model.IProgressUpdate;
import com.ametro.model.Model;
import com.ametro.model.ModelBuilder;
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
			try {
				Uri uri = getIntent().getData();
				String mapName = MapUri.getMapName(uri);
				Model model = ModelBuilder.Create(MapSettings.CATALOG_PATH, mapName, MapSettings.DEFAULT_MAP);
				TileManager.recreate(model, CreateMapCache.this);
			} catch (IOException e) {
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
