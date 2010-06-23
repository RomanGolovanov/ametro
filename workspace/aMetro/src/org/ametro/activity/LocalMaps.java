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

import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.adapter.GenericCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapDifference;

import android.content.Intent;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class LocalMaps extends BaseExpandableMaps {

	public static final String EXTRA_FAVORITES_ONLY = "FAVORITES_ONLY";
	
	private Catalog mLocal;
	private Catalog mOnline;
	private boolean mOnlineNotAvailable;
	
	private boolean mFavoritesOnly;
	
	
	protected void onInitialize(){
		Intent data = getIntent();
		if(data!=null){
			mFavoritesOnly = data.getBooleanExtra(EXTRA_FAVORITES_ONLY, false);
		}else{
			mFavoritesOnly = false;
		}
	}

	protected void onPrepareView() {
		mLocal = mStorage.getLocalCatalog();
		mOnline = mStorage.getOnlineCatalog();
		if(mLocal == null){
			mStorage.requestLocalCatalog(false);
		}
		if(mOnline == null){
			mStorage.requestOnlineCatalog(false);
		}
		onCatalogsUpdate();
		super.onPrepareView();
	}
	
	private void onCatalogsUpdate() {
		if(mLocal!=null && (mOnline!=null || mOnlineNotAvailable) && mMode != MODE_LIST){
			if(mLocal.getMaps().size()>0){
				setListView();
			}else{
				setEmptyView();
			}
		}		
	}
	
	protected ExpandableListAdapter getListAdapter() {
		 ArrayList<CatalogMapDifference> mCatalogDifferences = Catalog.diff(mLocal, mOnline, Catalog.MODE_LEFT_JOIN);
		return new GenericCatalogAdapter(this, mCatalogDifferences, Locale.getDefault().getLanguage() );
	}

	protected void onLocalCatalogLoaded(Catalog catalog) {
		mLocal = catalog;
		onCatalogsUpdate();
		super.onLocalCatalogLoaded(catalog);
	}
	
	protected void onOnlineCatalogLoaded(Catalog catalog) {
		mOnline = catalog;
		mOnlineNotAvailable = catalog == null;
		onCatalogsUpdate();
		super.onOnlineCatalogLoaded(catalog);
	}
	
	
	protected void onCatalogRefresh() {
		mStorage.requestLocalCatalog(true);
		super.onCatalogRefresh();
	}
	
//	public void onCatalogLoaded(int catalogId, Catalog catalog) {
//		if(catalogId == CatalogStorage.CATALOG_LOCAL){
//			if(catalog != null){
//				mLocal = catalog;
//				setListView();
//			}else{
//				setEmptyView();
//			}
//			if(mOnline == null){
//				mDownloading = true;
//				mStorage.requestOnlineCatalog(false); 
//			}		
//		}
//		if(catalogId == CatalogStorage.CATALOG_ONLINE){
//			mOnline = catalog;
//			mDownloading = false;
//			setListView();
//		}
//	}


	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		CatalogMapDifference diff = (CatalogMapDifference)mAdapter.getChild(groupPosition, childPosition);
		if(diff.isLocalAvailable()){
			String fileName = diff.getLocalUrl();
			Intent i = new Intent();
			i.setData(MapUri.create( mLocal.getBaseUrl() + "/" + fileName));
			
			AllMaps.Instance.setResult(RESULT_OK, i);
			AllMaps.Instance.finish();
		}else{
			Intent i = new Intent(this, MapDetails.class);
			i.putExtra(MapDetails.ONLINE_MAP_URL, diff.getRemoteUrl());
			startActivity(i);
		}
		return true;
	}

	protected int getEmptyListMessage() {
		return mFavoritesOnly ? R.string.msg_no_maps_in_favorites : R.string.msg_no_maps_in_local;
	}
	
}
