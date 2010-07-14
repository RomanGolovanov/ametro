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
import org.ametro.adapter.CheckedCatalogAdapter;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

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
		return mStorageState.getLocalCatalogState(local, remote);
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
	
	/****************** MAIN MENU ********************/
	
	private final int MAIN_MENU_UPDATE_MAPS = 1;
	private final static int REQUEST_UPDATE = 1;

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_UPDATE_MAPS, 4, R.string.menu_update_maps).setIcon(android.R.drawable.ic_menu_add);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_UPDATE_MAPS).setEnabled(mMode == MODE_LIST && !mStorage.hasTasks());
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_UPDATE_MAPS:
			Intent i = new Intent(this, CatalogMapSelectionActivity.class);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_TITLE, getText(R.string.menu_update_maps));
			i.putExtra(CatalogMapSelectionActivity.EXTRA_REMOTE_ID, CatalogStorage.ONLINE);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_FILTER, mActionBarEditText.getText().toString());
			i.putExtra(CatalogMapSelectionActivity.EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_CHECKABLE_STATES, new int[]{ CatalogMapState.IMPORT, CatalogMapState.UPDATE } );
			i.putExtra(CatalogMapSelectionActivity.EXTRA_VISIBLE_STATES, new int[]{ CatalogMapState.IMPORT, CatalogMapState.UPDATE } );
			startActivityForResult(i, REQUEST_UPDATE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_UPDATE:
			if(resultCode == RESULT_OK && data!=null){
				String[] names = data.getStringArrayExtra(CatalogMapSelectionActivity.EXTRA_SELECTION);
				for (String systemName : names) {
					mStorage.requestDownload(systemName);
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
		
}
