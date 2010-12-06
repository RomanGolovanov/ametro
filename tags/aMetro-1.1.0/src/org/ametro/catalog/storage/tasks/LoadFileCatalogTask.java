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

import java.io.File;
import java.util.ArrayList;

import org.ametro.app.Constants;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;

import android.os.Parcel;
import android.os.Parcelable;

public class LoadFileCatalogTask extends LoadBaseCatalogTask {

	protected final File mPath;

	public boolean isDerpecated() {
		if(mCatalog == null) return true;
		File catalogFile = new File(mCatalog.getBaseUrl());
		if(catalogFile.lastModified() > mCatalog.getTimestamp()) return true;
		if(mCatalogId == CatalogStorage.IMPORT){
			File[] files = catalogFile.listFiles();
			if(files == null && mCatalog.getSize()>0) return true;
			int mapFileCount = 0;
			if(files!=null){
				for(File f : files){
					String fileName = f.getName();
					if(fileName.endsWith(Constants.PMETRO_EXTENSION)){
						CatalogMap map = mCatalog.getMap(fileName.toLowerCase() + Constants.AMETRO_EXTENSION);
						if(map==null) return true;
						if(map.getFileTimestamp() != f.lastModified()) return true;
						mapFileCount++;
					}
				}
			}
			if(mapFileCount != mCatalog.getSize()) return true;
		}
		return false;
	}
	

	public void refresh() throws Exception {
		Catalog catalog = new Catalog(mPath.lastModified(), mPath.getAbsolutePath().toLowerCase(), null);
		ArrayList<CatalogMap> maps = new ArrayList<CatalogMap>();
		if(mPath.exists() && mPath.isDirectory() ){
			final File[] files =  mPath.listFiles();
			final int total = files.length;
			int progress = 0;
			for(File file: files){
				cancelCheck();
				final String fileName = file.getName().toLowerCase();
				update(progress, total, fileName);
				try{
					if( fileName.endsWith(Constants.PMETRO_EXTENSION)|| fileName.endsWith(Constants.AMETRO_EXTENSION)){
						Model model = ModelBuilder.loadModelDescription(file.getAbsolutePath());
						if(model!=null){
					    	maps.add(Catalog.extractCatalogMap(catalog,file, fileName, model));
						}else{
							maps.add(Catalog.makeBadCatalogMap(catalog,file, fileName));
						}
					}
				}catch(Exception ex){
					// skip file due error
				}
				progress++;
			}
		}
		catalog.setMaps(maps);
		mCatalog = catalog;
	}

	public LoadFileCatalogTask(int catalogId, File file, File path, boolean forceRefresh) {
		super(catalogId, file, forceRefresh);
		mPath = path;
	}

	protected LoadFileCatalogTask(Parcel in) {
		super(in);
		mPath = new File(in.readString());
	}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(mPath.getAbsolutePath());
	}
	
	public static final Parcelable.Creator<LoadFileCatalogTask> CREATOR = new Parcelable.Creator<LoadFileCatalogTask>() {
		public LoadFileCatalogTask createFromParcel(Parcel in) {
			return new LoadFileCatalogTask(in);
		}

		public LoadFileCatalogTask[] newArray(int size) {
			return new LoadFileCatalogTask[size];
		}
	};
	
}
