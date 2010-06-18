package org.ametro.catalog.storage;

import org.ametro.catalog.Catalog;

public interface ICatalogStorageListener {
	
	void onCatalogLoaded(int catalogId, Catalog catalog);
	
	void onCatalogOperationFailed(int catalogId, String message);
	
	void onCatalogOperationProgress(int catalogId, int progress, int total, String message);
	
}
