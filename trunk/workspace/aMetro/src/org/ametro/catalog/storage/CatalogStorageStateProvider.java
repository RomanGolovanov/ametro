package org.ametro.catalog.storage;

import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.tasks.ImportMapTask;

public class CatalogStorageStateProvider {

	private CatalogStorage mStorage;
	
	public CatalogStorageStateProvider(CatalogStorage storage){
		mStorage = storage;
	}

	public int getOnlineCatalogState(CatalogMap local, CatalogMap remote) {
		if(mStorage.mMapDownloadQueue.isProcessed(remote)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mStorage.mMapDownloadQueue.isPending(remote)){
			return CatalogMapState.DOWNLOAD_PENDING;
		}
		
		String systemName = local!=null ? local.getSystemName() : remote.getSystemName();
		if(mStorage.mSyncRunTask!=null && mStorage.mSyncRunTask instanceof ImportMapTask && systemName.equals(mStorage.mSyncRunTask.getTaskId()) ){
			return CatalogMapState.IMPORTING;
		}
		if(mStorage.findQueuedImportTask(systemName)!=null){
			return CatalogMapState.IMPORT_PENDING;
		}
		
		if (remote.isNotSupported()) {
			if (local == null || local.isNotSupported() || local.isCorruted()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.UPDATE_NOT_SUPPORTED;
			}
		} else {
			if (local == null) {
				return CatalogMapState.DOWNLOAD;
			} else if (local.isNotSupported() || local.isCorruted()) {
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

		if(mStorage.mSyncRunTask!=null && mStorage.mSyncRunTask instanceof ImportMapTask && systemName.equals(mStorage.mSyncRunTask.getTaskId()) ){
			return CatalogMapState.IMPORTING;
		}
		if(mStorage.findQueuedImportTask(systemName)!=null){
			return CatalogMapState.IMPORT_PENDING;
		}		
		
		if(mStorage.mMapDownloadQueue.isProcessed(systemName)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mStorage.mMapDownloadQueue.isPending(systemName)){
			return CatalogMapState.DOWNLOAD_PENDING;
		}
		
		if (remote.isCorruted()) {
			return CatalogMapState.CORRUPTED;
		} else {
			if (local == null) {
				return CatalogMapState.IMPORT;
			} else if (!local.isSupported() || local.isCorruted()) {
				return CatalogMapState.UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return CatalogMapState.INSTALLED;
				} else {
					return CatalogMapState.UPDATE;
				}
			}
		}
	}

	public int getLocalCatalogState(CatalogMap local, CatalogMap remote) {
		if(mStorage.mMapDownloadQueue.isProcessed(remote)){
			return CatalogMapState.DOWNLOADING;
		}
		if(mStorage.mMapDownloadQueue.isPending(remote)){
			return CatalogMapState.DOWNLOAD_PENDING;
		}

		String systemName = local!=null ? local.getSystemName() : remote.getSystemName();
		if(mStorage.mSyncRunTask!=null && mStorage.mSyncRunTask instanceof ImportMapTask && systemName.equals(mStorage.mSyncRunTask.getTaskId()) ){
			return CatalogMapState.IMPORTING;
		}
		if(mStorage.findQueuedImportTask(systemName)!=null){
			return CatalogMapState.IMPORT_PENDING;
		}
		
		if (remote == null) {
			// remote not exist
			if (local.isCorruted()) {
				return CatalogMapState.CORRUPTED;
			} else if (!local.isSupported()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.OFFLINE;
			}
		} else if (!remote.isSupported()) {
			// remote not supported
			if (local.isCorruted()) {
				return CatalogMapState.CORRUPTED;
			} else if (!local.isSupported()) {
				return CatalogMapState.NOT_SUPPORTED;
			} else {
				return CatalogMapState.INSTALLED;
			}
		} else {
			// remote OK
			if (local.isCorruted()) {
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
