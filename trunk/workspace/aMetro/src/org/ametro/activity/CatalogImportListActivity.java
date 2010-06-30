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
import org.ametro.activity.obsolete.ImportPmz;
import org.ametro.adapter.CatalogExpandableAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.storage.CatalogStorage;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class CatalogImportListActivity extends BaseCatalogExpandableActivity {

	private Catalog mLocal;
	private Catalog mImport;

	private final int MAIN_MENU_IMPORT = 1;
	private final static int REQUEST_IMPORT = 1;

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_IMPORT, 4, R.string.menu_import).setIcon(
				android.R.drawable.ic_menu_add);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_IMPORT).setEnabled(mMode == MODE_LIST);
		return super.onPrepareOptionsMenu(menu);
	}

	protected void onCatalogRefresh() {
		mStorage.requestImportCatalog(true);
		super.onCatalogRefresh();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_IMPORT:
			startActivityForResult(new Intent(this, ImportPmz.class),
					REQUEST_IMPORT);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_IMPORT:
			setWaitView();
			mStorage.requestImportCatalog(true);
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void onPrepareView() {
		Catalog localPrevious = mLocal;
		Catalog importPrevious = mImport;
		mLocal = mStorage.getLocalCatalog();
		mImport = mStorage.getImportCatalog();
		if (mLocal == null || !Catalog.equals(mLocal,localPrevious)) {
			mStorage.requestLocalCatalog(false);
		}
		if (mImport == null || !Catalog.equals(mImport,importPrevious)) {
			mStorage.requestImportCatalog(false);
		}
		onCatalogsUpdate(!Catalog.equals(mLocal,localPrevious) || !Catalog.equals(mImport,importPrevious));
		super.onPrepareView();
	}

	private void onCatalogsUpdate(boolean refresh) {
		if (mLocal != null && mImport != null) {
			if (mImport.getMaps().size() > 0) {
				if (mMode != MODE_LIST) {
					setListView();
				} else {
					if (refresh) {
						setListView();
					}
				}
			} else {
				setEmptyView();
			}
		}
	}

	protected int getEmptyListMessage() {
		return R.string.msg_no_maps_in_import;
	}

	protected CatalogExpandableAdapter getListAdapter() {
		return new CatalogExpandableAdapter(this, mLocal, mImport,
				CatalogMapPair.DIFF_MODE_REMOTE,
				R.array.import_catalog_map_state_colors, this);
	}

	protected void onLocalCatalogLoaded(Catalog catalog) {
		mLocal = catalog;
		onCatalogsUpdate(true);
		super.onLocalCatalogLoaded(catalog);
	}

	protected void onImportCatalogLoaded(Catalog catalog) {
		mImport = catalog;
		onCatalogsUpdate(true);
		super.onImportCatalogLoaded(catalog);
	}

	protected boolean isCatalogProgressEnabled(int catalogId) {
		return catalogId == CatalogStorage.CATALOG_IMPORT;
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mStorage.getImportCatalogState(local, remote);
	}

}
