package org.ametro.catalog.storage;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.CatalogMap;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.FileUtil;
import org.ametro.util.IOperationListener;

import android.util.Log;

public class MapImportQueue extends Thread implements IOperationListener {

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
				execute(mMap);
				// remove old map file if exists
				// move model from temporary to persistent file name
//				String mapFileName = GlobalSettings.getLocalCatalogMapFileName(map.getSystemName());
//				if(mapFile.exists()){
//					FileUtil.delete(mapFile);
//				}
				//FileUtil.move(mapFileTemp, mapFile);
				//final String fileName = mapFile.getName().toLowerCase();						
				//CatalogMap imported = CatalogBuilder.extractCatalogMap(mLocalCatalog, mapFile, fileName, model);				
				
			}
		} catch (InterruptedException e) {
		}
	}

	private void execute(CatalogMap map)
	{
		onBegin(map);
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

	public void onBegin(Object context) {
		mListener.onMapImportBegin((CatalogMap)context);
	}

	public void onCanceled(Object context) {
		mMap = null;
		mListener.onMapImportCanceled((CatalogMap)context);
	}

	public void onDone(Object context, File file) {
		mMap = null;
		mListener.onMapImportDone((CatalogMap)context, file);
	}

	public void onFailed(Object context, Throwable reason) {
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
