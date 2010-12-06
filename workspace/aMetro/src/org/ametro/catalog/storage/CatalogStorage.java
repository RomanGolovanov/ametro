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
package org.ametro.catalog.storage;

import static org.ametro.app.Notifications.TASK_FAILED_ID;
import static org.ametro.app.Notifications.TASK_PROGRESS_ID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.tasks.BaseTask;
import org.ametro.catalog.storage.tasks.DownloadIconsTask;
import org.ametro.catalog.storage.tasks.DownloadMapTask;
import org.ametro.catalog.storage.tasks.ICatalogStorageTaskListener;
import org.ametro.catalog.storage.tasks.ImportMapTask;
import org.ametro.catalog.storage.tasks.LoadBaseCatalogTask;
import org.ametro.catalog.storage.tasks.LoadFileCatalogTask;
import org.ametro.catalog.storage.tasks.LoadWebCatalogTask;
import org.ametro.catalog.storage.tasks.UpdateMapTask;
import org.ametro.service.CatalogTaskQueueService;
import org.ametro.ui.MapDetailsActivity;
import org.ametro.ui.TaskFailedList;
import org.ametro.ui.TaskQueuedList;
import org.ametro.util.FileUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CatalogStorage implements Runnable, ICatalogStorageTaskListener { //, IMapDownloadListener { //, IMapImportListener {

	public static final int LOCAL = 0;
	public static final int IMPORT = 1;
	public static final int ONLINE = 2;

	public static final int SAVING_DELAY = 30 * 1000;
	public static final String QUEUE_THREAD_NAME = "TASK_QUEUE";
	
	/*package*/ Catalog[] mCatalogs = new Catalog[3];
	/*package*/ ArrayList<ICatalogStorageListener> mListeners = new ArrayList<ICatalogStorageListener>();

	/*package*/ boolean mIsShutdown = false;
	/*package*/ Thread mTaskWorker = new Thread(this,QUEUE_THREAD_NAME);

	/*package*/ LinkedBlockingQueue<BaseTask> mTaskQueue = new LinkedBlockingQueue<BaseTask>();
	/*package*/ LinkedList<BaseTask> mAsyncRunQueue = new LinkedList<BaseTask>();
	/*package*/ LinkedList<BaseTask> mFailedQueue = new LinkedList<BaseTask>();
	/*package*/ BaseTask mSyncRunTask = null;
	
	private NotificationManager mNotificationManager;
	private Context mContext;
	
	private String mDownloadNotificationTitle;
	private String mImportNotificationTitle;
	private String mFailedTaskNotificationTitle;
	private String mFailedTaskNotificationText;

	private Notification mFailedNotification;
	
	private Notification mProgressNotification;
	
	private void removeTaskQueueNotification(){
		mContext.stopService(new Intent(mContext, CatalogTaskQueueService.class));
	}
	
	private void removeTaskProgressNotification(){
		mNotificationManager.cancel(TASK_PROGRESS_ID);
	}
	
	private void displayTaskQueueNotification(int taskLeft)
	{
		Intent intent = new Intent(mContext, CatalogTaskQueueService.class);
		intent.putExtra(CatalogTaskQueueService.EXTRA_TASK_LEFT, taskLeft);
		mContext.startService(intent);
	}	

	private void displayTaskProgressNotification(BaseTask task, String title, String message, int iconId)
	{
		Notification notification = mProgressNotification;
		if(notification==null){
			notification = new Notification(iconId, null,System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			mProgressNotification = notification;
		}
		PendingIntent contentIntent;
		if(task instanceof ImportMapTask || task instanceof DownloadMapTask){
			Intent detailsIntent = new Intent(mContext, MapDetailsActivity.class);
			detailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			detailsIntent.putExtra(MapDetailsActivity.EXTRA_SYSTEM_NAME, (String)task.getTaskId() );
			contentIntent = PendingIntent.getActivity(mContext, 0, detailsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		}else{
			contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, TaskQueuedList.class), 0);
		}
		notification.when = System.currentTimeMillis();
		notification.setLatestEventInfo(mContext, title, message, contentIntent);
		mNotificationManager.notify(TASK_PROGRESS_ID, notification);
	}	
	
	private void displayTaskFailedNotification()
	{
		Notification notification = mFailedNotification;
		if(notification==null){
			notification = new Notification(android.R.drawable.stat_notify_error, null, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			mFailedNotification = notification;
		}
		int count = mFailedQueue.size();
		String message = mFailedTaskNotificationText + " " + count;
		notification.when = System.currentTimeMillis();
		notification.number = count;
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, TaskFailedList.class), 0);
		notification.setLatestEventInfo(mContext, mFailedTaskNotificationTitle, message, contentIntent);
		mNotificationManager.notify(TASK_FAILED_ID, notification);
	}	
	
	public CatalogStorage(Context context){
		mTaskWorker.setPriority(Thread.MIN_PRIORITY);
		mTaskWorker.setDaemon(true);
		mTaskWorker.start();
		mContext = context;

		final Resources res = mContext.getResources();
		mDownloadNotificationTitle = res.getString(R.string.msg_download_notify_title);
		mImportNotificationTitle = res.getString(R.string.msg_import_notify_title);
		mFailedTaskNotificationTitle = res.getString(R.string.msg_task_error_notify_title);
		mFailedTaskNotificationText = res.getString(R.string.msg_task_error_notify_text);
		
		mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void shutdown(){
		mIsShutdown = true;
	}
	
	public void addCatalogStorageListener(ICatalogStorageListener listener){
		mListeners.add(listener);
	}
	
	public void removeCatalogStorageListener(ICatalogStorageListener listener){
		mListeners.remove(listener);
	}

	/*package*/ void fireCatalogChanged(int catalogId, Catalog catalog){
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogLoaded(catalogId, catalog);
		}
	}

	/*package*/ void fireCatalogOperationFailed(int catalogId, String message){
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogFailed(catalogId, message);
		}
	}

	/*package*/ void fireCatalogOperationProgress(int catalogId, int progress, int total, String message){
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogProgress(catalogId, progress, total, message);
		}
	}
	
	/*package*/ void fireCatalogMapChanged(String systemName){
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogMapChanged(systemName);
		}
	}
	
	/*package*/ void fireCatalogMapDownloadDone(String systemName) {
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogMapDownloadDone(systemName);
		}
	}
	
	/*package*/ void fireCatalogMapImportDone(String systemName) {
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogMapImportDone(systemName);
		}
	}	
	
	/*package*/ void fireCatalogMapDownloadFailed(String systemName, Throwable e) {
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogMapDownloadFailed(systemName, e);
		}
	}
	
	/*package*/ void fireCatalogMapImportFailed(String systemName, Throwable e) {
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogMapImportFailed(systemName, e);
		}
	}
	
	/*package*/ void fireCatalogMapDownloadProgress(String systemName, int progress, int total) {
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogMapDownloadProgress(systemName, progress, total);
		}
	}
	
	/*package*/ void fireCatalogMapImportProgress(String systemName, int progress, int total) {
		for(ICatalogStorageListener listener : mListeners){
			listener.onCatalogMapImportProgress(systemName, progress, total);
		}
	}

	public Catalog getCatalog(int catalogId) {
		synchronized (mTaskQueue) {
			return mCatalogs[catalogId];
		}
	}	
	
	public void requestCatalog(int catalogId, boolean refresh)
	{
		LoadBaseCatalogTask task = null;
		if(catalogId == LOCAL){
			task = new LoadFileCatalogTask(LOCAL, Constants.LOCAL_CATALOG_STORAGE, Constants.LOCAL_CATALOG_PATH, refresh);
		}
		if(catalogId == IMPORT){
			task = new LoadFileCatalogTask(IMPORT, Constants.IMPORT_CATALOG_STORAGE, Constants.IMPORT_CATALOG_PATH, refresh);
		}
		if(catalogId == ONLINE){
			task = new LoadWebCatalogTask(ONLINE, Constants.ONLINE_CATALOG_STORAGE, Constants.ONLINE_CATALOG_URL, Constants.ONLINE_CATALOG_BASE_URLS, refresh);
		}
		requestTask(task);
	}	
	
	public void deleteLocalMap(String systemName) {
		synchronized (mTaskQueue) {
			if(mCatalogs[LOCAL]!=null && !mCatalogs[LOCAL].isCorrupted()){
				CatalogMap map = mCatalogs[LOCAL].getMap(systemName);
				if(map!=null ){
					try {
						Log.w(Constants.LOG_TAG_MAIN, "Delete local map " + map.getAbsoluteUrl());
						if(FileUtil.delete(map.getAbsoluteUrl())){
							mCatalogs[LOCAL].deleteMap(map);
							mCatalogs[LOCAL].setTimestamp(System.currentTimeMillis());
							requestCatalogSave(LOCAL);
							fireCatalogMapChanged(systemName);
							fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
						}
					} catch (Exception e) {
						if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
							Log.e(Constants.LOG_TAG_MAIN, "Delete local map failed", e);
						}
					}
				}
				
			}
		}
	}

	public void deleteImportMap(String systemName) {
		synchronized (mTaskQueue) {
			if(mCatalogs[IMPORT]!=null && !mCatalogs[IMPORT].isCorrupted()){
				CatalogMap map = mCatalogs[IMPORT].getMap(systemName);
				if(map!=null ){
					try {
						Log.w(Constants.LOG_TAG_MAIN, "Delete import map " + map.getAbsoluteUrl());
						if(FileUtil.delete(map.getAbsoluteUrl())){
							mCatalogs[IMPORT].deleteMap(map);
							mCatalogs[IMPORT].setTimestamp(System.currentTimeMillis());
							requestCatalogSave(IMPORT);
							fireCatalogMapChanged(systemName);
							fireCatalogChanged(IMPORT, mCatalogs[IMPORT]);
						}
					} catch (Exception e) {
						if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
							Log.e(Constants.LOG_TAG_MAIN, "Delete import map failed", e);
						}
					}					
				}
			}
		}
	}
	
	public void cancelDownload(String systemName) {
		synchronized (mTaskQueue) {
			DownloadMapTask task = findQueuedDownloadTask(systemName);
			if(task!=null){
				mTaskQueue.remove(task);
				fireCatalogMapChanged(systemName);
			}
			if(mSyncRunTask instanceof DownloadMapTask && systemName.equals( mSyncRunTask.getTaskId())){
				mSyncRunTask.abort();
				fireCatalogMapChanged(systemName);
			}
		}
	}

	public void requestDownload(String systemName) {
		synchronized (mTaskQueue) {
			requestTask(new DownloadMapTask(systemName));
			fireCatalogMapChanged(systemName);
		}
	}

	public void requestDownload(List<String> systemNames) {
		synchronized (mTaskQueue) {
			for(String systemName : new LinkedList<String>(systemNames)){
				requestTask(new DownloadMapTask(systemName));
				fireCatalogMapChanged(systemName);
			}
		}
	}
	
	
	public void cancelImport(String systemName) {
		synchronized (mTaskQueue) {
			ImportMapTask task = findQueuedImportTask(systemName);
			if(task!=null){
				mTaskQueue.remove(task);
				fireCatalogMapChanged(systemName);
			}
		}
	}

	public void requestImport(String systemName) {
		synchronized (mTaskQueue) {
			requestTask(new ImportMapTask(systemName));
			fireCatalogMapChanged(systemName);
		}
	}

	public boolean isImportingTask(String systemName){
		synchronized (mTaskQueue) {
			return mSyncRunTask!=null && mSyncRunTask instanceof ImportMapTask && !mSyncRunTask.isDone() && systemName.equals(mSyncRunTask.getTaskId());
		}
	}
	
	public boolean isDownloadingTask(String systemName){
		synchronized (mTaskQueue) {
			return mSyncRunTask!=null && mSyncRunTask instanceof DownloadMapTask && !mSyncRunTask.isDone() && systemName.equals(mSyncRunTask.getTaskId());
		}
	}	
	
	public void cancelAllTasks(){
		synchronized (mTaskQueue) {
			mTaskQueue.clear();
			removeTaskQueueNotification();
		}
	}
	
	public ImportMapTask findQueuedImportTask(String systemName){
		synchronized (mTaskQueue) {
			for(BaseTask queued : mTaskQueue){
				if(queued instanceof ImportMapTask && systemName.equals(queued.getTaskId())){
					return (ImportMapTask)queued;
				}
			}			
		}
		return null;
	}

	public DownloadMapTask findQueuedDownloadTask(String systemName){
		synchronized (mTaskQueue) {
			for(BaseTask queued : mTaskQueue){
				if(queued instanceof DownloadMapTask && systemName.equals(queued.getTaskId())){
					return (DownloadMapTask)queued;
				}
			}			
		}
		return null;
	}	
	
	public boolean hasTasks(){
		synchronized (mTaskQueue) {
			return mSyncRunTask!=null || mTaskQueue.size()>0 || mAsyncRunQueue.size()>0;
		}
	}
	
	public void run() {
		while(!mIsShutdown){
			try {
				synchronized (mTaskQueue) {
					mSyncRunTask = null;
					if(mTaskQueue.size() == 0){
						removeTaskQueueNotification();
						removeTaskProgressNotification();
					}
				}
				BaseTask task = mTaskQueue.take();
				if(task.isAsync()){
					synchronized (mTaskQueue) {
						final BaseTask asyncTask = task;
						mAsyncRunQueue.add(asyncTask);
						Thread runner = new Thread(new Runnable() {
							public void run() {
								asyncTask.execute(ApplicationEx.getInstance(), CatalogStorage.this);
							}
						});
						runner.start();
					}
				}else{
					synchronized (mTaskQueue) {
						mSyncRunTask = task;
						int taskLeft = mTaskQueue.size()+1;
						displayTaskQueueNotification(taskLeft);
					}
					task.execute(ApplicationEx.getInstance(), this);
					synchronized (mTaskQueue) {
						mSyncRunTask = null;
					}
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
					Log.w(Constants.LOG_TAG_MAIN, "Interrupted CatalogService task waiting");
				}
			} catch(Exception e){
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
					Log.e(Constants.LOG_TAG_MAIN, "Failed CatalogService task",e);
				}
			} 
		}
	}

	public boolean isTaskCanceled(BaseTask task){
		if(mIsShutdown){
			return true;
		}
		return false;
	}
	
	public void onTaskUpdated(BaseTask task, long progress, long total, String message){
		if(task instanceof LoadBaseCatalogTask){
			LoadBaseCatalogTask info = (LoadBaseCatalogTask)task;
			fireCatalogOperationProgress(info.getCatalogId(), (int)progress, (int)total, message);
		}
		if(task instanceof ImportMapTask){
			String mapName = (String)task.getTaskId();
			fireCatalogMapImportProgress(mapName,(int)progress,(int)total);
			
			displayTaskProgressNotification(
					task,
					mImportNotificationTitle,
					mapName + ", " +  progress + "/" + total,
					android.R.drawable.stat_sys_download);
		}
		if(task instanceof DownloadMapTask){
			String mapName = (String)task.getTaskId();
			fireCatalogMapDownloadProgress(mapName,(int)progress,(int)total);
			
			displayTaskProgressNotification(
					task,
					mDownloadNotificationTitle,
					mapName + ", " +  progress + "/" + total,
					android.R.drawable.stat_sys_download);
		}
	}
	
	public void onTaskCanceled(BaseTask task){
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(Constants.LOG_TAG_MAIN, "Canceled task " + task.toString());
		}
		synchronized (mTaskQueue) {
			if(mSyncRunTask==task){
				mSyncRunTask = null;
			}
		}
		if(task instanceof LoadBaseCatalogTask){
			LoadBaseCatalogTask info = (LoadBaseCatalogTask)task;
			int catalogId = info.getCatalogId();
			Catalog catalog = info.getCatalog();
			synchronized (mTaskQueue) {
				mCatalogs[catalogId] = catalog;
			}
			fireCatalogChanged(catalogId, catalog);
		}
		if(task instanceof UpdateMapTask){
			fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
			fireCatalogMapChanged((String)task.getTaskId());
		}		
		if(mAsyncRunQueue.contains(task)){
			mAsyncRunQueue.remove(task);
		}
	}
	
	public void onTaskFailed(BaseTask task, Throwable reason){
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(Constants.LOG_TAG_MAIN, "Failed task " + task.toString());
		}
		synchronized (mTaskQueue) {
			if(mSyncRunTask==task){
				mSyncRunTask = null;
			}
		}
		mFailedQueue.add(task);

		if(task instanceof LoadBaseCatalogTask){
			LoadBaseCatalogTask info = (LoadBaseCatalogTask)task;
			int catalogId = info.getCatalogId();
			Catalog catalog = info.getCatalog();
			synchronized (mTaskQueue) {
				mCatalogs[catalogId] = catalog;
			}
			fireCatalogChanged(catalogId, catalog);
		}
		if(task instanceof UpdateMapTask){
			fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
			if(task instanceof DownloadMapTask){
				fireCatalogChanged(ONLINE, mCatalogs[ONLINE]);			
				fireCatalogMapDownloadFailed((String)task.getTaskId(), reason);
			}else  if(task instanceof ImportMapTask){
				fireCatalogChanged(IMPORT, mCatalogs[IMPORT]);
				fireCatalogMapImportFailed((String)task.getTaskId(), reason);	
			}
			displayTaskFailedNotification();
		}
		if(mAsyncRunQueue.contains(task)){
			mAsyncRunQueue.remove(task);
		}
	}
	
	public void onTaskBegin(BaseTask task){
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(Constants.LOG_TAG_MAIN, "Begin task " + task.toString());
		}
		if(task instanceof UpdateMapTask){
			fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
			fireCatalogMapChanged((String)task.getTaskId());
			String mapName = (String)task.getTaskId();
			
			if(task instanceof ImportMapTask){
				displayTaskProgressNotification(
						task,
						mImportNotificationTitle,
						mapName,
						android.R.drawable.stat_sys_download);
			}
			if(task instanceof DownloadMapTask){
				displayTaskProgressNotification(
						task,
						mDownloadNotificationTitle,
						mapName,
						android.R.drawable.stat_sys_download);
			}			
		}
	}
	
	public void onTaskDone(BaseTask task){
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(Constants.LOG_TAG_MAIN, "Done task " + task.toString());
		}
		synchronized (mTaskQueue) {
			if(mSyncRunTask==task){
				mSyncRunTask = null;
			}
		}
		if(task instanceof LoadBaseCatalogTask){
			LoadBaseCatalogTask info = (LoadBaseCatalogTask)task;
			int catalogId = info.getCatalogId();
			Catalog catalog = info.getCatalog();
			synchronized (mTaskQueue) {
				mCatalogs[catalogId] = catalog;
			}
			fireCatalogChanged(catalogId, catalog);
		}else if(task instanceof UpdateMapTask){
			fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
			if(task instanceof DownloadMapTask){
				fireCatalogChanged(ONLINE, mCatalogs[ONLINE]);
				fireCatalogMapDownloadDone((String)task.getTaskId());
			}else  if(task instanceof ImportMapTask){
				fireCatalogChanged(IMPORT, mCatalogs[IMPORT]);
				fireCatalogMapImportDone((String)task.getTaskId());
			}
		}else if(task instanceof DownloadIconsTask){
			fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
			fireCatalogChanged(IMPORT, mCatalogs[IMPORT]);
			fireCatalogChanged(ONLINE, mCatalogs[ONLINE]);
		}
		if(mAsyncRunQueue.contains(task)){
			mAsyncRunQueue.remove(task);
		}
	}

	public boolean requestTask(BaseTask task) {
		synchronized (mTaskQueue) {
			final Object taskId = task.getTaskId();
			if(taskId!=null){
				final Class<? extends BaseTask> newTaskClass = task.getClass();
				for(BaseTask queued : mTaskQueue){
					if(queued.getClass().equals(newTaskClass) && taskId.equals( queued.getTaskId() )){
						if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
							Log.d(Constants.LOG_TAG_MAIN, "Reject task " + task.toString() + " due it already queued");
						}
						return false;
					}
				}
				if(mSyncRunTask!=null && mSyncRunTask.getClass().equals(newTaskClass) && taskId.equals( mSyncRunTask.getTaskId() )){
					if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
						Log.d(Constants.LOG_TAG_MAIN, "Reject task " + task.toString() + " due it already runned");
					}
					return false;
				}
				for(BaseTask running : mAsyncRunQueue){
					if(running.getClass().equals(newTaskClass) && taskId.equals( running.getTaskId() )){
						if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
							Log.d(Constants.LOG_TAG_MAIN, "Reject task " + task.toString() + " due it already runned async.");
						}
						return false;
					}
				}
			}
		}
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(Constants.LOG_TAG_MAIN, "Queued task " + task.toString() );
		}
		mTaskQueue.add(task);
		return true;
	}

	private Handler mCatalogSaveHandler = new Handler(){
		public void handleMessage(Message msg) {
			synchronized (mTaskQueue) {
				Catalog catalog = null;
				File storage = null;
				switch (msg.what) {
				case LOCAL:
					catalog = mCatalogs[LOCAL];
					storage = Constants.LOCAL_CATALOG_STORAGE;
					break;
				case IMPORT:
					catalog = mCatalogs[IMPORT];
					storage = Constants.IMPORT_CATALOG_STORAGE;
					break;
				case ONLINE:
					catalog = mCatalogs[ONLINE];
					storage = Constants.ONLINE_CATALOG_STORAGE;
					GlobalSettings.setUpdateDate(mContext, catalog.getTimestamp());
					break;
				}
				if(catalog!=null){
					try {
						catalog.save(storage);

					} catch (IOException e) {
						if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
							Log.e(Constants.LOG_TAG_MAIN, "Failed to save catalog " + storage.toString(), e );
						}
					}
				}
			}
			super.handleMessage(msg);
		}
	};
	
	public void requestCatalogSave(int catalogId) {
		synchronized (mTaskQueue) {
			mCatalogSaveHandler.removeMessages(catalogId);
			if(mTaskQueue.isEmpty()){
				mCatalogSaveHandler.sendEmptyMessage(catalogId);
			}else{
				mCatalogSaveHandler.sendEmptyMessageDelayed(catalogId, SAVING_DELAY);
			}
		}
	}
	
	public ArrayList<BaseTask> takeFailedTaskList(){
		ArrayList<BaseTask> lst = new ArrayList<BaseTask>(mFailedQueue);
		mFailedQueue.removeAll(lst);
		return lst;
	}

	public ArrayList<BaseTask> takeQueuedTaskList() {
		synchronized (mTaskQueue) {
			ArrayList<BaseTask> lst = new ArrayList<BaseTask>(mTaskQueue.size()+1);
			if(mSyncRunTask!=null){
				lst.add(mSyncRunTask);
			}
			lst.addAll(mTaskQueue);
			return lst;
		}
	}

}
