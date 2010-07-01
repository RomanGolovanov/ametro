package org.ametro.catalog.storage;

import java.io.File;
import java.util.ArrayList;

import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;

public class DirectoryCatalogProvider extends BaseCatalogProvider  {

	protected final File mPath;

	public DirectoryCatalogProvider(ICatalogBuilderListener listener, File storage, File path)
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
