package org.ametro.catalog.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import org.ametro.Constants;
import org.ametro.util.FileUtil;
import org.ametro.util.IOperationListener;
import org.ametro.util.WebUtil;

import android.util.Log;

public class WebCatalogProvider extends BaseCatalogProvider implements IOperationListener {

	protected final URI mURI;
	protected final File mTempFile;
	protected final long mDeprecatedTimeout;
	
	public WebCatalogProvider(ICatalogBuilderListener listener, File storage, URI uri)
	{
		super(listener, storage);
		mURI = uri;
		mTempFile = new File(Constants.TEMP_CATALOG_PATH, "catalog.xml");
		mDeprecatedTimeout = 15*60*1000;
	}

	public boolean isDerpecated(){
		if(mCatalog == null) return true;
		return System.currentTimeMillis() > (mCatalog.getLoadingTimestamp() + mDeprecatedTimeout);
	}
	
	public void refresh() {
		WebUtil.downloadFile(null, mURI, mTempFile, this);
	}
	
	public void onBegin(Object context) {
		FileUtil.delete(mTempFile);
	}

	public void onCanceled(Object context) {
	}

	public void onDone(Object context, File file) {
		try {
			mCatalog = CatalogDeserializer.deserializeCatalog(new BufferedInputStream(new FileInputStream(mTempFile)));
		} catch (Exception e) {
			onFailed(context, e);
		}
	}

	public void onFailed(Object context, Throwable reason) {
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
			Log.e(Constants.LOG_TAG_MAIN, "Failed download catalog", reason);
		}
		FileUtil.delete(mTempFile);
		if(mCatalog == null){
			mCatalog = getCorruptedCatalog();
		}
	}
	
	public boolean onUpdate(Object context, long position, long total) {
		fireProgressChanged((int)position, (int)total, "");
		return true;
	}

	protected String getBaseUrl() {
		return  mURI.toString();
	}
	
}
