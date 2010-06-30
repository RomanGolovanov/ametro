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

import static org.ametro.catalog.CatalogMapState.UPDATE;

import org.ametro.R;
import org.ametro.adapter.CatalogExpandableAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.storage.CatalogStorage;

public class CatalogLocalListActivity extends BaseCatalogExpandableActivity {

	private Catalog mLocal;
	private Catalog mOnline;
	private boolean mOnlineNotAvailable;

	private boolean mFavoritesOnly;

	protected void onPrepareView() {
		Catalog localPrevious = mLocal;
		Catalog onlinePrevious = mOnline;
		mLocal = mStorage.getLocalCatalog();
		mOnline = mStorage.getOnlineCatalog();
		if (mLocal == null || !Catalog.equals(mLocal,localPrevious)) {
			mStorage.requestLocalCatalog(false);
		}
		if (mOnline == null || !Catalog.equals(mOnline,onlinePrevious)) {
			mStorage.requestOnlineCatalog(false);
		}
		onCatalogsUpdate(!Catalog.equals(mLocal,localPrevious) || !Catalog.equals(mOnline,onlinePrevious));
		super.onPrepareView();
	}

	private void onCatalogsUpdate(boolean refresh) {
		if (mLocal != null && (mOnline != null || mOnlineNotAvailable)) {
			if (mLocal.getMaps().size() > 0) {
				if (mMode != MODE_LIST) {
					setListView();
				} else {
					if (refresh) {
						setListView();
					}else{
						updateList(mLocal, mOnline);
					}
				}
			} else {
				setEmptyView();
			}
		}
	}

	protected CatalogExpandableAdapter getListAdapter() {
		return new CatalogExpandableAdapter(this, mLocal, mOnline,
				CatalogMapPair.DIFF_MODE_LOCAL, R.array.local_catalog_map_state_colors,
				this);
	}

	protected void onLocalCatalogLoaded(Catalog catalog) {
		mLocal = catalog;
		onCatalogsUpdate(true);
		super.onLocalCatalogLoaded(catalog);
	}

	protected void onOnlineCatalogLoaded(Catalog catalog) {
		mOnline = catalog;
		mOnlineNotAvailable = catalog == null || catalog.isCorrupted();
		onCatalogsUpdate(true);
		super.onOnlineCatalogLoaded(catalog);
	}

	protected void onCatalogRefresh() {
		mStorage.requestLocalCatalog(true);
		super.onCatalogRefresh();
	}

	protected boolean isCatalogProgressEnabled(int catalogId) {
		return catalogId == CatalogStorage.CATALOG_LOCAL;
	}

	protected int getEmptyListMessage() {
		return mFavoritesOnly ? R.string.msg_no_maps_in_favorites
				: R.string.msg_no_maps_in_local;
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mStorage.getLocalCatalogState(local, remote);
	}

	public boolean onCatalogMapClick(CatalogMap local, CatalogMap remote,
			int state) {
		switch (state) {
		case UPDATE:
			invokeFinish(local);
			return true;
		}
		return super.onCatalogMapClick(local, remote);
	}

}
