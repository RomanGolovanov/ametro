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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapState;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.FileUtil;
import org.ametro.util.WebUtil;

import android.os.AsyncTask;
import android.util.Log;

public class CatalogStorage implements ICatalogBuilderListener {

	public static final int CATALOG_LOCAL = 0;
	public static final int CATALOG_IMPORT = 1;
	public static final int CATALOG_ONLINE = 2;

	/*package*/ File mLocalStorage;
	/*package*/ File mLocalPath;
	/*package*/ File mImportStorage;
	/*package*/ File mImportPath;
	/*package*/ File mOnlineStorage;
	/*package*/ String mOnlineUrl;
	
	/*package*/ Object mMutex = new Object();
	/*package*/ LocalCatalogLoadTask mLoadLocalCatalogTask;
	/*package*/ ImportCatalogLoadTask mLoadImportCatalogTask;
	/*package*/ OnlineCatalogLoadTask mLoadOnlineCatalogTask;
	
	/*package*/ Catalog mLocalCatalog;
	/*package*/ Catalog mOnlineCatalog;
	/*package*/ Catalog mImportCatalog;

	/*package*/ Catalog mPreviousLocalCatalog;
	/*package*/ Catalog mPreviousOnlineCatalog;
	/*package*/ Catalog mPreviousImportCatalog;
	
	/*package*/ CatalogBuilder mLocalCatalogBuilder;
	/*package*/ CatalogBuilder mOnlineCatalogBuilder;
	/*package*/ CatalogBuilder mImportCatalogBuilder;
	
	/*package*/ ArrayList<ICatalogStorageListener> mCatalogListeners;
	
	/*package*/ Queue<CatalogMap> mDownloadQueue;
	/*package*/ Queue<CatalogMap> mImportQueue;
	
	/*package*/ CatalogMap mDownloadingMap;
	/*package*/ CatalogMap mImportingMap;
	
	/*package*/ boolean mIsShutdown;
	
	public CatalogStorage(File localStorage, File localPath, File importStorage, File importPath, File onlineStorage, String onlineUrl){
		this.mCatalogListeners = new ArrayList<ICatalogStorageListener>();
		this.mDownloadQueue = new LinkedList<CatalogMap>();
		this.mImportQueue = new LinkedList<CatalogMap>();
		this.mLocalStorage = localStorage;
		this.mLocalPath = localPath;
		this.mImportStorage = importStorage;
		this.mImportPath = importPath;
		this.mOnlineStorage = onlineStorage;
		this.mOnlineUrl = onlineUrl;
		
		this.mOnlineCatalogBuilder = new CatalogBuilder();
		this.mOnlineCatalogBuilder.addOnCatalogBuilderEvents(this);
		
		this.mLocalCatalogBuilder = new CatalogBuilder();
		this.mLocalCatalogBuilder.addOnCatalogBuilderEvents(this);
		
		this.mImportCatalogBuilder = new CatalogBuilder();
		this.mImportCatalogBuilder.addOnCatalogBuilderEvents(this);
		
		this.mIsShutdown = false;

		this.mDownloadSignal = new Object();
		this.mDownloadThread = new Thread(mDownloadRunnable);
		this.mDownloadThread.start();
		
		this.mImportSignal = new Object();
		this.mImportThread = new Thread(mImportRunnable);
		this.mImportThread.start();
		
	}
	
	private Object mImportSignal;
	private Thread mImportThread;
	private Runnable mImportRunnable = new Runnable() {
		public void run() {
			while(!mIsShutdown){
				CatalogMap map = null;
				CatalogMap imported = null;
				String systemName = null;
				synchronized (mMutex) {
					map = mImportQueue.poll();
					if(map!=null){
						systemName = map.getSystemName();
						mImportingMap = map;
						fireCatalogMapChanged(systemName);
					}
				}
				if(map!=null){
					try {
						// load model from PMZ file 
						Model model = ModelBuilder.loadModel(map.getAbsoluteUrl());
						
						String mapFileName = GlobalSettings.getLocalCatalogMapFileName(map.getSystemName());
						String mapFileNameTemp = GlobalSettings.getTemporaryImportMapFile(map.getSystemName());
						
						File mapFile = new File(mapFileName);
						File mapFileTemp = new File(mapFileNameTemp);
						// remove temporary file is exists 
						if(mapFileTemp.exists()){
							FileUtil.delete(mapFileTemp);
						}
						// serialize model into temporary file
						ModelBuilder.saveModel(mapFileTemp.getAbsolutePath(), model);
						// remove old map file if exists
						if(mapFile.exists()){
							FileUtil.delete(mapFile);
						}
						// move model from temporary to persistent file name
						FileUtil.move(mapFileTemp, mapFile);
						
						final String fileName = mapFile.getName().toLowerCase();						
						imported = CatalogBuilder.extractCatalogMap(mLocalCatalog, mapFile, fileName, model);
						
					} catch (Throwable e) {
						if(Log.isLoggable(Constants.LOG_TAG_MAIN,Log.ERROR)){
							Log.e(Constants.LOG_TAG_MAIN, "Failed Import map " + systemName + " from catalog " + map.getOwner().getBaseUrl(),e);
						}
						fireCatalogMapImportFailed(systemName, e);
					}
					synchronized (mMutex) {
						mImportingMap = null;
						if(imported!=null){
							mLocalCatalog.addMap(imported);
							mLocalCatalogBuilder.saveCatalog(mLocalStorage, mLocalCatalog);
							fireCatalogChanged(CATALOG_LOCAL, mLocalCatalog);
						}
						fireCatalogMapChanged(systemName);
					}
				}
				if(!mIsShutdown){
					try {
						synchronized (mImportSignal) {
							mImportSignal.wait();
						}
					} catch (InterruptedException e) {
					}
				}
			}
		}
	};

