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

import java.util.Locale;

import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.adapter.ImportCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapDifference;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class ImportMaps extends BaseExpandableMaps {

	private Catalog mLocal;
	private Catalog mImport;

	private final int MAIN_MENU_IMPORT = 1;
	private final static int REQUEST_IMPORT = 1;
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_IMPORT, 4, R.string.menu_import).setIcon(android.R.drawable.ic_menu_add);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_IMPORT).setEnabled(mMode == MODE_LIST);
		return super.onPrepareOptionsMenu(menu);
	}

	protected void onCatalogRefresh() {
		setWaitView();
		mStorage.requestImportCatalog(true);
		super.onCatalogRefresh();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_IMPORT:
			startActivityForResult(new Intent(this, ImportPmz.class), REQUEST_IMPORT);
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
		mLocal = mStorage.getLocalCatalog();
		mImport = mStorage.getImportCatalog();
		if(mLocal == null){
			mStorage.requestLocalCatalog(false);
		}
		if(mImport == null){
			mStorage.requestImportCatalog(false);
		}
		onCatalogsUpdate();	
	}
		
	private void onCatalogsUpdate() {
		if(mLocal!=null && mImport!=null && mMode != MODE_LIST){
			if(mImport.getMaps().size()>0){
				setListView();
			}else{
				setEmptyView();
			}
		}
	}
	
	protected ExpandableListAdapter getListAdapter() {
		return new ImportCatalogAdapter(this, mImport, mLocal, Locale.getDefault().getLanguage() );
	}	
	
	protected void onLocalCatalogLoaded(Catalog catalog) {
		mLocal = catalog;
		onCatalogsUpdate();
		super.onLocalCatalogLoaded(catalog);
	}
	
	protected void onImportCatalogLoaded(Catalog catalog) {
		mImport = catalog;
		onCatalogsUpdate();
		super.onImportCatalogLoaded(catalog);
	}
	
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		CatalogMapDifference diff = (CatalogMapDifference)mAdapter.getChild(groupPosition, childPosition);
		if(diff.isLocalAvailable()){
			String fileName = diff.getLocalUrl();
			Intent i = new Intent();
			i.setData(MapUri.create( mLocal.getBaseUrl() + "/" + fileName));
			AllMaps.Instance.setResult(RESULT_OK, i);
			AllMaps.Instance.finish();
		}else{
			Intent i = new Intent(this, BrowseMapDetails.class);
			i.putExtra(BrowseMapDetails.IMPORT_MAP_URL, diff.getRemoteUrl());
			startActivity(i);
		}
		return true;		
	}	
}
