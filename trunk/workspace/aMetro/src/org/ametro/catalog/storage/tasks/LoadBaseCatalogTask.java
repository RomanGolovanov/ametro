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
package org.ametro.catalog.storage.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.CatalogDeserializer;
import org.ametro.catalog.storage.CatalogSerializer;
import org.ametro.util.FileUtil;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.util.Log;

public abstract class LoadBaseCatalogTask extends BaseTask {

	/* PERSISTENT STATE */
	protected final File mFile;
	protected final boolean mForceRefresh;
	protected final int mCatalogId;
	
	/* TRANSITITION STATE */
	protected Catalog mCatalog;
	
	public abstract boolean isDerpecated();
	public abstract void refresh() throws Exception;
	
	public boolean isAsync() {
		return false;
	}
	
	public Object getTaskId() {
		return mCatalogId;
	}
	
	public LoadBaseCatalogTask(int catalogId, File file, boolean forceRefresh){
		this.mCatalogId = catalogId;
		this.mFile = file;
		this.mForceRefresh = forceRefresh;
	}

	protected LoadBaseCatalogTask(Parcel in) {
		mCatalogId = in.readInt();
		mForceRefresh = in.readInt()!=0;
		mFile = new File(in.readString());
	}
	
	protected void begin() {
		FileUtil.touchDirectory(Constants.ROOT_PATH);
		super.begin();
	}

	protected void failed(Throwable reason) {
		mCatalog = getCorruptedCatalog();
		super.failed(reason);
	}
	
	protected void run(Context context) throws Exception{
		Catalog backup = ApplicationEx.getInstance().getCatalogStorage().getCatalog(getCatalogId());
		
		final Resources res = context.getResources();
		if(mCatalog==null || mForceRefresh){
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN,"Begin load catalog " + mCatalogId);
			}
			if(!mForceRefresh && mFile.exists()){
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
					Log.d(Constants.LOG_TAG_MAIN,"Load catalog storage " + mCatalogId);
				}
				update(0, 0, res.getString(R.string.msg_init_catalog));
				loadFromStorage();
				if(mCatalog!=null && !mCatalog.isCorrupted()){
					backup = mCatalog;
				}
			}
			if(mCatalog==null || mForceRefresh || isDerpecated()){
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
					Log.d(Constants.LOG_TAG_MAIN,"Need refresh catalog " + mCatalogId);
				}
				refresh();
				if(mCatalog!=null && !mCatalog.isCorrupted()){
					update(0, 0, res.getString(R.string.msg_save_catalog));
					saveToStorage();
				}
				if((mCatalog==null || mCatalog.isCorrupted()) && backup!=null && !backup.isCorrupted()){
					if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
						Log.d(Constants.LOG_TAG_MAIN,"Restore storage version of catalog " + mCatalogId);
					}
					mCatalog = backup;
				}
			}		
		}
	}

	protected Catalog getEmptyCatalog()
	{
		return new Catalog(System.currentTimeMillis(), getBaseUrl(), new ArrayList<CatalogMap>());
	}
	
	protected Catalog getCorruptedCatalog(){
		Catalog catalog = getEmptyCatalog();
		catalog.setMaps(new ArrayList<CatalogMap>());
		catalog.setCorrupted(true);
		return catalog;
	}
	
	protected String getBaseUrl(){
		return mFile.getAbsolutePath();
	}
	
	protected void loadFromStorage(){
		BufferedInputStream strm = null;
		try{
			strm = new BufferedInputStream(new FileInputStream(mFile));
			mCatalog = CatalogDeserializer.deserializeCatalog(strm);
		}catch(Exception ex){
			FileUtil.delete(mFile);
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}		
	}
	
	protected void saveToStorage(){
		try{
			BufferedOutputStream strm = null;
			try{
				strm = new BufferedOutputStream(new FileOutputStream(mFile));
				CatalogSerializer.serializeCatalog(mCatalog, strm);
			}catch(Exception ex){
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
					Log.e(Constants.LOG_TAG_MAIN, "Failed save catalog", ex);
				}
				failed(ex);
			}finally{
				if(strm!=null){
					try { strm.close(); }catch(IOException ex){}
				}
			}
		}catch(Exception ex){
			failed(ex);
		}		
	}
	
	public Catalog getCatalog() {
		return mCatalog;
	}	
	
	public int getCatalogId(){
		return mCatalogId;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mCatalogId);
		out.writeInt(mForceRefresh ? 1 : 0);
		out.writeString(mFile.getAbsolutePath());
	}

}
