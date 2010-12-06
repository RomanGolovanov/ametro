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
package org.ametro.service;

import static org.ametro.app.Notifications.AUTO_UPDATE_ID;
import static org.ametro.app.Notifications.AUTO_UPDATE_RESULT_ID;
import static org.ametro.catalog.storage.CatalogStorage.LOCAL;
import static org.ametro.catalog.storage.CatalogStorage.ONLINE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.CatalogStorageStateProvider;
import org.ametro.catalog.storage.ICatalogStorageListener;
import org.ametro.ui.CatalogLocalListActivity;
import org.ametro.ui.CatalogTabHostActivity;
import org.ametro.ui.TaskQueuedList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class AutoUpdateService extends Service implements ICatalogStorageListener, Runnable {

	private NotificationManager mNotificationManager;
	private Notification mNotification;

	private CatalogStorage mStorage;
	private CatalogStorageStateProvider mStorageStateProvider;
	private Catalog mOnlineCatalog;
	private Catalog mLocalCatalog;

	private LinkedList<String> mDownloadMaps; 
	private int mUpdatedMapCount;
	private int mFailedMapCount;

	private boolean mIsShutdown;

	private int mStage;

	private static final int STAGE_NONE = 0;
	private static final int STAGE_REQUEST_ONLINE_CATALOG = 1;
	private static final int STAGE_REQUEST_LOCAL_CATALOG = 2;
	private static final int STAGE_DOWNLOAD_MAPS = 3;

	private Thread mWorkingThread;
	private Object mMutex = new Object();

	@SuppressWarnings("unchecked")
	private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class};
	@SuppressWarnings("unchecked")
	private static final Class[] mStopForegroundSignature = new Class[] { boolean.class};

	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];


	public void onCreate() {
		Log.d(Constants.LOG_TAG_MAIN, "Create AutoUpdateService");
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		try {
			mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			mStartForeground = mStopForeground = null;
		}
		mStorage = ((ApplicationEx)getApplicationContext()).getCatalogStorage();
		mStorage.addCatalogStorageListener(this);
		mStorageStateProvider = new CatalogStorageStateProvider(mStorage);

		mIsShutdown = false;

		mWorkingThread = new Thread(this);
		mWorkingThread.start();

		startForegroundCompat();
	}

	public void onDestroy() {
		Log.d(Constants.LOG_TAG_MAIN, "Destroy AutoUpdateService");
		mIsShutdown = true;
		synchronized (mMutex) {
			mMutex.notify();
		}
		try {
			mWorkingThread.join();
		} catch (InterruptedException e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN, "Failed to join worker thread", e);
			}
		}
		stopForegroundCompat();
	}	

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCatalogFailed(int catalogId, String message) {
		if(catalogId==ONLINE && mStage == STAGE_REQUEST_ONLINE_CATALOG){
			mStage = STAGE_NONE;
			mOnlineCatalog = null;
			synchronized (mMutex) {
				mMutex.notify();
			}
		}
		if(catalogId==LOCAL && mStage == STAGE_REQUEST_LOCAL_CATALOG){
			mStage = STAGE_NONE;
			mLocalCatalog = null;
			synchronized (mMutex) {
				mMutex.notify();
			}
		}
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		if(catalogId==ONLINE && mStage == STAGE_REQUEST_ONLINE_CATALOG){
			mStage = STAGE_NONE;
			mOnlineCatalog = catalog;
			synchronized (mMutex) {
				mMutex.notify();
			}
		}
		if(catalogId==LOCAL && mStage == STAGE_REQUEST_LOCAL_CATALOG){
			mStage = STAGE_NONE;
			mLocalCatalog = catalog;
			synchronized (mMutex) {
				mMutex.notify();
			}
		}
	}

	public void onCatalogMapChanged(String systemName) {
	}

	public void onCatalogMapDownloadDone(String systemName) {
		if(mStage == STAGE_DOWNLOAD_MAPS){
			if(mDownloadMaps.remove(systemName)){
				mUpdatedMapCount++;
				synchronized (mMutex) {
					mMutex.notify();
				}
			}
		}
	}

	public void onCatalogMapDownloadFailed(String systemName, Throwable ex) {
		if(mStage == STAGE_DOWNLOAD_MAPS){
			if(mDownloadMaps.remove(systemName)){
				mFailedMapCount++;
				synchronized (mMutex) {
					mMutex.notify();
				}
			}
		}
	}

	public void onCatalogMapDownloadProgress(String systemName, int progress, int total) {
	}

	public void onCatalogMapImportDone(String systemName) {
	}
	
	public void onCatalogMapImportFailed(String systemName, Throwable e) {
	}

	public void onCatalogMapImportProgress(String systemName, int progress, int total) {
	}

	public void onCatalogProgress(int catalogId, int progress, int total, String message) {
	}	

	private void createAutoUpdatesResultNotification() {
		Notification notification = new Notification(android.R.drawable.stat_notify_sync_noanim, null,System.currentTimeMillis());
		Intent i = new Intent(this, CatalogTabHostActivity.class);
		if(mDownloadMaps.size()>0){
			i.putExtra(CatalogLocalListActivity.EXTRA_INVOKE_LOCAL_UPDATE_LIST, true);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
			notification.setLatestEventInfo(this, getString(R.string.app_name), getString(R.string.msg_map_updates_available), contentIntent);
			notification.number = mDownloadMaps.size();
		}else{
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
			notification.setLatestEventInfo(this, getString(R.string.app_name), String.format(getString(R.string.msg_map_updates_count), mUpdatedMapCount), contentIntent);
			notification.number = mUpdatedMapCount;
		}
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(AUTO_UPDATE_RESULT_ID, notification);
	}

	private void createNotification()
	{
		Notification notification = mNotification;
		if(notification==null){
			notification = new Notification(android.R.drawable.stat_notify_sync, null,System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			mNotification = notification;
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TaskQueuedList.class), 0);
		notification.setLatestEventInfo(this, getString(R.string.app_name), getString(R.string.msg_update_online_catalog), contentIntent);
		mNotificationManager.notify(AUTO_UPDATE_ID, notification);
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	private void startForegroundCompat() {
		createNotification();
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(AUTO_UPDATE_ID);
			mStartForegroundArgs[1] = mNotification;
			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke startForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke startForeground", e);
			}
			return;
		}

		// Fall back on the old API.
		setForeground(true);
		mNotificationManager.notify(AUTO_UPDATE_ID, mNotification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	private void stopForegroundCompat() {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke stopForeground", e);
			}
			return;
		}
		// Fall back on the old API.  Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		mNotificationManager.cancel(AUTO_UPDATE_ID);
		setForeground(false);
	}

	public void run() {
		try{

			loadLocalCatalog();
			if(mIsShutdown) { 
				return;
			}
			loadOnlineCatalog();
			if(mIsShutdown) { 
				return;
			}
			createUpdateList();
			if(mDownloadMaps.size()>0){
				if(GlobalSettings.isAutoUpdateMapsEnabled(AutoUpdateService.this)){
					if(mIsShutdown) { 
						return;
					}
					downloadMaps();
				}
				//if(mDownloadMaps.size()>0 || mUpdatedMapCount>0){
					createAutoUpdatesResultNotification();
				//}
			}
		}catch(Exception ex){
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN, "Failed to execute autoupdate", ex);
			}
		}
		stopSelf();
	}

	private void downloadMaps() throws Exception {
		mUpdatedMapCount = 0;
		mFailedMapCount = 0;
		mStage = STAGE_DOWNLOAD_MAPS;
		mStorage.requestDownload(mDownloadMaps);
		while(mDownloadMaps.size()>0){
			synchronized (mMutex) {
				mMutex.wait();
			}
			if(mIsShutdown) { 
				return;
			}
		}
	}

	private void createUpdateList() {
		mDownloadMaps = new LinkedList<String>();
		ArrayList<CatalogMapPair> pairs = CatalogMapPair.diff(mLocalCatalog, mOnlineCatalog, CatalogMapPair.DIFF_MODE_LOCAL);
		for(CatalogMapPair pair : pairs){
			final CatalogMap local = pair.getLocal();
			final CatalogMap remote = pair.getRemote();
			int state = mStorageStateProvider.getLocalCatalogState(local, remote);
			if(state == CatalogMapState.UPDATE || state == CatalogMapState.NEED_TO_UPDATE){
				mDownloadMaps.add(pair.getSystemName());
			}
		}
	}

	private void loadOnlineCatalog() throws Exception {
		// get current online catalog
		mStage = STAGE_REQUEST_ONLINE_CATALOG;
		mStorage.requestCatalog(ONLINE, true);
		synchronized (mMutex) {
			mMutex.wait();
		}
		if(mOnlineCatalog==null || mOnlineCatalog.isCorrupted()){
			throw new Exception("Online catalog is empty or failed");
		}else{
			mStorage.requestCatalogSave(CatalogStorage.ONLINE);
		}
	}

	private void loadLocalCatalog() throws Exception {
		// get current local catalog
		mStage = STAGE_REQUEST_LOCAL_CATALOG;
		mStorage.requestCatalog(LOCAL, false);
		synchronized (mMutex) {
			mMutex.wait();
		}
		if(mLocalCatalog==null || mLocalCatalog.isCorrupted() || mLocalCatalog.getMaps() == null || mLocalCatalog.getMaps().size() == 0){
			// nothing to update. exit
			throw new Exception("Local catalog is empty or failed");
		}
	}

}
