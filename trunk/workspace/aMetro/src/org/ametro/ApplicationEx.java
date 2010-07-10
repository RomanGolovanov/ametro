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

import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.tasks.DownloadIconsTask;
import org.ametro.directory.CityDirectory;
import org.ametro.directory.CountryDirectory;
import org.ametro.directory.ImportDirectory;
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
					CatalogStorage instance = new CatalogStorage(
							Constants.LOCAL_CATALOG_STORAGE,
							Constants.LOCAL_CATALOG_PATH,
							Constants.IMPORT_CATALOG_STORAGE,
							Constants.IMPORT_CATALOG_PATH,
							Constants.ONLINE_CATALOG_STORAGE,
							Constants.ONLINE_CATALOG_PATH);
					mStorage = instance;
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
		Natives.Initialize();
		
		startService(new Intent(this, CatalogService.class));
		if(Constants.ICONS_PATH.exists() && Constants.ICONS_PATH.isDirectory())
		{
			String[] files = Constants.ICONS_PATH.list();
			if(files == null || files.length == 0){
				getCatalogStorage().requestTask( new DownloadIconsTask() );
			}
		}else{
			FileUtil.deleteAll(Constants.ICONS_PATH);
			getCatalogStorage().requestTask( new DownloadIconsTask() );
		}
		getCatalogStorage().requestCatalog(CatalogStorage.LOCAL, false);
		getCatalogStorage().requestCatalog(CatalogStorage.IMPORT, false);
		getCatalogStorage().requestCatalog(CatalogStorage.ONLINE, false);
		super.onCreate();
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
	private CityDirectory mCityDirectory;
	private CountryDirectory mCountryDirectory;
	
}
