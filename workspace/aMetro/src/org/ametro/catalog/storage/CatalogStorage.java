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
package org.ametro.catalog.storage;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.ametro.ApplicationEx;
import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.obsolete.MapDownloadQueue;
import org.ametro.catalog.storage.obsolete.MapDownloadQueue.IMapDownloadListener;
import org.ametro.catalog.storage.tasks.BaseTask;
import org.ametro.catalog.storage.tasks.ICatalogStorageTaskListener;
import org.ametro.catalog.storage.tasks.ImportMapTask;
import org.ametro.catalog.storage.tasks.LoadBaseCatalogTask;
import org.ametro.catalog.storage.tasks.LoadFileCatalogTask;
import org.ametro.catalog.storage.tasks.LoadWebCatalogTask;
import org.ametro.catalog.storage.tasks.UpdateMapTask;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.FileUtil;

import android.util.Log;

public class CatalogStorage implements Runnable, ICatalogStorageTaskListener, IMapDownloadListener { //, IMapImportListener {

	public static final int LOCAL = 0;
	public static final int IMPORT = 1;
	public static final int ONLINE = 2;

	/*package*/ Object mMutex = new Object();
	
	/*package*/ Catalog[] mCatalogs;

	/*package*/ ArrayList<ICatalogStorageListener> mCatalogListeners;
	
	/*package*/ MapDownloadQueue mMapDownloadQueue;
	
	/*package*/ //MapImportQueue mMapImportQueue;
	
	/*package*/ boolean mIsShutdown;
	
	/*package*/ Thread mTaskWorker;

	/*package*/ LinkedBlockingQueue<BaseTask> mTaskQueue = new LinkedBlockingQueue<BaseTask>();
	/*package*/ LinkedList<BaseTask> mAsyncRunQueue = new LinkedList<BaseTask>();
	/*package*/ BaseTask mSyncRunTask = null;
	
	public CatalogStorage(
			File localStorage, File localPath, 
			File importStorage, File importPath, 
			File onlineStorage, String onlineUrl){
		
		this.mCatalogListeners = new ArrayList<ICatalogStorageListener>();

		this.mIsShutdown = false;

		this.mMapDownloadQueue = new MapDownloadQueue(this);
		//this.mMapImportQueue = new MapImportQueue(this); 
		
		mCatalogs = new Catalog[3];

		mTaskWorker = new Thread(this);
		mTaskWorker.start();
	}
	
	public void shutdown(){
		mIsShutdown = true;
		//mMapImportQueue.shutdown();
		mMapDownloadQueue.shutdown();
	}

	
	public void addCatalogChangedListener(ICatalogStorageListener listener){
		mCatalogListeners.add(listener);
	}
	
	public void removeCatalogChangedListener(ICatalogStorageListener listener){
		mCatalogListeners.remove(listener);
	}

