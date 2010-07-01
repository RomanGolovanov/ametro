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

public class CatalogLocalListActivity extends BaseCatalogExpandableActivity {

	protected void onCatalogRefresh() {
		mStorage.requestCatalog(CatalogStorage.LOCAL, true);
		super.onCatalogRefresh();
	}

	protected boolean isCatalogProgressEnabled(int catalogId) {
		return catalogId == CatalogStorage.LOCAL;
	}

	protected int getEmptyListMessage() {
		return R.string.msg_no_maps_in_local;
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mStorage.getLocalCatalogState(local, remote);
	}

	public boolean onCatalogMapClick(CatalogMap local, CatalogMap remote) {
		invokeFinish(local);
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
