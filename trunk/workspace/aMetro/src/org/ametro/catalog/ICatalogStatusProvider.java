package org.ametro.catalog;

public interface ICatalogStatusProvider{

	public static final int OFFLINE = 0; 
	public static final int NOT_SUPPORTED = 1;
	public static final int CORRUPTED = 2;
	public static final int UPDATE = 3; 
	public static final int INSTALLED = 4; 
	public static final int IMPORT = 5; 
	public static final int DOWNLOAD = 6; 
	public static final int UPDATE_NOT_SUPPORTED = 7; 
	
	int getCatalogStatus(CatalogMap local, CatalogMap remote);

}
