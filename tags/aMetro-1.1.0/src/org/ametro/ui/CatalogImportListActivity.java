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
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.ui.adapters.CheckedCatalogAdapter;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CatalogImportListActivity extends BaseCatalogExpandableActivity {

	protected int getEmptyListHeader() {
		return R.string.msg_no_maps_in_import_header;
	}

	protected int getEmptyListMessage() {
		return R.string.msg_no_maps_in_import;
	}

	protected boolean isCatalogProgressEnabled(int catalogId) {
		return catalogId == CatalogStorage.IMPORT; 
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mStorageState.getImportCatalogState(local, remote);
	}

	protected int getDiffMode() {
		return CatalogMapPair.DIFF_MODE_REMOTE;
	}

	protected int getLocalCatalogId() {
		return CatalogStorage.LOCAL;
	}

	protected int getRemoteCatalogId() {
		return CatalogStorage.IMPORT;
	}

	protected int getDiffColors() {
		return R.array.import_catalog_map_state_colors;
	}

	
	/****************** MAIN MENU ********************/
	
	private final int MAIN_MENU_IMPORT = 1;
	private final static int REQUEST_IMPORT = 1;

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_IMPORT, 4, R.string.menu_import_maps).setIcon(R.drawable.icon_tab_import_selected);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_IMPORT).setEnabled(mMode == MODE_LIST  && !mStorage.hasTasks());
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_IMPORT:
			Intent i = new Intent(this, CatalogMapSelectionActivity.class);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_TITLE, getText(R.string.menu_import_maps));
			i.putExtra(CatalogMapSelectionActivity.EXTRA_REMOTE_ID, CatalogStorage.IMPORT);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_FILTER, mActionBarEditText.getText().toString());
			i.putExtra(CatalogMapSelectionActivity.EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
			i.putExtra(CatalogMapSelectionActivity.EXTRA_CHECKABLE_STATES, new int[]{ CatalogMapState.IMPORT, CatalogMapState.UPDATE, CatalogMapState.NEED_TO_UPDATE } );
			i.putExtra(CatalogMapSelectionActivity.EXTRA_VISIBLE_STATES, new int[]{ CatalogMapState.IMPORT, CatalogMapState.UPDATE, CatalogMapState.NEED_TO_UPDATE } );
			startActivityForResult(i, REQUEST_IMPORT);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_IMPORT:
			if(resultCode == RESULT_OK && data!=null){
				String[] names = data.getStringArrayExtra(CatalogMapSelectionActivity.EXTRA_SELECTION);
				for (String systemName : names) {
					mStorage.requestImport(systemName);
				}
			}
			if(resultCode == CatalogMapSelectionActivity.RESULT_MAP_LIST_EMPTY){
				Toast.makeText(this, R.string.msg_no_maps_to_import, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
