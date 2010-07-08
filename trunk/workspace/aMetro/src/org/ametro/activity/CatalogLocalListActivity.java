/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.ametro.activity;

import org.ametro.R;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.storage.CatalogStorage;

import static org.ametro.catalog.CatalogMapState.CORRUPTED;
import static org.ametro.catalog.CatalogMapState.DOWNLOAD;
import static org.ametro.catalog.CatalogMapState.DOWNLOADING;
import static org.ametro.catalog.CatalogMapState.DOWNLOAD_PENDING;
import static org.ametro.catalog.CatalogMapState.IMPORT;
import static org.ametro.catalog.CatalogMapState.IMPORTING;
import static org.ametro.catalog.CatalogMapState.IMPORT_PENDING;
import static org.ametro.catalog.CatalogMapState.INSTALLED;
import static org.ametro.catalog.CatalogMapState.NEED_TO_UPDATE;
import static org.ametro.catalog.CatalogMapState.NOT_SUPPORTED;
import static org.ametro.catalog.CatalogMapState.OFFLINE;
import static org.ametro.catalog.CatalogMapState.UPDATE;
import static org.ametro.catalog.CatalogMapState.UPDATE_NOT_SUPPORTED;

public class CatalogLocalListActivity extends BaseCatalogActivity {

	protected boolean isCatalogProgressEnabled(int catalogId) {
		return catalogId == CatalogStorage.LOCAL;
	}

	protected int getEmptyListMessage() {
		return R.string.msg_no_maps_in_local;
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mStorage.getLocalCatalogState(local, remote);
	}

	public boolean onCatalogMapClick(CatalogMap local, CatalogMap remote, int state) {
		switch(state){
		case OFFLINE:
		case INSTALLED:
		case UPDATE:
			invokeFinish(local);
			return true;
		case IMPORT:
		case DOWNLOAD:
		case DOWNLOAD_PENDING:
		case DOWNLOADING:
		case IMPORT_PENDING:
		case IMPORTING:
		case NEED_TO_UPDATE:
		case NOT_SUPPORTED:
		case UPDATE_NOT_SUPPORTED:
		case CORRUPTED:
			invokeMapDetails(local,remote,state);
			return true;
			// do nothing
		}				
		return true;
	}

	protected int getDiffMode() {
		return CatalogMapPair.DIFF_MODE_LOCAL;
	}

	protected int getLocalCatalogId() {
		return CatalogStorage.LOCAL;
	}

	protected int getRemoteCatalogId() {
		return CatalogStorage.ONLINE;
	}

	protected int getDiffColors() {
		return R.array.local_catalog_map_state_colors;
	}
	
	
}
