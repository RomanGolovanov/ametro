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
package org.ametro.app;

import static org.ametro.app.Constants.HTTP_CONNECTION_TIMEOUT;
import static org.ametro.app.Constants.HTTP_SOCKET_TIMEOUT;

import java.io.FileOutputStream;

import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.directory.CityDirectory;
import org.ametro.directory.CountryDirectory;
import org.ametro.directory.ImportDirectory;
import org.ametro.directory.ImportMapDirectory;
import org.ametro.directory.ImportTransportDirectory;
import org.ametro.directory.StationDirectory;
import org.ametro.receiver.AlarmReceiver;
import org.ametro.receiver.BootCompletedReceiver;
import org.ametro.receiver.NetworkStateReceiver;
import org.ametro.service.AutoUpdateService;
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
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
//					mStorage.requestCatalog(CatalogStorage.LOCAL, false);
//					mStorage.requestCatalog(CatalogStorage.ONLINE, false);
//					mStorage.requestCatalog(CatalogStorage.IMPORT, false);
				}
			}
		}
		return mStorage;
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
	
	public void onCreate() {
		if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.INFO)) {
			Log.i(Constants.LOG_TAG_MAIN, "aMetro application started");
		}
		mInstance = this;
		mConnectionManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		FileUtil.touchDirectory(Constants.ROOT_PATH);
		FileUtil.touchDirectory(Constants.LOCAL_CATALOG_PATH);
		FileUtil.touchDirectory(Constants.IMPORT_CATALOG_PATH);
		FileUtil.touchDirectory(Constants.TEMP_CATALOG_PATH);
		FileUtil.touchDirectory(Constants.ICONS_PATH);
		FileUtil.touchFile(Constants.NO_MEDIA_FILE);
		extractEULA(this);

		super.onCreate();
	}


	public void onTerminate() {
		stopService(new Intent(this, AutoUpdateService.class));
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

	public boolean isNetworkAvailable(){
		NetworkInfo[] infs = mConnectionManager.getAllNetworkInfo();
		if(infs!=null && infs.length>0){
			for(NetworkInfo inf : infs){
				if(inf.isConnected()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isAutoUpdateNetworkAvailable(){
		boolean isWifiConnected =  mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		boolean isMobileConnected = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
		boolean isConnected = false;
		if(GlobalSettings.isUpdateOnlyByWifi(this)){
			isConnected = isWifiConnected;
		}else if(!GlobalSettings.isUpdateByAnyNetwork(this)){
			isConnected = isWifiConnected || isMobileConnected;
		}else{
			isConnected = isNetworkAvailable();
		}
		return isConnected;
	}
	
	public void invalidateAutoUpdate(){
		if(GlobalSettings.isAutoUpdateIndexEnabled(this)){
			changeBootCompletedReceiverState(true);
			changeAlarmReceiverState(true);
		}else{
			changeBootCompletedReceiverState(false);
			changeAlarmReceiverState(false);
			changeNetworkStateReceiverState(false);
		}
	}
	
	public void changeAlarmReceiverState(boolean enabled) {
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		alarmManager.cancel(pendingIntent);
		if(enabled){
			long interval = (GlobalSettings.getUpdatePeriod(this) == 900) ? AlarmManager.INTERVAL_FIFTEEN_MINUTES : AlarmManager.INTERVAL_DAY;
			alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000*60*2 , interval, pendingIntent);
		}
	}
	
	public void changeNetworkStateReceiverState(boolean enabled){
		PackageManager manager = getPackageManager();
		ComponentName name = new ComponentName(this, NetworkStateReceiver.class);
		int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		manager.setComponentEnabledSetting(name, state, PackageManager.DONT_KILL_APP);
	}

	public void changeBootCompletedReceiverState(boolean enabled){
		PackageManager manager = getPackageManager();
		ComponentName name = new ComponentName(this, BootCompletedReceiver.class);
		int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		manager.setComponentEnabledSetting(name, state, PackageManager.DONT_KILL_APP);
	}
		
	public boolean checkAutoUpdate() {
		if(GlobalSettings.isAutoUpdateIndexEnabled(this)){
			long lastModified = GlobalSettings.getUpdateDate(this);
			long currentDate = System.currentTimeMillis();
			long updateTimeout = GlobalSettings.getUpdatePeriod(this);
			long timeout = (currentDate - lastModified)/1000;
			if(timeout > updateTimeout){
				if(isAutoUpdateNetworkAvailable() && mConnectionManager.getBackgroundDataSetting()){
					changeNetworkStateReceiverState(false);
					startService(new Intent(this, AutoUpdateService.class));
					return true;
				}else{
					changeNetworkStateReceiverState(mConnectionManager.getBackgroundDataSetting());
				}
			}
		}else{
			changeBootCompletedReceiverState(false);
			changeNetworkStateReceiverState(false);
			changeAlarmReceiverState(false);
		}
		return false;
	}

	
	private HttpClient createHttpClient() {
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(Constants.LOG_TAG_MAIN, "Create HTTP client");
		}
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpConnectionParams.setConnectionTimeout(params, HTTP_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_SOCKET_TIMEOUT);

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

	private ConnectivityManager mConnectionManager;
	
	
	private StationDirectory mStationDirectory;
	private CatalogStorage mStorage;

	private ImportDirectory mImportDirectory;
	private ImportMapDirectory mImportMapDirectory;
	private ImportTransportDirectory mImportTransportDirectory;
	private CityDirectory mCityDirectory;
	private CountryDirectory mCountryDirectory;
	
	
}
