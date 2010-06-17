package org.ametro.catalog.storage;

import org.ametro.catalog.Catalog;

public interface ICatalogStorageListener {
	
	void onCatalogLoaded(int catalogId, Catalog catalog);
	
}
