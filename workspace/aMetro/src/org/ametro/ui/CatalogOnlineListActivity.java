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
package org.ametro.ui;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.ui.adapters.CheckedCatalogAdapter;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CatalogOnlineListActivity extends BaseCatalogExpandableActivity {

	protected int getEmptyListHeader() {
		return R.string.msg_no_maps_in_online_header;
	}

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
	private final static int MAIN_MENU_DOWNLOAD = 1;
	private final static int REQUEST_DOWNLOAD = 1;

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_DOWNLOAD, 4, R.string.menu_download_maps).setIcon(R.drawable.icon_tab_import_selected);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_DOWNLOAD).setEnabled(mMode == MODE_LIST && !mStorage.hasTasks());
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_REFRESH:
			if(!ApplicationEx.getInstance().isNetworkAvailable()){
				Toast.makeText(this, R.string.msg_no_network_available, Toast.LENGTH_SHORT).show();
				return true;
			}
			break;
		case MAIN_MENU_DOWNLOAD:
			Intent i = new Intent(this, CatalogMapSelectionActivity.class);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_TITLE, getText(R.string.menu_download_maps));
			i.putExtra(CatalogMapSelectionActivity.EXTRA_REMOTE_ID, CatalogStorage.ONLINE);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_FILTER, mActionBarEditText.getText().toString());
			i.putExtra(CatalogMapSelectionActivity.EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_CHECKABLE_STATES, new int[]{ CatalogMapState.DOWNLOAD, CatalogMapState.UPDATE, CatalogMapState.NEED_TO_UPDATE } );
			i.putExtra(CatalogMapSelectionActivity.EXTRA_VISIBLE_STATES, new int[]{ CatalogMapState.DOWNLOAD, CatalogMapState.UPDATE, CatalogMapState.NEED_TO_UPDATE } );
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
			if(resultCode == CatalogMapSelectionActivity.RESULT_MAP_LIST_EMPTY){
				Toast.makeText(this, R.string.msg_no_maps_to_download, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
