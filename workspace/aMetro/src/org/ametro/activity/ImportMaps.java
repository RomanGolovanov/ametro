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

import java.util.ArrayList;
import java.util.Locale;

import org.ametro.R;
import org.ametro.adapter.LocalCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapDifference;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.ICatalogStorageListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class ImportMaps extends Activity implements ICatalogStorageListener {

	private static final int MODE_WAIT = 0;
	private static final int MODE_LIST = 1;
	private static final int MODE_EMPTY = 2;
	
	public static final String EXTRA_FAVORITES_ONLY = "FAVORITES_ONLY";
	
	private CatalogStorage mStorage;
	private Catalog mLocal;
	private Catalog mImport;
	
	private int mMode;
	
	private LocalCatalogAdapter mAdapter;
	private ArrayList<CatalogMapDifference> mCatalogDifferences;
	private ExpandableListView mList ;

	private final int MAIN_MENU_REFRESH = 1;
	private final int MAIN_MENU_LOCATION = 4;
	private final int MAIN_MENU_IMPORT = 5;
	private final int MAIN_MENU_SETTINGS = 6;
	private final int MAIN_MENU_ABOUT = 7;

	private final static int REQUEST_IMPORT = 1;
	private final static int REQUEST_SETTINGS = 2;
	private final static int REQUEST_LOCATION = 3;
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_REFRESH, 0, R.string.menu_refresh).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, MAIN_MENU_LOCATION, 3, R.string.menu_location).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, MAIN_MENU_IMPORT, 4, R.string.menu_import).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MAIN_MENU_SETTINGS, 5, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 6, R.string.menu_about).setIcon(android.R.drawable.ic_menu_help);

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_LOCATION).setVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_REFRESH:
			setWaitView();
			mStorage.requestImportCatalog(true);
			return true;
		case MAIN_MENU_LOCATION:
			startActivityForResult(new Intent(this, SearchLocation.class), REQUEST_LOCATION);
			return true;
		case MAIN_MENU_IMPORT:
			startActivityForResult(new Intent(this, ImportPmz.class), REQUEST_IMPORT);
			return true;
		case MAIN_MENU_SETTINGS:
			startActivityForResult(new Intent(this, Settings.class), REQUEST_SETTINGS);
			return true;
		case MAIN_MENU_ABOUT:
			startActivity(new Intent(this, About.class));
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
		case REQUEST_SETTINGS:
			break;
		case REQUEST_LOCATION:
//			if(resultCode == RESULT_OK){
//				Location location = data.getParcelableExtra(SearchLocation.LOCATION);
//				mLocationSearchTask = new LocationSearchTask();
//				mLocationSearchTask.execute(location);
//			}
			if(resultCode == RESULT_CANCELED){
				Toast.makeText(this,R.string.msg_location_unknown, Toast.LENGTH_SHORT).show();			
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mStorage = AllMaps.Instance.getStorage();
		setWaitView();
	}

	protected void onResume() {
		mLocal = mStorage.getLocalCatalog();
		mImport = mStorage.getImportCatalog();
		mStorage.addCatalogChangedListener(this);
		if(mLocal == null){
			mStorage.requestLocalCatalog(false);
		}
		if(mImport == null){
			mStorage.requestImportCatalog(false);
		}
		invalidateCatalogs();
		super.onResume();
	}
	
	protected void onPause() {
		mStorage.removeCatalogChangedListener(this);
		super.onPause();
	}
		
	private void invalidateCatalogs() {
		if(mLocal!=null && mImport!=null && mMode != MODE_LIST){
			if(mImport.getMaps().size()>0){
				setListView();
			}else{
				setEmptyView();
			}
		}
	}
	
	private void setEmptyView() {
		setContentView(R.layout.maps_list_empty);
		((TextView)findViewById(R.id.maps_message)).setText(R.string.msg_no_maps_in_import);
		mMode = MODE_EMPTY;
	} 

	private void setListView() {
		setContentView(R.layout.browse_catalog_list_item);
		mCatalogDifferences = Catalog.diff(mLocal, mImport, Catalog.MODE_RIGHT_JOIN);
		setContentView(R.layout.browse_catalog_main);
		mList = (ExpandableListView)findViewById(R.id.browse_catalog_list);
		mAdapter = new LocalCatalogAdapter(this, mCatalogDifferences, Locale.getDefault().getLanguage() ); 
		mList.setAdapter(mAdapter);
		mMode = MODE_LIST;
	}
	
	private void setWaitView() {
		setContentView(R.layout.maps_wait);
		mMode = MODE_WAIT;
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		if(catalogId == CatalogStorage.CATALOG_LOCAL){
			mLocal = catalog;
			invalidateCatalogs();
		}
		if(catalogId == CatalogStorage.CATALOG_IMPORT){
			mImport = catalog;
			invalidateCatalogs();
		}
	}
	
}
