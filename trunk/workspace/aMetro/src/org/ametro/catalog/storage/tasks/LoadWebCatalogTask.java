package org.ametro.catalog.storage.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ametro.Constants;
import org.ametro.catalog.storage.CatalogDeserializer;
import org.ametro.util.FileUtil;
import org.ametro.util.IDownloadListener;
import org.ametro.util.WebUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class LoadWebCatalogTask extends LoadBaseCatalogTask implements IDownloadListener {

	protected static final long DEPRECATED_TIMEOUT =  15*60*1000;
	
	protected final URI mURI;

	public boolean isDerpecated() {
		if(mCatalog == null) return true;
		return System.currentTimeMillis() > (mCatalog.getTimestamp() + DEPRECATED_TIMEOUT);
	}

	public void refresh() throws Exception {
		WebUtil.downloadFileUnchecked(null, mURI, new File(Constants.TEMP_CATALOG_PATH, "catalog.zip"), this);
	}

	public LoadWebCatalogTask(int catalogId, File file, URI uri, boolean forceRefresh) {
		super(catalogId, file, forceRefresh);
		mURI = uri;
	}

	protected LoadWebCatalogTask(Parcel in) {
		super(in);
		mURI = URI.create(in.readString());
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(mURI.toString());
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
	}

	public boolean onUpdate(Object context, long position, long total) throws Exception {
		update(position,total, "");
		cancelCheck(); // can throws CanceledException 
		return true;
	}

	/* We do not use this callbacks */
	public void onFailed(Object context, File file, Throwable reason) {
	}
	
	public void onCanceled(Object context, File file) {
	}
	
}
