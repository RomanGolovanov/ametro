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
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.ui.adapters.CheckedCatalogAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import static org.ametro.catalog.CatalogMapState.CALCULATING;;

public class CatalogLocalListActivity extends BaseCatalogActivity {

	public static final String EXTRA_INVOKE_LOCAL_UPDATE_LIST = "EXTRA_INVOKE_LOCAL_UPDATE_LIST";
	
	private boolean mInvokeSelectCurrent;
	private boolean mInvokeUpdateList;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInvokeSelectCurrent = true;
		Intent data = getIntent();
		if(data!=null){
			mInvokeUpdateList = data.getBooleanExtra(EXTRA_INVOKE_LOCAL_UPDATE_LIST, false);
		}
	}
	
	protected void setListView() {
		super.setListView();
		if(mInvokeSelectCurrent){
			mInvokeSelectCurrent = false;
			GlobalSettings.MapPath path = GlobalSettings.getCurrentMap(this);
			String systemMapName = path.getSystemMapName();
			if(systemMapName!=null){
				int position = mAdapter.findItemPosition(systemMapName);
				if(position!=-1){
					mList.setSelection(position);
				}
			}
		}
		if(mInvokeUpdateList){
			mInvokeUpdateList = false;
			invokeSelectUpdateMaps();
		}		
	}
	
	protected boolean isCatalogProgressEnabled(int catalogId) {
		return catalogId == CatalogStorage.LOCAL;
	}
	
	protected int getEmptyListHeader() {
		return R.string.msg_no_maps_in_local_header;
	}

	protected int getEmptyListMessage() {
		return R.string.msg_no_maps_in_local;
	}
	
	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mRemote!=null && mLocal!=null ? mStorageState.getLocalCatalogState(local, remote) : CatalogMapState.CALCULATING;
	}

	public boolean onCatalogMapClick(CatalogMap local, CatalogMap remote, int state) {
		switch(state){
		case CALCULATING:
			if(local.isAvailable()){
				invokeFinish(local);
			}else{
				invokeMapDetails(local,remote,state);
			}
			return true;
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
	private final int MAIN_MENU_DELETE_MAPS = 2;
	
	private final static int REQUEST_UPDATE = 1;
	private final static int REQUEST_DELETE = 2;

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_UPDATE_MAPS, 5, R.string.menu_update_maps).setIcon(R.drawable.icon_tab_import_selected);
		menu.add(0, MAIN_MENU_DELETE_MAPS, 6, R.string.menu_delete_maps).setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_UPDATE_MAPS).setEnabled(mMode == MODE_LIST && !mStorage.hasTasks());
		menu.findItem(MAIN_MENU_DELETE_MAPS).setEnabled(mMode == MODE_LIST && !mStorage.hasTasks());
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_UPDATE_MAPS:
			invokeSelectUpdateMaps();
			return true;
		case MAIN_MENU_DELETE_MAPS:
			invokeSelectDeleteMaps();
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
			if(resultCode == CatalogMapSelectionActivity.RESULT_MAP_LIST_EMPTY){
				Toast.makeText(this, R.string.msg_no_maps_to_update, Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_DELETE:
			if(resultCode == RESULT_OK && data!=null){
				String[] names = data.getStringArrayExtra(CatalogMapSelectionActivity.EXTRA_SELECTION);
				for (String systemName : names) {
					mStorage.deleteLocalMap(systemName);
				}
			}
			if(resultCode == CatalogMapSelectionActivity.RESULT_MAP_LIST_EMPTY){
				Toast.makeText(this, R.string.msg_no_maps_to_delete, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void invokeSelectDeleteMaps() {
		Intent i = new Intent(this, CatalogMapSelectionActivity.class);
		i.putExtra(CatalogMapSelectionActivity.EXTRA_TITLE, getText(R.string.menu_delete_maps));
		i.putExtra(CatalogMapSelectionActivity.EXTRA_REMOTE_ID, CatalogStorage.ONLINE);
		i.putExtra(CatalogMapSelectionActivity.EXTRA_REMOTE_MODE, CatalogMapPair.DIFF_MODE_LOCAL);
		i.putExtra(CatalogMapSelectionActivity.EXTRA_FILTER, mActionBarEditText.getText().toString());
		i.putExtra(CatalogMapSelectionActivity.EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
		startActivityForResult(i, REQUEST_DELETE);
	}

	private void invokeSelectUpdateMaps() {
		Intent i = new Intent(this, CatalogMapSelectionActivity.class);
		i.putExtra(CatalogMapSelectionActivity.EXTRA_TITLE, getText(R.string.menu_update_maps));
		i.putExtra(CatalogMapSelectionActivity.EXTRA_REMOTE_ID, CatalogStorage.ONLINE);
		i.putExtra(CatalogMapSelectionActivity.EXTRA_FILTER, mActionBarEditText.getText().toString());
		i.putExtra(CatalogMapSelectionActivity.EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
		i.putExtra(CatalogMapSelectionActivity.EXTRA_CHECKABLE_STATES, new int[]{ CatalogMapState.UPDATE, CatalogMapState.NEED_TO_UPDATE } );
		i.putExtra(CatalogMapSelectionActivity.EXTRA_VISIBLE_STATES, new int[]{ CatalogMapState.UPDATE, CatalogMapState.NEED_TO_UPDATE } );
		startActivityForResult(i, REQUEST_UPDATE);
	}	
}
