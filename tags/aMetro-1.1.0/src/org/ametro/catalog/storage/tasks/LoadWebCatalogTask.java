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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.storage.CatalogDeserializer;
import org.ametro.util.FileUtil;
import org.ametro.util.IDownloadListener;
import org.ametro.util.WebUtil;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import static org.ametro.app.Constants.ONLINE_CATALOG_DEPRECATED_TIMEOUT;

public class LoadWebCatalogTask extends LoadBaseCatalogTask implements IDownloadListener {
	
	private String mCatalogUrl;
	private String[] mCatalogBaseUrls;
	private boolean mCompleted;
	private String mProgressMessage;

	public boolean isDerpecated() {
		if(mCatalog == null) return true;
		if(GlobalSettings.isAutoUpdateIndexEveryHourEnabled(getContext())){
			return System.currentTimeMillis() > (mCatalog.getTimestamp() + ONLINE_CATALOG_DEPRECATED_TIMEOUT);
		}else{
			return false;
		}
	}

	public void refresh() throws Exception {
		mCompleted = false;
		File temp = new File(Constants.TEMP_CATALOG_PATH, "catalog.zip");
		int catalogId = 0;
		final Context ctx = getContext();
		if(ApplicationEx.getInstance().isNetworkAvailable()){
			for(String catalogUrl : mCatalogBaseUrls){
				String url = catalogUrl + mCatalogUrl;
				mProgressMessage = ctx.getString(R.string.msg_try_download_catalog) + " " + ctx.getString(Constants.ONLINE_CATALOG_NAMES[catalogId]); 
				update(0, 0, mProgressMessage);
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
					Log.d(Constants.LOG_TAG_MAIN,"Download web catalog from " + url + " to local file " + temp.getAbsolutePath() );
				}
				try{
					FileUtil.touchDirectory(Constants.TEMP_CATALOG_PATH);
					WebUtil.downloadFileUnchecked(catalogUrl, URI.create(url), temp, this);
					if(mCompleted){
						break;
					} 
				} catch(Exception ex){
					if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
						Log.w(Constants.LOG_TAG_MAIN,"Failed download web catalog from " + url + mCatalogUrl, ex);
					}
				}
				catalogId++;
			}
		}
		if(!mCompleted){
			mCatalog = getCorruptedCatalog();
		}
	}
	
	public LoadWebCatalogTask(int catalogId, File file, String catalogUrl, String[] catalogBaseUrls, boolean forceRefresh) {
		super(catalogId, file, forceRefresh);
		mCatalogUrl = catalogUrl;
		mCatalogBaseUrls = catalogBaseUrls;
	}

	protected LoadWebCatalogTask(Parcel in) {
		super(in);
		mCatalogUrl = in.readString();
		mCatalogBaseUrls = new String[in.readInt()]; 
		in.readStringArray(mCatalogBaseUrls);
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(mCatalogUrl);
		out.writeInt(mCatalogBaseUrls.length);
		out.writeStringArray(mCatalogBaseUrls);
	}
	
	public static final Parcelable.Creator<LoadWebCatalogTask> CREATOR = new Parcelable.Creator<LoadWebCatalogTask>() {
		public LoadWebCatalogTask createFromParcel(Parcel in) {
			return new LoadWebCatalogTask(in);
		}

		public LoadWebCatalogTask[] newArray(int size) {
			return new LoadWebCatalogTask[size];
		}
	};

	public void onBegin(Object context, File file) {
		FileUtil.delete(file);
	}

	public void onDone(Object context, File file) throws Exception {
		try{
			File catalogFile;
			ZipInputStream zip = null;
			String fileName = null;
			try{
				zip = new ZipInputStream(new FileInputStream(file));
				ZipEntry zipEntry = zip.getNextEntry();
				if(zipEntry != null) { 
					fileName = zipEntry.getName();
					final File outputFile = new File(Constants.TEMP_CATALOG_PATH, fileName); 
					FileUtil.writeToStream(new BufferedInputStream(zip), new FileOutputStream(outputFile), false);
					zip.closeEntry();
				}
				zip.close();
				zip = null;
			}finally{
				if(zip != null){
					try{ zip.close(); } catch(Exception e){}
				}
			}
			if(fileName==null){
				throw new Exception("Invalid map catalog archive");
			}
			catalogFile = new File(Constants.TEMP_CATALOG_PATH, fileName);
			mCatalog = CatalogDeserializer.deserializeCatalog(new BufferedInputStream(new FileInputStream(catalogFile)));
			// set timestamp to now for timeout detection 
			mCatalog.setTimestamp(System.currentTimeMillis());
			//mCatalog.setBaseUrl((String)context + mCatalog.getBaseUrl());
			mCompleted = true;
			GlobalSettings.setUpdateDate(getContext(), mCatalog.getTimestamp());
		}catch(Exception ex){
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
				Log.w(Constants.LOG_TAG_MAIN,"Failed extract web catalog from " +(String)context + mCatalogUrl, ex);
			}
		}
	}

	public boolean onUpdate(Object context, long position, long total) throws Exception {
		update(position, total, mProgressMessage);
		cancelCheck(); // can throws CanceledException 
		return true;
	}

	/* We do not use this callbacks */
	public void onFailed(Object context, File file, Throwable reason) {
	}
	
	public void onCanceled(Object context, File file) {
	}
	
}
