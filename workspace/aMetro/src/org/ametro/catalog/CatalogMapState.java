package org.ametro.catalog;

public class CatalogMapState {
	public static final int OFFLINE = 0;
	public static final int NOT_SUPPORTED = 1;
	public static final int CORRUPTED = 2;
	public static final int UPDATE = 3;
	public static final int INSTALLED = 4;
	public static final int IMPORT = 5;
	public static final int DOWNLOAD = 6;
	public static final int UPDATE_NOT_SUPPORTED = 7;
	public static final int NEED_TO_UPDATE = 8;
	public static final int DOWNLOAD_PENDING = 9;
	public static final int IMPORT_PENDING = 10;
	public static final int DOWNLOADING = 11;
	public static final int IMPORTING = 12;

	public static int getOnlineCatalogState(CatalogMap local, CatalogMap remote) {
		if (remote.isNotSupported()) {
			if (local == null || local.isNotSupported() || local.isCorruted()) {
				return NOT_SUPPORTED;
			} else {
				return UPDATE_NOT_SUPPORTED;
			}
		} else {
			if (local == null) {
				return DOWNLOAD;
			} else if (local.isNotSupported() || local.isCorruted()) {
				return NEED_TO_UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return INSTALLED;
				} else {
					return UPDATE;
				}
			}
		}
	}

	public static int getImportCatalogState(CatalogMap local, CatalogMap remote) {
		if (remote.isCorruted()) {
			return CORRUPTED;
		} else {
			if (local == null) {
				return IMPORT;
			} else if (!local.isSupported() || local.isCorruted()) {
				return UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return INSTALLED;
				} else {
					return UPDATE;
				}
			}
		}
	}

	public static int getLocalCatalogState(CatalogMap local, CatalogMap remote) {
		if (remote == null) {
			// remote not exist
			if (local.isCorruted()) {
				return CORRUPTED;
			} else if (!local.isSupported()) {
				return NOT_SUPPORTED;
			} else {
				return OFFLINE;
			}
		} else if (!remote.isSupported()) {
			// remote not supported
			if (local.isCorruted()) {
				return CORRUPTED;
			} else if (!local.isSupported()) {
				return NOT_SUPPORTED;
			} else {
				return INSTALLED;
			}
		} else {
			// remote OK
			if (local.isCorruted()) {
				return NEED_TO_UPDATE;
			} else if (!local.isSupported()) {
				return NEED_TO_UPDATE;
			} else {
				if (local.getTimestamp() >= remote.getTimestamp()) {
					return INSTALLED;
				} else {
					return UPDATE;
				}
			}
		}
	}

}
