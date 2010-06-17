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

import org.ametro.Constants;
import org.ametro.R;
import org.ametro.adapter.LocalCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapDifference;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.ICatalogStorageListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class LocalMaps extends Activity implements ICatalogStorageListener {

	private static final int MODE_WAIT = 0;
	private static final int MODE_LIST = 1;
	private static final int MODE_EMPTY = 2;
	
	public static final String EXTRA_FAVORITES_ONLY = "FAVORITES_ONLY";
	
	private CatalogStorage mStorage;
	private Catalog mLocal;
	private Catalog mOnline;
	private boolean mDownloading;
	
	private int mMode;
	
	private LocalCatalogAdapter mAdapter;
	private ArrayList<CatalogMapDifference> mCatalogDifferences;
	private ExpandableListView mList;
	
	private boolean mFavoritesOnly;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent data = getIntent();
		if(data!=null){
			mFavoritesOnly = data.getBooleanExtra(EXTRA_FAVORITES_ONLY, false);
		}else{
			mFavoritesOnly = false;
		}
		
		mStorage = AllMaps.Instance.getStorage();
		setWaitView();
	}

	protected void onResume() {
		mLocal = mStorage.getLocalCatalog();
		mOnline = mStorage.getOnlineCatalog();
		mStorage.addCatalogChangedListener(this);
		if(mLocal == null){
			mStorage.requestLocalCatalog(false);
		}else if(mMode != MODE_LIST){
			setListView();
		}
		super.onResume();
	}
	
	protected void onPause() {
		mStorage.removeCatalogChangedListener(this);
		super.onPause();
	}
	
	private void setListView() {
		if(mLocal.getMaps().size()>0 || (mOnline!=null && mOnline.getMaps().size()>0)){
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Setup map list view");
			}
			setContentView(R.layout.maps_list);
			mCatalogDifferences = Catalog.diff(mLocal, mOnline, Catalog.MODE_LEFT_JOIN);
			setContentView(R.layout.browse_catalog_main);
			mList = (ExpandableListView)findViewById(R.id.browse_catalog_list);
			mAdapter = new LocalCatalogAdapter(this, mCatalogDifferences, Locale.getDefault().getLanguage() ); 
			mList.setAdapter(mAdapter);
			mMode = MODE_LIST;
		}else if(mOnline == null && mDownloading){
			setWaitView();
		}else{
			setEmptyView();
		}
	}
	
	private void setWaitView() {
		setContentView(R.layout.maps_wait);
		mMode = MODE_WAIT;
	}
	
	private void setEmptyView() {
		setContentView(R.layout.maps_list_empty);
		((TextView)findViewById(R.id.maps_message)).setText(mFavoritesOnly ? R.string.msg_no_maps_in_favorites : R.string.msg_no_maps_in_local);
		mMode = MODE_EMPTY;
	} 

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		if(catalogId == CatalogStorage.CATALOG_LOCAL){
			if(mOnline == null){
				mDownloading = true;
				mStorage.requestOnlineCatalog(false); 
			}		
			if(catalog != null){
				mLocal = catalog;
				setListView();
			}else{
				setEmptyView();
			}
		}
		if(catalogId == CatalogStorage.CATALOG_ONLINE){
			mOnline = catalog;
			mDownloading = false;
			setListView();
		}
	}
	
}
