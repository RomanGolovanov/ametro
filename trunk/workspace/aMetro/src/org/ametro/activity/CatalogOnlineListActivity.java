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

public class CatalogOnlineListActivity extends BaseCatalogExpandableActivity {

	protected int getEmptyListMessage() {
		return R.string.msg_no_maps_in_online;
	}

	protected CharSequence formatProgress(int mProgress, int mTotal) {
		return mProgress + "/" + mTotal + " bytes"; 
	}

	protected boolean isCatalogProgressEnabled(int catalogId) {
		return catalogId == CatalogStorage.ONLINE;
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mStorageState.getOnlineCatalogState(local, remote);
	}
	
	protected int getDiffMode() {
		return CatalogMapPair.DIFF_MODE_REMOTE;
	}

	protected int getLocalCatalogId() {
		return CatalogStorage.LOCAL;
	}

	protected int getRemoteCatalogId() {
		return CatalogStorage.ONLINE;
	}

	protected int getDiffColors() {
		return R.array.online_catalog_map_state_colors;
	}

	/****************** MAIN MENU ********************/
	
	private final int MAIN_MENU_DOWNLOAD = 1;
	private final static int REQUEST_DOWNLOAD = 1;

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_DOWNLOAD, 4, R.string.menu_download).setIcon(android.R.drawable.ic_menu_add);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_DOWNLOAD).setEnabled(mMode == MODE_LIST);
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_DOWNLOAD:
			Intent i = new Intent(this, CatalogMapSelectionActivity.class);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_REMOTE_ID, CatalogStorage.ONLINE);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_FILTER, mActionBarEditText.getText().toString());
			i.putExtra(CatalogMapSelectionActivity.EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_CHECKABLE_STATES, new int[]{ CatalogMapState.DOWNLOAD, CatalogMapState.UPDATE } );
			startActivityForResult(i, REQUEST_DOWNLOAD);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_DOWNLOAD:
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
