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

import org.ametro.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.MapDownloadQueue.IMapDownloadListener;
import org.ametro.catalog.storage.MapImportQueue.IMapImportListener;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.FileUtil;

import android.os.AsyncTask;

public class CatalogStorage implements ICatalogBuilderListener, IMapDownloadListener, IMapImportListener {

	public static final int LOCAL = 0;
	public static final int IMPORT = 1;
	public static final int ONLINE = 2;

	/*package*/ Object mMutex = new Object();
	
	/*package*/ BaseCatalogProvider[] mBuilders;
	/*package*/ CatalogLoadTask[] mCatalogTasks;

	/*package*/ ArrayList<ICatalogStorageListener> mCatalogListeners;
	
	/*package*/ MapDownloadQueue mMapDownloadQueue;
	/*package*/ MapImportQueue mMapImportQueue;
	
	/*package*/ boolean mIsShutdown;
	
	public CatalogStorage(
			File localStorage, File localPath, 
			File importStorage, File importPath, 
			File onlineStorage, String onlineUrl){
		
		this.mCatalogListeners = new ArrayList<ICatalogStorageListener>();

		this.mIsShutdown = false;

		this.mMapDownloadQueue = new MapDownloadQueue(this);
		this.mMapImportQueue = new MapImportQueue(this); 
		
		mBuilders = new BaseCatalogProvider[3];
		
		mBuilders[LOCAL] = new DirectoryCatalogProvider(this, localStorage, localPath);
		mBuilders[IMPORT] = new DirectoryCatalogProvider(this, importStorage, importPath);
		mBuilders[ONLINE] = new WebCatalogProvider(this, onlineStorage, URI.create(onlineUrl), true);
		
		mCatalogTasks = new CatalogLoadTask[3];
		
	}
	
