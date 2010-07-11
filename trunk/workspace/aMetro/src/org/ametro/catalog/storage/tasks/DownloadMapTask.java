package org.ametro.catalog.storage.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.ametro.ApplicationEx;
import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.FileUtil;
import org.ametro.util.IDownloadListener;
import org.ametro.util.WebUtil;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class DownloadMapTask extends UpdateMapTask implements IDownloadListener {

	private Catalog mLocalCatalog;
	private Catalog mOnlineCatalog;
	private Throwable mFailReason;
	
	public DownloadMapTask(String systemName) {
		super(systemName);
	}

	protected void run(Context context) throws Exception {
		final CatalogStorage storage = ApplicationEx.getInstance().getCatalogStorage();
		 mOnlineCatalog = storage.getCatalog(CatalogStorage.ONLINE);
		if(mOnlineCatalog==null){
			throw new CanceledException("No online catalog available");
		}
		mLocalCatalog = storage.getCatalog(CatalogStorage.LOCAL);
		if(mLocalCatalog==null){
			throw new CanceledException("No local catalog available");
		}
		final CatalogMap map = mOnlineCatalog.getMap(mSystemName);
		if(map==null){
			throw new CanceledException("No maps found in import catalog with system name " + mSystemName);
		}
		
		URI uri = URI.create(map.getAbsoluteUrl());
		File file = new File(GlobalSettings.getTemporaryDownloadMapFile(map.getSystemName()));

		WebUtil.downloadFile(map, uri, file, false, this);
		if(mFailReason!=null){
			throw new Exception("Map download failed", mFailReason);
		}
	}
	
	public DownloadMapTask(Parcel in) {
		super(in);
	}

	public static final Parcelable.Creator<DownloadMapTask> CREATOR = new Parcelable.Creator<DownloadMapTask>() {
		public DownloadMapTask createFromParcel(Parcel in) {
			return new DownloadMapTask(in);
		}

		public DownloadMapTask[] newArray(int size) {
			return new DownloadMapTask[size];
		}
	};

	public void onBegin(Object context, File file) {
	}

	public void onCanceled(Object context, File file) {
	}

	public void onDone(Object context, File file) throws IOException {
		final File onlineFile = new File(GlobalSettings.getTemporaryDownloadMapFile(mSystemName));
		final File localFile = new File(GlobalSettings.getLocalCatalogMapFileName(mSystemName));
		
		FileUtil.delete(localFile);
		FileUtil.move(onlineFile, localFile);
		Model model = ModelBuilder.loadModelDescription(localFile.getAbsolutePath());
		CatalogMap localMap = Catalog.extractCatalogMap(mLocalCatalog, localFile, localFile.getName().toLowerCase(), model);
		mLocalCatalog.appendMap(localMap);
		Catalog.save(mLocalCatalog, Constants.LOCAL_CATALOG_STORAGE);
	}

	public void onFailed(Object context, File file, Throwable reason) {
		if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
			String message = "Failed download map " + mSystemName + " from catalog " + mOnlineCatalog.getBaseUrl();
			Log.e(Constants.LOG_TAG_MAIN, message, reason);
		}
		mFailReason = reason;
	}

	public boolean onUpdate(Object context, long position, long total) throws CanceledException {
		cancelCheck();
		update(position, total, mSystemName);
		return true;
	}

}
