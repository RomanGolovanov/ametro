package org.ametro.service;

import org.ametro.ApplicationEx;
import org.ametro.Constants;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CatalogService extends Service {

	public IBinder onBind(Intent intent) {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onBind");
		return null;
	}

	public void onCreate() {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onCreate");
		super.onCreate();
	}

	public void onStart(Intent intent, int startId) {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onStart");
		((ApplicationEx)getApplicationContext()).getCatalogStorage();
	}

	public void onDestroy() {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onDestroy");
		super.onDestroy();
	}

}
