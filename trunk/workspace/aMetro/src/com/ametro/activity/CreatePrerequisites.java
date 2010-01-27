package com.ametro.activity;

import java.io.File;
import java.io.IOException;

import com.ametro.MapSettings;
import com.ametro.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class CreatePrerequisites extends Activity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waiting);
		mExecute.start();
	}
	
	private final Handler mHandler = new Handler();
	private Throwable failReason = null;

	private final Runnable mReturnOk = new Runnable() {
		public void run() {
			setResult(RESULT_OK);
			finish();
		}
	};

	private final Runnable mHandleException = new Runnable() {
		public void run() {
			setResult(RESULT_CANCELED);
			Toast.makeText(CreatePrerequisites.this,"Create prerequisites error: " + failReason.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	};

	private final Thread mExecute = new Thread() {
		public void run() {
			try {
				createDirectory(MapSettings.CATALOG_PATH);
				createDirectory(MapSettings.IMPORT_PATH);
				createDirectory(MapSettings.CACHE_PATH);
				createFile(MapSettings.ROOT_PATH + MapSettings.NO_MEDIA_TAG);
			} catch (Throwable e) {
				setResult(RESULT_CANCELED);
				failReason = e;
				mHandler.post(mHandleException);
			}			
			mHandler.post(mReturnOk);
		}
	};	
	
	private void createFile(String path) throws IOException{
		File f = new File(path);
		f.createNewFile();
	}
	private void createDirectory(String path){
		File f = new File(path);
		f.mkdirs();
	}
}
