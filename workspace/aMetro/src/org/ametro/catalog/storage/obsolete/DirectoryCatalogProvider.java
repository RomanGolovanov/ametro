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
import java.util.ArrayList;

import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;

public class DirectoryCatalogProvider extends BaseCatalogProvider  {

	protected final File mPath;

	public DirectoryCatalogProvider(ICatalogProviderListener listener, File storage, File path)
	{
		super(listener, storage);
		mPath = path;
	}

	public boolean isDerpecated(){
		if(mCatalog == null) return true;
		return new File(mCatalog.getBaseUrl()).lastModified() > mCatalog.getTimestamp();
	}
		
	public void refresh() {
		mCatalog = scan();
	}	
	
	protected Catalog scan(){
		try{
			int fileTypes = FILE_TYPE_AMETRO | FILE_TYPE_PMETRO;
			Catalog catalog = new Catalog(mPath.lastModified(), mPath.getAbsolutePath().toLowerCase(), null);
			ArrayList<CatalogMap> maps = new ArrayList<CatalogMap>();
			if(mPath.exists() && mPath.isDirectory() ){
				final File[] files =  mPath.listFiles();
				final int total = files.length;
				int progress = 0;
				for(File file: files){
					progress++;
					try{
						final String fileName = file.getName().toLowerCase();
						fireProgressChanged(progress, total, fileName);
						if( ((fileTypes & FILE_TYPE_PMETRO)!=0 && fileName.endsWith(PMETRO_EXTENSION))||
							((fileTypes & FILE_TYPE_AMETRO)!=0 && fileName.endsWith(AMETRO_EXTENSION))){
		
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
				}
			}
			catalog.setMaps(maps);
			return catalog;
		}catch(Exception ex){
			fireOperationFailed("Failed scan catalog due error: " + ex.getMessage());
			return null;
		}
	}
	

}