	private Object mDownloadSignal;
	private Thread mDownloadThread;
	private Runnable mDownloadRunnable = new Runnable() {
		public void run() {
			while(!mIsShutdown){
				CatalogMap map = null;
				String systemName = null;
				CatalogMap downloaded = null;
				synchronized (mMutex) {
					map = mDownloadQueue.poll();
					if(map!=null){
						systemName = map.getSystemName();
						mDownloadingMap = map;
						fireCatalogMapChanged(systemName);
					}
				}
				if(map!=null){
					try {
						File tempFile = new File(GlobalSettings.getTemporaryDownloadMapFile(systemName));
						File localFile = new File(GlobalSettings.getLocalCatalogMapFileName(systemName));
						WebUtil.downloadFile(map.getAbsoluteUrl(), tempFile.getAbsolutePath());
						Model model = ModelBuilder.loadModelDescription(localFile.getAbsolutePath());
						FileUtil.move( tempFile, localFile );

						final String fileName = localFile.getName().toLowerCase();						
						downloaded = CatalogBuilder.extractCatalogMap(mLocalCatalog, localFile, fileName, model);
					} catch (IOException e) {
						if(Log.isLoggable(Constants.LOG_TAG_MAIN,Log.ERROR)){
							Log.e(Constants.LOG_TAG_MAIN, "Failed download map " + systemName + " from catalog " + map.getOwner().getBaseUrl(),e);
						}
						fireCatalogMapDownloadFailed(systemName, e);
					}
					synchronized (mMutex) {
						mDownloadingMap = null;
						if(downloaded!=null){
							mLocalCatalog.addMap(downloaded);
							mLocalCatalogBuilder.saveCatalog(mLocalStorage, mLocalCatalog);
							fireCatalogChanged(CATALOG_LOCAL, mLocalCatalog);
						}
						fireCatalogMapChanged(systemName);
					}
				}
				
				if(!mIsShutdown){
					try {
						synchronized (mDownloadSignal) {
							mDownloadSignal.wait();
						}
					} catch (InterruptedException e) {
					}
				}
			}
		}
	};	
	public void shutdown(){
		mIsShutdown = true;
		fireDownloadSignal();
	}

