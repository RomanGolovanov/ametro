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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ametro.Constants;
import org.ametro.util.FileUtil;
import org.ametro.util.IOperationListener;
import org.ametro.util.WebUtil;

import android.util.Log;

public class WebCatalogProvider extends BaseCatalogProvider implements IOperationListener {

	protected final URI mURI;
	protected final long mDeprecatedTimeout;
	protected final boolean mCompressed;
	
	public WebCatalogProvider(ICatalogBuilderListener listener, File storage, URI uri, boolean compressed)
	{
		super(listener, storage);
		mURI = uri;
		mDeprecatedTimeout = 15*60*1000;
		mCompressed = compressed;
	}

	public boolean isDerpecated(){
		if(mCatalog == null) return true;
		return System.currentTimeMillis() > (mCatalog.getLoadingTimestamp() + mDeprecatedTimeout);
	}
	
	public void refresh() {
		WebUtil.downloadFile(null, mURI, new File(Constants.TEMP_CATALOG_PATH, "catalog.zip"), this);
	}
	
	public void onBegin(Object context, File file) {
		FileUtil.delete(file);
	}

	public void onCanceled(Object context, File file) {
	}

	public void onDone(Object context, File file) throws Exception {
		File catalogFile;
		if(mCompressed){
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
		}else{
			catalogFile = file;
		}
		mCatalog = CatalogDeserializer.deserializeCatalog(new BufferedInputStream(new FileInputStream(catalogFile)));
	}

	public void onFailed(Object context, File file, Throwable reason) {
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
			Log.e(Constants.LOG_TAG_MAIN, "Failed download catalog", reason);
		}
		FileUtil.delete(file);
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
