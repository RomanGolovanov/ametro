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

package org.ametro.catalog.storage.obsolete;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.CatalogMap;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.FileUtil;
import org.ametro.util.IDownloadListener;

import android.util.Log;

public class MapImportQueue extends Thread implements IDownloadListener {

	public static interface IMapImportListener
	{
		void onMapImportBegin(CatalogMap map);
		void onMapImportProgressChanged(CatalogMap map, long progress, long total);
		void onMapImportDone(CatalogMap map, File file);
		void onMapImportFailed(CatalogMap map, Throwable reason);
		void onMapImportCanceled(CatalogMap map);
	}

	private boolean mIsShutdown;
	
	private LinkedBlockingQueue<CatalogMap> mQueue;
	private IMapImportListener mListener;

	private CatalogMap mMap;
	private boolean mCanceled;	
	

	public MapImportQueue(IMapImportListener listener) {
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
				execute(mMap);
			}
		} catch (InterruptedException e) {
		}
	}

	private void execute(CatalogMap map)
	{
		onBegin(map, null);
		onUpdate(map, 0, 100);
		String absoluteFilePath = map.getAbsoluteUrl();
		Model model = ModelBuilder.loadModel(absoluteFilePath);
		onUpdate(map, 50, 100);
		File file = new File(GlobalSettings.getTemporaryImportMapFile(map.getSystemName()));
		FileUtil.delete(file);
		ModelBuilder.saveModel(file.getAbsolutePath(), model);
		onUpdate(map, 100, 100);
		onDone(map, file);
	}

	public void onBegin(Object context, File file) {
		mListener.onMapImportBegin((CatalogMap)context);
	}

	public void onCanceled(Object context, File file) {
		mMap = null;
		mListener.onMapImportCanceled((CatalogMap)context);
	}

	public void onDone(Object context, File file) {
		mMap = null;
		mListener.onMapImportDone((CatalogMap)context, file);
	}

	public void onFailed(Object context, File file, Throwable reason) {
		CatalogMap map = (CatalogMap)context;
		if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
			String message = "Failed import map " + map.getSystemName() + " from catalog " + map.getOwner().getBaseUrl();
			Log.e(Constants.LOG_TAG_MAIN, message, reason);
		}
		mMap = null;
		mListener.onMapImportFailed((CatalogMap)context, reason);
	}

	public boolean onUpdate(Object context, long position, long total) {
		mListener.onMapImportProgressChanged((CatalogMap)context, (int) position, (int) total);
		return !mIsShutdown && !(mCanceled && position<total );
	}
	
}