	public void shutdown(){
		mIsShutdown = true;
		mMapImportQueue.shutdown();
		mMapDownloadQueue.shutdown();
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
			return mBuilders[catalogId].getCatalog();
		}
	}	
	
	public void requestCatalog(int catalogId, boolean refresh)
	{
		if(mCatalogTasks[catalogId]==null){
			synchronized (mMutex) {
				if(mCatalogTasks[catalogId]==null){
					mCatalogTasks[catalogId] = new CatalogLoadTask();
					mCatalogTasks[catalogId].execute(catalogId,refresh);
				}
			}
		}	
	}	

	private class CatalogLoadTask extends AsyncTask<Object, Void, Catalog> {
		
		private int mCatalogId;
		
		protected Catalog doInBackground(Object... params) {
			mCatalogId = (Integer)params[0];
			boolean refresh = (Boolean)params[1];
			mBuilders[mCatalogId].load(refresh);
			return mBuilders[mCatalogId].getCatalog();
		}
		
		protected void onPostExecute(Catalog result) {
			synchronized (mMutex) {
				mCatalogTasks[mCatalogId] = null;
			}
			super.onPostExecute(result);
		}
	}

	private int getCatalogId(BaseCatalogProvider source) {
		if(source == mBuilders[IMPORT]){
			return IMPORT;
		}else if(source == mBuilders[ONLINE]){
			return ONLINE;
		}else if(source == mBuilders[LOCAL]){
			return LOCAL;
		}
		throw new RuntimeException("Unknown CatalogBuilder instance");
	}

	public int getOnlineCatalogState(CatalogMap local, CatalogMap remote) {
		if(mMapDownloadQueue.isProcessed(remote)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mMapDownloadQueue.isPending(remote)){
			return CatalogMapState.DOWNLOAD_PENDING;
		}
		
		if (remote.isNotSupported()) {
			if (local == null || local.isNotSupported() || local.isCorruted()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.UPDATE_NOT_SUPPORTED;
			}
		} else {
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
		if(mMapImportQueue.isProcessed(remote)){
			return CatalogMapState.IMPORTING;
		}
		if(mMapImportQueue.isPending(remote)){
			return CatalogMapState.IMPORT_PENDING;
		}
		
		if (remote.isCorruted()) {
			return CatalogMapState.CORRUPTED;
		} else {
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
		if(mMapDownloadQueue.isProcessed(remote)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mMapDownloadQueue.isPending(remote)){
			return CatalogMapState.DOWNLOAD_PENDING;
		}

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

	
	
	public void deleteLocalMap(String systemName) {
		synchronized (mMutex) {
			if(mBuilders[LOCAL].getCatalog()!=null && !mBuilders[LOCAL].getCatalog().isCorrupted()){
				CatalogMap map = mBuilders[LOCAL].getCatalog().getMap(systemName);
				if(map!=null ){
					mBuilders[LOCAL].getCatalog().deleteMap(map);
					mBuilders[LOCAL].save();
					FileUtil.delete(map.getAbsoluteUrl());
					fireCatalogMapChanged(systemName);
					fireCatalogChanged(LOCAL, mBuilders[LOCAL].getCatalog());
				}
				
			}
		}
	}

	public void deleteImportMap(String systemName) {
		synchronized (mMutex) {
			if(mBuilders[IMPORT].getCatalog()!=null && !mBuilders[IMPORT].getCatalog().isCorrupted()){
				CatalogMap map = mBuilders[IMPORT].getCatalog().getMap(systemName);
				if(map!=null ){
					mBuilders[IMPORT].getCatalog().deleteMap(map);
					mBuilders[IMPORT].save();
					FileUtil.delete(map.getAbsoluteUrl());
					fireCatalogMapChanged(systemName);
					fireCatalogChanged(IMPORT, mBuilders[IMPORT].getCatalog());
				}
				
			}
		}
	}
	
	public void cancelDownload(String systemName) {
		synchronized (mMutex) {
			if(mBuilders[ONLINE].getCatalog()!=null && !mBuilders[ONLINE].getCatalog().isCorrupted()){
				CatalogMap map = mBuilders[ONLINE].getCatalog().getMap(systemName);
				if(map!=null){
					mMapDownloadQueue.cancel(map);
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

	public void requestDownload(String systemName) {
		synchronized (mMutex) {
			if(mBuilders[ONLINE].getCatalog()!=null && !mBuilders[ONLINE].getCatalog().isCorrupted()){
				CatalogMap map = mBuilders[ONLINE].getCatalog().getMap(systemName);
				if(map!=null){
					mMapDownloadQueue.request(map);
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

	public void cancelImport(String systemName) {
		synchronized (mMutex) {
			if(mBuilders[IMPORT].getCatalog()!=null && !mBuilders[IMPORT].getCatalog().isCorrupted()){
				CatalogMap map = mBuilders[IMPORT].getCatalog().getMap(systemName);
				if(map!=null){
					mMapImportQueue.cancel(map);
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
	}

	public void requestImport(String systemName) {
		synchronized (mMutex) {
			if(mBuilders[IMPORT].getCatalog()!=null && !mBuilders[IMPORT].getCatalog().isCorrupted()){
				CatalogMap map = mBuilders[IMPORT].getCatalog().getMap(systemName);
				if(map!=null){
					mMapImportQueue.request(map);
					fireCatalogMapChanged(map.getSystemName());
				}
			}
		}
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
			CatalogMap downloaded = Catalog.extractCatalogMap(mBuilders[LOCAL].getCatalog(), local, local.getName().toLowerCase(), model);
			mBuilders[LOCAL].getCatalog().appendMap(downloaded);
			mBuilders[LOCAL].save();
		}
		fireCatalogChanged(LOCAL, mBuilders[LOCAL].getCatalog());
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


	public void onMapImportBegin(CatalogMap map) {
		String systemName = map.getSystemName();
		fireCatalogMapChanged(systemName);
	}

	public void onMapImportCanceled(CatalogMap map) {
		String systemName = map.getSystemName();
		fireCatalogMapChanged(systemName);
	}

	public void onMapImportDone(CatalogMap map, File file) {
		String systemName = map.getSystemName();
		File local = new File(GlobalSettings.getLocalCatalogMapFileName(map.getSystemName()));
		FileUtil.delete(local);
		FileUtil.move(file, local);
		Model model = ModelBuilder.loadModelDescription(local.getAbsolutePath());
		synchronized(mMutex){
			CatalogMap downloaded = Catalog.extractCatalogMap(mBuilders[LOCAL].getCatalog(), local, local.getName().toLowerCase(), model);
			mBuilders[LOCAL].getCatalog().appendMap(downloaded);
			mBuilders[LOCAL].save();
		}
		fireCatalogChanged(LOCAL, mBuilders[LOCAL].getCatalog());
		fireCatalogMapChanged(systemName);
	}

	public void onMapImportFailed(CatalogMap map, Throwable reason) {
		String systemName = map.getSystemName();
		fireCatalogMapImportFailed(systemName,reason);
		fireCatalogMapChanged(systemName);
	}

	public void onMapImportProgressChanged(CatalogMap map, long progress, long total) {
		String systemName = map.getSystemName();
		fireCatalogMapImportProgress(systemName, (int)progress, (int)total);
	}
	
	public void onCatalogBuilderCatalogChanged(BaseCatalogProvider source, Catalog catalog) {
		fireCatalogChanged(getCatalogId(source), catalog);
	}

	public void onCatalogBuilderProgressChanged(BaseCatalogProvider source, int progress, int total, String message) {
		fireCatalogOperationProgress(getCatalogId(source), progress, total, message);
	}

	public void onCatalogBuilderOperationFailed(BaseCatalogProvider source, String message) {
		fireCatalogOperationFailed(getCatalogId(source), message);
	}	
}
