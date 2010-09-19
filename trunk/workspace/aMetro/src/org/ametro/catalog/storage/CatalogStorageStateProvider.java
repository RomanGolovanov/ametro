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

import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapState;

public class CatalogStorageStateProvider {

	private CatalogStorage mStorage;
	
	public CatalogStorageStateProvider(CatalogStorage storage){
		mStorage = storage;
	}

	public int getOnlineCatalogState(CatalogMap local, CatalogMap remote) {
		String systemName = local!=null ? local.getSystemName() : remote.getSystemName();
		
		if(mStorage.isDownloadingTask(systemName)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mStorage.findQueuedDownloadTask(systemName)!=null){
			return CatalogMapState.DOWNLOAD_PENDING;
		}
		
		if(mStorage.isImportingTask(systemName)){
			return CatalogMapState.IMPORTING;
		}
		if(mStorage.findQueuedImportTask(systemName)!=null){
			return CatalogMapState.IMPORT_PENDING;
		}
		
		if (remote.isNotSupported()) {
			if (local == null || local.isNotSupported() || local.isCorrupted()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.UPDATE_NOT_SUPPORTED;
			}
		} else {
			if (local == null) {
				return CatalogMapState.DOWNLOAD;
			} else if (local.isNotSupported() || local.isCorrupted()) {
				return CatalogMapState.NEED_TO_UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return CatalogMapState.INSTALLED;
				} else {
					return CatalogMapState.UPDATE;
				}
			}
		}
	}

	public int getImportCatalogState(CatalogMap local, CatalogMap remote) {
		String systemName = local!=null ? local.getSystemName() : remote.getSystemName();

		if(mStorage.isImportingTask(systemName)){
			return CatalogMapState.IMPORTING;
		}
		if(mStorage.findQueuedImportTask(systemName)!=null){
			return CatalogMapState.IMPORT_PENDING;
		}		
		
		if(mStorage.isDownloadingTask(systemName)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mStorage.findQueuedDownloadTask(systemName)!=null){
			return CatalogMapState.DOWNLOAD_PENDING;
		}
		
		if (remote.isCorrupted()) {
			return CatalogMapState.CORRUPTED;
		} else {
			if (local == null) {
				return CatalogMapState.IMPORT;
			} else if (local.isNotSupported() || local.isCorrupted()) {
				return CatalogMapState.IMPORT_NEED_TO_UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return CatalogMapState.INSTALLED;
				} else {
					return CatalogMapState.IMPORT_UPDATE;
				}
			}
		}
	}

	public int getLocalCatalogState(CatalogMap local, CatalogMap remote) {
		String systemName = local!=null ? local.getSystemName() : remote.getSystemName();

		if(mStorage.isDownloadingTask(systemName)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mStorage.findQueuedDownloadTask(systemName)!=null){
			return CatalogMapState.DOWNLOAD_PENDING;
		}
		
		if(mStorage.isImportingTask(systemName)){
			return CatalogMapState.IMPORTING;
		}
		if(mStorage.findQueuedImportTask(systemName)!=null){
			return CatalogMapState.IMPORT_PENDING;
		}
		
		if (remote == null) {
			// remote not exist
			if (local.isCorrupted()) {
				return CatalogMapState.CORRUPTED;
			} else if (!local.isSupported()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.OFFLINE;
			}
		} else if (!remote.isSupported()) {
			// remote not supported
			if (local.isCorrupted()) {
				return CatalogMapState.CORRUPTED;
			} else if (!local.isSupported()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.INSTALLED;
			}
		} else {
			// remote OK
			if (local.isCorrupted()) {
				return CatalogMapState.NEED_TO_UPDATE;
			} else if (!local.isSupported()) {
				return CatalogMapState.NEED_TO_UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return CatalogMapState.INSTALLED;
				} else {
					return CatalogMapState.UPDATE;
				}
			}
		}
	}	

		
}
