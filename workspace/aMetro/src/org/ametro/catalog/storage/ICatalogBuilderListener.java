package org.ametro.catalog.storage;

public interface ICatalogBuilderListener {

	void onCatalogBuilderOperationFailed(CatalogBuilder source, String message);
	
	void onCatalogBuilderOperationProgress(CatalogBuilder source, int progress, int total, String message);
	
}
