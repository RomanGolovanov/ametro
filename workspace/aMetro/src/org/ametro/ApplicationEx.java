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
package org.ametro;

import java.io.FileOutputStream;

import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.directory.CityDirectory;
import org.ametro.directory.CountryDirectory;
import org.ametro.directory.ImportDirectory;
import org.ametro.directory.ImportMapDirectory;
import org.ametro.directory.ImportTransportDirectory;
import org.ametro.directory.StationDirectory;
import org.ametro.jni.Natives;
import org.ametro.service.CatalogService;
import org.ametro.util.FileUtil;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ApplicationEx extends Application {
	
	public CountryDirectory getCountryDirectory() {
		if (mCountryDirectory == null) {
			synchronized (ApplicationEx.class) {
				if (mCountryDirectory == null) {
					mCountryDirectory = new CountryDirectory(this);
				}
			}
		}
		return mCountryDirectory;
	}

	public CityDirectory getCityDirectory() {
		if (mCityDirectory == null) {
			synchronized (ApplicationEx.class) {
				if (mCityDirectory == null) {
					mCityDirectory = new CityDirectory(this);
				}
			}
		}
		return mCityDirectory;
	}

	public ImportTransportDirectory getImportTransportDirectory() {
		if (mImportTransportDirectory == null) {
			synchronized (ApplicationEx.class) {
				if (mImportTransportDirectory == null) {
					mImportTransportDirectory = new ImportTransportDirectory(this);
				}
			}
		}
		return mImportTransportDirectory;
	}
	
	public ImportMapDirectory getImportMapDirectory() {
		if (mImportMapDirectory == null) {
			synchronized (ApplicationEx.class) {
				if (mImportMapDirectory == null) {
					mImportMapDirectory = new ImportMapDirectory(this);
				}
			}
		}
		return mImportMapDirectory;
	}
	
	public ImportDirectory getImportDirectory() {
		if (mImportDirectory == null) {
			synchronized (ApplicationEx.class) {
				if (mImportDirectory == null) {
					mImportDirectory = new ImportDirectory(this);
				}
			}
		}
		return mImportDirectory;
	}

	public StationDirectory getStationDirectory() {
		if (mStationDirectory == null) {
			synchronized (ApplicationEx.class) {
				if (mStationDirectory == null) {
					mStationDirectory = new StationDirectory(
							getApplicationContext());
				}
			}
		}
		return mStationDirectory;
	}

	public CatalogStorage getCatalogStorage() {
		if (mStorage == null) {
			synchronized (ApplicationEx.class) {
				if (mStorage == null) {
					mStorage = new CatalogStorage(this);
				}
			}
		}
		return mStorage;
	}
	
	public void onCreate() {
		if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.INFO)) {
			Log.i(Constants.LOG_TAG_MAIN, "aMetro application started");
		}
		mInstance = this;
		//getCatalogStorage();
		FileUtil.touchDirectory(Constants.ROOT_PATH);
		FileUtil.touchDirectory(Constants.LOCAL_CATALOG_PATH);
		FileUtil.touchDirectory(Constants.IMPORT_CATALOG_PATH);
		FileUtil.touchDirectory(Constants.TEMP_CATALOG_PATH);
		FileUtil.touchDirectory(Constants.ICONS_PATH);
		FileUtil.touchFile(Constants.NO_MEDIA_FILE);

		extractEULA(this);
		
		Natives.Initialize();
		
		//startService(new Intent(this, CatalogService.class));
		super.onCreate();
	}

	public static void extractEULA(Context context) {
		if(!Constants.EULA_FILE.exists()){
			try {
				FileUtil.writeToStream( context.getAssets().open("gpl.html") , new FileOutputStream(Constants.EULA_FILE), true);
			} catch (Exception e) {
				// do nothing!
			}
		}
	}

	public void onTerminate() {
		stopService(new Intent(this, CatalogService.class));
		shutdownHttpClient();
		super.onTerminate();
	}

	public static ApplicationEx getInstance() {
		return mInstance;
	}

	public HttpClient getHttpClient() {
		if(mHttpClient==null){
			synchronized (ApplicationEx.class) {
				if(mHttpClient==null){
					mHttpClient = createHttpClient();
				}
			}
		}
		return mHttpClient;
	}

	private HttpClient createHttpClient() {
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(Constants.LOG_TAG_MAIN, "Create HTTP client");
		}
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
		return new DefaultHttpClient(conMgr, params);
	}
	
	private void shutdownHttpClient() {
		if (mHttpClient != null && mHttpClient.getConnectionManager() != null) {
			mHttpClient.getConnectionManager().shutdown();
		}
	}

	private HttpClient mHttpClient;
	private static ApplicationEx mInstance;

	private StationDirectory mStationDirectory;
	private CatalogStorage mStorage;

	private ImportDirectory mImportDirectory;
	private ImportMapDirectory mImportMapDirectory;
	private ImportTransportDirectory mImportTransportDirectory;
	private CityDirectory mCityDirectory;
	private CountryDirectory mCountryDirectory;
	
	
}
