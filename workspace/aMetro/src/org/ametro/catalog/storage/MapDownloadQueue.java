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
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.CatalogMap;
import org.ametro.util.IDownloadListener;
import org.ametro.util.WebUtil;

import android.util.Log;

public class MapDownloadQueue extends Thread implements IDownloadListener {

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
		if(map == null) return false;
		return mQueue.contains(map);
	}

	public boolean isProcessed(CatalogMap map) {
		if(map == null) return false;
		return mMap == map;
	}

	public boolean isPending(String systemName) {
		if(systemName == null) return false;
		Iterator<CatalogMap> iterator = mQueue.iterator();
		while(iterator.hasNext()){
			CatalogMap map = iterator.next();
			if(map.getSystemName().equals(systemName)){
				return true;
			}
		}
		return false;
	}	
	public boolean isProcessed(String systemName) {
		if(systemName == null) return false;
		synchronized (mQueue) {
			return mMap != null && systemName.equals(mMap.getSystemName() );
		}
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

	public void onBegin(Object context, File file) {
		mListener.onMapDownloadBegin((CatalogMap)context);
	}

	public void onCanceled(Object context, File file) {
		mMap = null;
		mListener.onMapDownloadCanceled((CatalogMap)context);
	}

	public void onDone(Object context, File file) {
		mMap = null;
		mListener.onMapDownloadDone((CatalogMap)context, file);
	}

	public void onFailed(Object context, File file, Throwable reason) {
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