	public Object getSync(){
		return mMutex;
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
			listener.onCatalogOperationFailed(catalogId, message);
		}
	}

	/*package*/ void fireCatalogOperationProgress(int catalogId, int progress, int total, String message){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogOperationProgress(catalogId, progress, total, message);
		}
	}
	
	/*package*/ void fireCatalogMapChanged(String systemName){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogMapChanged(systemName);
		}
	}
	
	/*package*/ void fireCatalogMapDownloadFailed(String systemName, Throwable e) {
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.fireCatalogMapDownloadFailed(systemName, e);
		}
	}
	
	/*package*/ void fireCatalogMapImportFailed(String systemName, Throwable e) {
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.fireCatalogMapImportFailed(systemName, e);
		}
	}


	
	/*package*/ void fireImportSignal(){
		synchronized (mImportSignal) {
			mImportSignal.notify();
		}
	}

	/*package*/ void fireDownloadSignal(){
		synchronized (mDownloadSignal) {
			mDownloadSignal.notify();
		}
	}
	
	public Catalog getLocalCatalog() {
		synchronized (mMutex) {
			return mLocalCatalog;
		}
	}	
	
	public Catalog getOnlineCatalog() {
		synchronized (mMutex) {
			return mOnlineCatalog;
		}
	}
	
	public Catalog getImportCatalog() {
		synchronized (mMutex) {
			return mImportCatalog;
		}
	}	
	
	public void requestLocalCatalog(boolean refresh)
	{
		if(mLoadLocalCatalogTask==null){
			synchronized (mMutex) {
				if(mLoadLocalCatalogTask==null){
					mLoadLocalCatalogTask = new LocalCatalogLoadTask();
					mLoadLocalCatalogTask.execute(refresh);
				}
			}
		}		
	}
	
	public void requestImportCatalog(boolean refresh)
	{
		if(mLoadImportCatalogTask==null){
			synchronized (mMutex) {
				if(mLoadImportCatalogTask==null){
					mLoadImportCatalogTask = new ImportCatalogLoadTask();
					mLoadImportCatalogTask.execute(refresh);
				}
			}
		}		
	}
	
	public void requestOnlineCatalog(boolean refresh)
	{
		if(mLoadOnlineCatalogTask==null){
			synchronized (mMutex) {
				if(mLoadOnlineCatalogTask==null){
					mLoadOnlineCatalogTask = new OnlineCatalogLoadTask();
					mLoadOnlineCatalogTask.execute(refresh);
				}
			}
		}	
	}	

	private class OnlineCatalogLoadTask extends AsyncTask<Boolean, Void, Catalog> {
		protected Catalog doInBackground(Boolean... params) {
			return mOnlineCatalogBuilder.downloadCatalog(mOnlineStorage, mOnlineUrl, params[0]);
		}
		
		protected void onPreExecute() {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Requested online catalog");
			}
			synchronized(mMutex)
			{
				mPreviousOnlineCatalog = mOnlineCatalog;
				mOnlineCatalog = null;
			}
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Online catalog request response: " + result!=null ? result.toString() : "null");
			}
			synchronized(mMutex)
			{
				if(result!=null){
					mOnlineCatalog = result;
				}else{
					mOnlineCatalog = mPreviousOnlineCatalog;
				}
				mLoadOnlineCatalogTask = null;
			}
			fireCatalogChanged(CATALOG_ONLINE, result);
			super.onPostExecute(result);
		}
		
	}	
	
	private class LocalCatalogLoadTask extends AsyncTask<Boolean, Void, Catalog> {
		protected Catalog doInBackground(Boolean... params) {
			return mLocalCatalogBuilder.loadCatalog(mLocalStorage, mLocalPath, params[0], CatalogBuilder.FILE_TYPE_AMETRO);
		}
		
		protected void onPreExecute() {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Requested local catalog");
			}
			synchronized(mMutex)
			{
				mPreviousLocalCatalog = mLocalCatalog;
				mLocalCatalog = null;
			}
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Local catalog request response: " + result!=null ? result.toString() : "null");
			}
			synchronized(mMutex)
			{
				if(result!=null){
					mLocalCatalog = result;
				}else{
					mLocalCatalog = mPreviousLocalCatalog;
				}
				mLoadLocalCatalogTask = null;
			}			
			fireCatalogChanged(CATALOG_LOCAL, result);
			super.onPostExecute(result);
		}
	}	
	
	private class ImportCatalogLoadTask extends AsyncTask<Boolean, Void, Catalog> {
		protected Catalog doInBackground(Boolean... params) {
			return mImportCatalogBuilder.loadCatalog(mImportStorage, mImportPath, params[0], CatalogBuilder.FILE_TYPE_PMETRO);
		}
		
		protected void onPreExecute() {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Requested import catalog");
			}
			synchronized(mMutex)
			{
				mPreviousImportCatalog = mImportCatalog;
				mImportCatalog = null;
			}			
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Import catalog request response: " + result!=null ? result.toString() : "null");
			}
			synchronized(mMutex)
			{
				if(result!=null){
					mImportCatalog = result;
				}else{
					mImportCatalog = mPreviousImportCatalog;
				}
				mLoadImportCatalogTask = null;
			}	
			fireCatalogChanged(CATALOG_IMPORT, result);
			super.onPostExecute(result);
		}
		
	}

	private int getCatalogId(CatalogBuilder source) {
		if(source == mImportCatalogBuilder){
			return CATALOG_IMPORT;
		}else if(source == mOnlineCatalogBuilder){
			return CATALOG_ONLINE;
		}else if(source == mLocalCatalogBuilder){
			return CATALOG_LOCAL;
		}
		throw new RuntimeException("Unknown CatalogBuilder instance");
	}

	public int getOnlineCatalogState(CatalogMap local, CatalogMap remote) {
		if (remote.isNotSupported()) {
			if (local == null || local.isNotSupported() || local.isCorruted()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.UPDATE_NOT_SUPPORTED;
			}
		} else {
			if(mDownloadingMap == remote){
				return CatalogMapState.DOWNLOADING;
			}
			if(mDownloadQueue.contains(remote)){
				return CatalogMapState.DOWNLOAD_PENDING;
			}
			
			if (local == null) {
				return CatalogMapState.DOWNLOAD;
			} else if (local.isNotSupported() || local.isCorruted()) {
				return CatalogMapState.NEED_TO_UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return CatalogMapState.INSTALLED;
				} else {
					return CatalogMapState.UPDATE;
				}
			}
		}
	}

	public int getImportCatalogState(CatalogMap local, CatalogMap remote) {
		if (remote.isCorruted()) {
			return CatalogMapState.CORRUPTED;
		} else {
			if(mImportingMap == remote){
				return CatalogMapState.IMPORTING;
			}
			if(mImportQueue.contains(remote)){
				return CatalogMapState.IMPORT_PENDING;
			}
			
			if (local == null) {
				return CatalogMapState.IMPORT;
			} else if (!local.isSupported() || local.isCorruted()) {
				return CatalogMapState.UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return CatalogMapState.INSTALLED;
				} else {
					return CatalogMapState.UPDATE;
				}
			}
		}
	}

	public int getLocalCatalogState(CatalogMap local, CatalogMap remote) {
		if (remote == null) {
			// remote not exist
			if (local.isCorruted()) {
				return CatalogMapState.CORRUPTED;
			} else if (!local.isSupported()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.OFFLINE;
			}
		} else if (!remote.isSupported()) {
			// remote not supported
			if (local.isCorruted()) {
				return CatalogMapState.CORRUPTED;
			} else if (!local.isSupported()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.INSTALLED;
			}
		} else {
			// remote OK
			if(mDownloadingMap == remote){
				return CatalogMapState.DOWNLOADING;
			}
			if(mDownloadQueue.contains(remote)){
				return CatalogMapState.DOWNLOAD_PENDING;
			}
			
			if (local.isCorruted()) {
				return CatalogMapState.NEED_TO_UPDATE;
			} else if (!local.isSupported()) {
				return CatalogMapState.NEED_TO_UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return CatalogMapState.INSTALLED;
				} else {
					return CatalogMapState.UPDATE;
				}
			}
		}
	}	
	
	public void onCatalogBuilderOperationFailed(CatalogBuilder source, String message) {
		fireCatalogOperationFailed(getCatalogId(source), message);
	}

	public void onCatalogBuilderOperationProgress(CatalogBuilder source, int progress, int total, String message) {
		fireCatalogOperationProgress(getCatalogId(source), progress, total, message);
	}
	
	public void deleteLocalMap(String systemName) {
		synchronized (mMutex) {
			if(mLocalCatalog!=null && !mLocalCatalog.isCorrupted()){
				CatalogMap map = mLocalCatalog.getMap(systemName);
				if(map!=null ){
					mLocalCatalog.deleteMap(map);
					mLocalCatalogBuilder.saveCatalog(mLocalStorage, mLocalCatalog);
					FileUtil.delete(map.getAbsoluteUrl());
					fireCatalogChanged(CATALOG_LOCAL, mLocalCatalog);
				}
				
			}
		}
	}

	public void deleteImportMap(String systemName) {
		synchronized (mMutex) {
			if(mImportCatalog!=null && !mImportCatalog.isCorrupted()){
				CatalogMap map = mImportCatalog.getMap(systemName);
				if(map!=null ){
					mImportCatalog.deleteMap(map);
					mImportCatalogBuilder.saveCatalog(mImportStorage, mImportCatalog);
					FileUtil.delete(map.getAbsoluteUrl());
					fireCatalogChanged(CATALOG_IMPORT, mImportCatalog);
				}
				
			}
		}
	}
	
	public void cancelDownload(String systemName) {
		synchronized (mMutex) {
			if(mOnlineCatalog!=null && !mOnlineCatalog.isCorrupted()){
				CatalogMap map = mOnlineCatalog.getMap(systemName);
				if(map!=null && !mDownloadQueue.contains(map)){
					mDownloadQueue.offer(map);
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

	public void requestDownload(String systemName) {
		synchronized (mMutex) {
			if(mOnlineCatalog!=null && !mOnlineCatalog.isCorrupted()){
				CatalogMap map = mOnlineCatalog.getMap(systemName);
				if(map!=null && !mDownloadQueue.contains(map)){
					mDownloadQueue.offer(map);
					fireDownloadSignal();
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

	public void cancelImport(String systemName) {
		synchronized (mMutex) {
			if(mImportCatalog!=null && !mImportCatalog.isCorrupted()){
				CatalogMap map = mImportCatalog.getMap(systemName);
				if(map!=null && !mImportQueue.contains(map)){
					mImportQueue.poll();
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

	public void requestImport(String systemName) {
		synchronized (mMutex) {
			if(mImportCatalog!=null && !mImportCatalog.isCorrupted()){
				CatalogMap map = mImportCatalog.getMap(systemName);
				if(map!=null && !mImportQueue.contains(map)){
					mImportQueue.offer(map);
					fireImportSignal();
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

}
