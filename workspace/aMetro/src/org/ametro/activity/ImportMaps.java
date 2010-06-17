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
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;

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
