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
package org.ametro.catalog.storage;

import org.ametro.catalog.Catalog;

public interface ICatalogStorageListener {
	
	void onCatalogLoaded(int catalogId, Catalog catalog);
	void onCatalogFailed(int catalogId, String message);
	void onCatalogProgress(int catalogId, int progress, int total, String message);

	void onCatalogMapChanged(String systemName);
	void onCatalogMapDownloadFailed(String systemName, Throwable ex);
	void onCatalogMapImportFailed(String systemName, Throwable e);
	void onCatalogMapDownloadDone(String systemName);
	void onCatalogMapImportDone(String systemName);
	
	void onCatalogMapDownloadProgress(String systemName, int progress, int total);
	void onCatalogMapImportProgress(String systemName, int progress, int total);
	
}