	/*package*/ void fireCatalogChanged(int catalogId, Catalog catalog){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogLoaded(catalogId, catalog);
		}
	}

	/*package*/ void fireCatalogOperationFailed(int catalogId, String message){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogFailed(catalogId, message);
		}
	}

	/*package*/ void fireCatalogOperationProgress(int catalogId, int progress, int total, String message){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogProgress(catalogId, progress, total, message);
		}
	}
	
	/*package*/ void fireCatalogMapChanged(String systemName){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogMapChanged(systemName);
		}
	}
	
	/*package*/ void fireCatalogMapDownloadFailed(String systemName, Throwable e) {
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogMapDownloadFailed(systemName, e);
		}
	}
	
	/*package*/ void fireCatalogMapImportFailed(String systemName, Throwable e) {
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogMapImportFailed(systemName, e);
		}
	}
	
	/*package*/ void fireCatalogMapDownloadProgress(String systemName, int progress, int total) {
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogMapDownloadProgress(systemName, progress, total);
		}
	}
	
	/*package*/ void fireCatalogMapImportProgress(String systemName, int progress, int total) {
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogMapImportProgress(systemName, progress, total);
		}
	}

	public Catalog getCatalog(int catalogId) {
		synchronized (mMutex) {
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
			task = new LoadWebCatalogTask(ONLINE, Constants.ONLINE_CATALOG_STORAGE, URI.create(Constants.ONLINE_CATALOG_PATH), refresh);
		}
		requestTask(task);
		//mTaskQueue.add(task);
	}	
	
	public void deleteLocalMap(String systemName) {
		synchronized (mMutex) {
			if(mCatalogs[LOCAL]!=null && !mCatalogs[LOCAL].isCorrupted()){
				CatalogMap map = mCatalogs[LOCAL].getMap(systemName);
				if(map!=null ){
					mCatalogs[LOCAL].deleteMap(map);
					//mCatalogs[LOCAL].save();
					FileUtil.delete(map.getAbsoluteUrl());
					fireCatalogMapChanged(systemName);
					fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
				}
				
			}
		}
	}

	public void deleteImportMap(String systemName) {
		synchronized (mMutex) {
			if(mCatalogs[IMPORT]!=null && !mCatalogs[IMPORT].isCorrupted()){
				CatalogMap map = mCatalogs[IMPORT].getMap(systemName);
				if(map!=null ){
					mCatalogs[IMPORT].deleteMap(map);
					//mCatalogs[IMPORT].save();
					FileUtil.delete(map.getAbsoluteUrl());
					fireCatalogMapChanged(systemName);
					fireCatalogChanged(IMPORT, mCatalogs[IMPORT]);
				}
				
			}
		}
	}
	
	public void cancelDownload(String systemName) {
		synchronized (mMutex) {
			if(mCatalogs[ONLINE]!=null && !mCatalogs[ONLINE].isCorrupted()){
				CatalogMap map = mCatalogs[ONLINE].getMap(systemName);
				if(map!=null){
					mMapDownloadQueue.cancel(map);
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

	public void requestDownload(String systemName) {
		synchronized (mMutex) {
			if(mCatalogs[ONLINE]!=null && !mCatalogs[ONLINE].isCorrupted()){
				CatalogMap map = mCatalogs[ONLINE].getMap(systemName);
				if(map!=null){
					mMapDownloadQueue.request(map);
					fireCatalogMapChanged(map.getSystemName());
				}
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
	
	public void onMapDownloadBegin(CatalogMap map) {
		String systemName = map.getSystemName();
		fireCatalogMapChanged(systemName);
	}

	public void onMapDownloadCanceled(CatalogMap map) {
		String systemName = map.getSystemName();
		fireCatalogMapChanged(systemName);
	}

	public void onMapDownloadDone(CatalogMap map, File file) {
		String systemName = map.getSystemName();
		File local = new File(GlobalSettings.getLocalCatalogMapFileName(map.getSystemName()));
		FileUtil.delete(local);
		FileUtil.move(file, local);
		Model model = ModelBuilder.loadModelDescription(local.getAbsolutePath());
		synchronized(mMutex){
			CatalogMap downloaded = Catalog.extractCatalogMap(mCatalogs[LOCAL], local, local.getName().toLowerCase(), model);
			mCatalogs[LOCAL].appendMap(downloaded);
			//mCatalogs[LOCAL].save();
		}
		fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
		fireCatalogMapChanged(systemName);
	}

	public void onMapDownloadFailed(CatalogMap map, Throwable reason) {
		String systemName = map.getSystemName();
		fireCatalogMapDownloadFailed(systemName,reason);
		fireCatalogMapChanged(systemName);
	}

	public void onMapDownloadProgressChanged(CatalogMap map, long progress, long total) {
		String systemName = map.getSystemName();
		fireCatalogMapDownloadProgress(systemName, (int)progress, (int)total);
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
				}
				BaseTask task = mTaskQueue.take();
				if(task!=null){
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
						}
						task.execute(ApplicationEx.getInstance(), this);
						synchronized (mTaskQueue) {
							mSyncRunTask = null;
						}
					}
				}
			} catch (InterruptedException e) {
				Log.w(Constants.LOG_TAG_MAIN, "Interrupted CatalogService task waiting");
			} catch(Exception e){
				Log.e(Constants.LOG_TAG_MAIN, "Failed CatalogService task",e);
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
			fireCatalogMapImportProgress((String)task.getTaskId(),(int)progress,(int)total);
		}
	}
	
	public void onTaskCanceled(BaseTask task){
		if(task instanceof LoadBaseCatalogTask){
			LoadBaseCatalogTask info = (LoadBaseCatalogTask)task;
			int catalogId = info.getCatalogId();
			Catalog catalog = info.getCatalog();
			mCatalogs[catalogId] = catalog;
			fireCatalogChanged(catalogId, catalog);
		}
		if(task instanceof UpdateMapTask){
			fireCatalogMapChanged((String)task.getTaskId());
		}		
		if(mAsyncRunQueue.contains(task)){
			mAsyncRunQueue.remove(task);
		}
	}
	
	public void onTaskFailed(BaseTask task, Throwable reason){
		if(task instanceof LoadBaseCatalogTask){
			LoadBaseCatalogTask info = (LoadBaseCatalogTask)task;
			int catalogId = info.getCatalogId();
			Catalog catalog = info.getCatalog();
			mCatalogs[catalogId] = catalog;
			fireCatalogChanged(catalogId, catalog);
		}
		if(task instanceof UpdateMapTask){
			fireCatalogMapChanged((String)task.getTaskId());
		}
		if(mAsyncRunQueue.contains(task)){
			mAsyncRunQueue.remove(task);
		}
	}
	
	public void onTaskBegin(BaseTask task){
		if(task instanceof UpdateMapTask){
			fireCatalogMapChanged((String)task.getTaskId());
		}
	}
	
	public void onTaskDone(BaseTask task){
		if(task instanceof LoadBaseCatalogTask){
			LoadBaseCatalogTask info = (LoadBaseCatalogTask)task;
			int catalogId = info.getCatalogId();
			Catalog catalog = info.getCatalog();
			mCatalogs[catalogId] = catalog;
			fireCatalogChanged(catalogId, catalog);
		}
		if(task instanceof UpdateMapTask){
			fireCatalogMapChanged((String)task.getTaskId());
			fireCatalogChanged(LOCAL, mCatalogs[LOCAL]);
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
						if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
							Log.w(Constants.LOG_TAG_MAIN, "Reject task " + task.toString() + " due it already queued");
						}
						return false;
					}
				}
				if(mSyncRunTask!=null && mSyncRunTask.getClass().equals(newTaskClass) && taskId.equals( mSyncRunTask.getTaskId() )){
					if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
						Log.w(Constants.LOG_TAG_MAIN, "Reject task " + task.toString() + " due it already runned");
					}
					return false;
				}
				for(BaseTask running : mAsyncRunQueue){
					if(running.getClass().equals(newTaskClass) && taskId.equals( running.getTaskId() )){
						if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
							Log.w(Constants.LOG_TAG_MAIN, "Reject task " + task.toString() + " due it already runned async.");
						}
						return false;
					}
				}
			}
		}
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
			Log.w(Constants.LOG_TAG_MAIN, "Queued task " + task.toString() );
		}
		mTaskQueue.add(task);
		return true;
	}
	
}
