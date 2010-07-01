package org.ametro.catalog.storage;

import java.io.File;
import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;

import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.CatalogMap;
import org.ametro.util.IOperationListener;
import org.ametro.util.WebUtil;

import android.util.Log;

public class MapDownloadQueue extends Thread implements IOperationListener {

	public static interface IMapDownloadListener
	{
		void onMapDownloadBegin(CatalogMap map);
		void onMapDownloadProgressChanged(CatalogMap map, long progress, long total);
		void onMapDownloadDone(CatalogMap map, File file);
		void onMapDownloadFailed(CatalogMap map, Throwable reason);
		void onMapDownloadCanceled(CatalogMap map);
	}
	

	private boolean mIsShutdown;
	
	private LinkedBlockingQueue<CatalogMap> mQueue;
	private IMapDownloadListener mListener;

	private CatalogMap mMap;
	private boolean mCanceled;	

	public MapDownloadQueue(IMapDownloadListener listener) {
		mListener = listener;
		mQueue = new LinkedBlockingQueue<CatalogMap>();
		this.start();
	}

	public void shutdown() {
		mIsShutdown = true;
		this.interrupt();
	}

	public boolean isPending(CatalogMap map) {
		return mQueue.contains(map);
	}

	public boolean isProcessed(CatalogMap map) {
		return mMap == map;
	}

	public void request(CatalogMap remote) {
		if (remote != null) {
			mQueue.offer(remote);
		}
	}

	public void cancel(CatalogMap remote) {
		if(mMap == remote){
			mCanceled = true;
		}else if(mQueue.contains(remote)) {
			mQueue.remove(remote);
		}
	}

	public void run() {
		try {
			while (!mIsShutdown) {
				mMap = mQueue.take();
				mCanceled = false;
				URI uri = URI.create(mMap.getAbsoluteUrl());
				File file = new File(GlobalSettings.getTemporaryDownloadMapFile(mMap.getSystemName()));
				WebUtil.downloadFile(mMap, uri, file, this);
			}
		} catch (InterruptedException e) {
		}
	}

	public void onBegin(Object context) {
		mListener.onMapDownloadBegin((CatalogMap)context);
	}

	public void onCanceled(Object context) {
		mMap = null;
		mListener.onMapDownloadCanceled((CatalogMap)context);
	}

	public void onDone(Object context, File file) {
		mMap = null;
		mListener.onMapDownloadDone((CatalogMap)context, file);
	}

	public void onFailed(Object context, Throwable reason) {
		CatalogMap map = (CatalogMap)context;
		if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
			String message = "Failed download map " + map.getSystemName() + " from catalog " + map.getOwner().getBaseUrl();
			Log.e(Constants.LOG_TAG_MAIN, message, reason);
		}
		mMap = null;
		mListener.onMapDownloadFailed((CatalogMap)context, reason);
	}

	public boolean onUpdate(Object context, long position, long total) {
		mListener.onMapDownloadProgressChanged((CatalogMap)context, (int) position, (int) total);
		return !mIsShutdown && !(mCanceled && position<total );
	}
	
}
