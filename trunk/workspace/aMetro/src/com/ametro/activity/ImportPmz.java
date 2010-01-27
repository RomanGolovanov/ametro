package com.ametro.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.ametro.MapSettings;
import com.ametro.R;
import com.ametro.model.MapBuilder;
import com.ametro.model.Model;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class ImportPmz extends Activity {

	private final Handler mHandler = new Handler();
	private Throwable failReason = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waiting);
		
		mImport.start();
	}
	
	private final Runnable mReturnOk = new Runnable() {
		public void run() {
			setResult(RESULT_OK, null);
			finish();
		}
	};

	private final Runnable mHandleException = new Runnable() {
		public void run() {
			Toast.makeText(ImportPmz.this, "Import failed: " + failReason.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED, null);
			finish();
		}
	};

	private final Thread mImport = new Thread() {
		public void run() {
			try {
				File dir = new File(MapSettings.IMPORT_PATH);
				String[] files = dir.list(new FilenameFilter() {
					@Override
					public boolean accept(File f, String filename) {
						return filename.endsWith(MapSettings.PMZ_FILE_TYPE);
					}
				}); 
				if(files!=null){
					for(int i = 0; i < files.length; i++){
						String fileName = files[i].replace(".pmz", "");
						Model map = MapBuilder.ImportPmz(MapSettings.IMPORT_PATH, fileName, MapSettings.DEFAULT_MAP);
						MapBuilder.saveModel(map);
					}
				}				
				
			} catch (IOException e) {
				Log.e("aMetro","Failed import map", e);
				failReason = e;
				mHandler.post(mHandleException);
			}			
			mHandler.post(mReturnOk);
		}
	};
	
}
