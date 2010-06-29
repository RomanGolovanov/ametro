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
import java.util.ArrayList;

import org.ametro.Constants;
import org.ametro.catalog.Catalog;

import android.os.AsyncTask;
import android.util.Log;

public class CatalogStorage implements ICatalogBuilderListener {

	public static final int CATALOG_LOCAL = 0;
	public static final int CATALOG_IMPORT = 1;
	public static final int CATALOG_ONLINE = 2;
	
//	private static CatalogStorage mStorage;
//	
//	public static CatalogStorage getStorage(){
//		if(mStorage==null){
//			synchronized (CatalogStorage.class) {
//				if(mStorage==null){
//					
//					CatalogStorage instance = new CatalogStorage(
//							Constants.LOCAL_CATALOG_STORAGE, Constants.LOCAL_CATALOG_PATH,
//							Constants.IMPORT_CATALOG_STORAGE, Constants.IMPORT_CATALOG_PATH,
//							Constants.ONLINE_CATALOG_STORAGE, Constants.ONLINE_CATALOG_PATH
//							);
//					instance.requestLocalCatalog(false);
//					instance.requestOnlineCatalog(false);
//					instance.requestImportCatalog(false);
//					mStorage = instance;
//					
//				}
//			}
//		}
//		return mStorage;
//	}
	
	public CatalogStorage(File localStorage, File localPath, File importStorage, File importPath, File onlineStorage, String onlineUrl){
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

	public void fireCatalogChanged(int catalogId, Catalog catalog){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogLoaded(catalogId, catalog);
		}
	}

	public void fireCatalogOperationFailed(int catalogId, String message){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogOperationFailed(catalogId, message);
		}
	}

	public void fireCatalogOperationProgress(int catalogId, int progress, int total, String message){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogOperationProgress(catalogId, progress, total, message);
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
	
	/*package*/ ArrayList<ICatalogStorageListener> mCatalogListeners = new ArrayList<ICatalogStorageListener>();

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
	
	public void onCatalogBuilderOperationFailed(CatalogBuilder source, String message) {
		fireCatalogOperationFailed(getCatalogId(source), message);
	}

	public void onCatalogBuilderOperationProgress(CatalogBuilder source, int progress, int total, String message) {
		fireCatalogOperationProgress(getCatalogId(source), progress, total, message);
	}

	public void deleteMap(String mSystemName) {
		// TODO Auto-generated method stub
		
	}

	public void cancelDownload(String mSystemName) {
		// TODO Auto-generated method stub
		
	}

	public void requestDownload(String mSystemName) {
		// TODO Auto-generated method stub
		
	}

	public void cancelImport(String mSystemName) {
		// TODO Auto-generated method stub
		
	}

	public void requestImport(String mSystemName) {
		// TODO Auto-generated method stub
		
	}

//	public IBinder onBind(Intent intent) {
//		return new IBinder() {
//			
//			public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
//				return false;
//			}
//			
//			public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
//				return false;
//			}
//			
//			public IInterface queryLocalInterface(String descriptor) {
//				return null;
//			}
//			
//			public boolean pingBinder() {
//				return false;
//			}
//			
//			public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
//			}
//			
//			public boolean isBinderAlive() {
//				return false;
//			}
//			
//			public String getInterfaceDescriptor() throws RemoteException {
//				return null;
//			}
//			
//			public void dump(FileDescriptor fd, String[] args) throws RemoteException {
//			}
//		}; 
//	}


}
