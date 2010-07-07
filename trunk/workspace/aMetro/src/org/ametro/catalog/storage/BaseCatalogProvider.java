package org.ametro.catalog.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.ametro.Constants;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.util.FileUtil;

import android.util.Log;

public abstract class BaseCatalogProvider {

	public final static int FILE_TYPE_AMETRO = 1;
	public final static int FILE_TYPE_PMETRO = 2;

	public final static String AMETRO_EXTENSION = ".ametro";
	public final static String PMETRO_EXTENSION = ".pmz";
	
	protected Catalog mCatalog;
	protected File mStorage;
	protected ICatalogBuilderListener mListener;
	
	public BaseCatalogProvider(ICatalogBuilderListener listener, File storage)
	{
		mListener = listener;
		mStorage = storage;
	}
	
	public void load(boolean refreshCatalog){
		if(!refreshCatalog && mStorage.exists()){
			loadFromStorage();
		}
		if(mCatalog==null || refreshCatalog){
			refresh();
			if(mCatalog!=null && !mCatalog.isCorrupted()){
				saveToStorage();
			}
		}
		fireCatalogChanged(mCatalog);
	}
	
	public void save(){
		if(mCatalog!=null && !mCatalog.isCorrupted()){
			saveToStorage();
		}
	}
	
	public abstract boolean isDerpecated();
		
	public abstract void refresh();

	protected Catalog getEmptyCatalog()
	{
		return new Catalog(System.currentTimeMillis(), getBaseUrl(), new ArrayList<CatalogMap>());
	}
	
	protected Catalog getCorruptedCatalog(){
		Catalog catalog = getEmptyCatalog();
		catalog.setCorrupted(true);
		return catalog;
	}
	
	protected String getBaseUrl(){
		return mStorage.getAbsolutePath();
	}
	
	protected void loadFromStorage(){
		BufferedInputStream strm = null;
		try{
			strm = new BufferedInputStream(new FileInputStream(mStorage));
			mCatalog = CatalogDeserializer.deserializeCatalog(strm);
		}catch(Exception ex){
			FileUtil.delete(mStorage);
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
				strm = new BufferedOutputStream(new FileOutputStream(mStorage));
				CatalogSerializer.serializeCatalog(mCatalog, strm);
			}catch(Exception ex){
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
					Log.e(Constants.LOG_TAG_MAIN, "Failed save catalog", ex);
				}
				fireOperationFailed("Failed save catalog due error: " + ex.getMessage());
			}finally{
				if(strm!=null){
					try { strm.close(); }catch(IOException ex){}
				}
			}
		}catch(Exception ex){
			fireOperationFailed("Cannot save catalog due error: " + ex.getMessage());
		}		
	}
	
	protected void fireProgressChanged(int progress, int total, String message)
	{
		mListener.onCatalogBuilderProgressChanged(this, progress, total, message);
	}
	
	protected void fireOperationFailed(String message)
	{
		mListener.onCatalogBuilderOperationFailed(this, message);
	}	
	
	protected void fireCatalogChanged(Catalog catalog)
	{
		mListener.onCatalogBuilderCatalogChanged(this, catalog);
	}

	public Catalog getCatalog() {
		return mCatalog;
	}	
	
}
