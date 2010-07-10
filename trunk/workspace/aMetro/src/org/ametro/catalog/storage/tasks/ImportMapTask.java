package org.ametro.catalog.storage.tasks;

import java.io.File;

import org.ametro.ApplicationEx;
import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.FileUtil;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class ImportMapTask extends UpdateMapTask {

	public ImportMapTask(String systemName) {
		super(systemName);
	}

	protected void run(Context context) throws Exception {
		final CatalogStorage storage = ApplicationEx.getInstance().getCatalogStorage();
		final Catalog importCatalog = storage.getCatalog(CatalogStorage.IMPORT);
		if(importCatalog==null){
			throw new CanceledException("No import catalog available");
		}
		final Catalog localCatalog = storage.getCatalog(CatalogStorage.LOCAL);
		if(localCatalog==null){
			throw new CanceledException("No local catalog available");
		}
		final CatalogMap map = importCatalog.getMap(mSystemName);
		if(map==null){
			throw new CanceledException("No maps found in import catalog with system name " + mSystemName);
		}
		final String absoluteFilePath = map.getAbsoluteUrl();
		final File importFile = new File(GlobalSettings.getTemporaryImportMapFile(mSystemName));
		final File localFile = new File(GlobalSettings.getLocalCatalogMapFileName(mSystemName));
		
		update(0,100,mSystemName);
		Model model = ModelBuilder.loadModel(absoluteFilePath);
		update(50,100,mSystemName);
		FileUtil.delete(importFile);
		ModelBuilder.saveModel(importFile.getAbsolutePath(), model);
		FileUtil.delete(localFile);
		FileUtil.move(importFile, localFile);
		CatalogMap localMap = Catalog.extractCatalogMap(localCatalog, localFile, localFile.getName().toLowerCase(), model);
		localCatalog.appendMap(localMap);
		Catalog.save(localCatalog, Constants.LOCAL_CATALOG_STORAGE);
		update(100,100,mSystemName);
	}
	
	public ImportMapTask(Parcel in) {
		super(in);
	}

	public static final Parcelable.Creator<ImportMapTask> CREATOR = new Parcelable.Creator<ImportMapTask>() {
		public ImportMapTask createFromParcel(Parcel in) {
			return new ImportMapTask(in);
		}

		public ImportMapTask[] newArray(int size) {
			return new ImportMapTask[size];
		}
	};	
}
