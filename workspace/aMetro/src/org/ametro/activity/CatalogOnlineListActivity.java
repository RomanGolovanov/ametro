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
import org.ametro.adapter.CatalogExpandableAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;

import android.widget.TextView;


public class CatalogOnlineListActivity extends BaseCatalogExpandableActivity  {

	private static final int MODE_DOWNLOAD_FAILED = 1000;

	private Catalog mLocal;
	private Catalog mOnline;

	private boolean mOnlineDownload;
	private boolean mOnlineDownloadFailed;
	
	protected void onPrepareView() {
		mLocal = mStorage.getLocalCatalog();
		mOnline = mStorage.getOnlineCatalog();
		if (mLocal == null) {
			mStorage.requestLocalCatalog(false);
		}
		if (mOnline == null && !mOnlineDownload) {
			mOnlineDownload = true;
			mOnlineDownloadFailed = false;
			mStorage.requestOnlineCatalog(false);
		}
		onCatalogsUpdate(false);
		super.onPrepareView();
	}

	private void onCatalogsUpdate(boolean refresh) {
		if(mLocal!=null && (mOnline!=null || !mOnlineDownload)){
			if(mOnline!=null && mOnline.getMaps().size()>0){
				if(mMode != MODE_LIST){
					setListView();
				}else{
					if(refresh){
						setListView();
					}
				}
			}else{
				if(mOnlineDownloadFailed){
					setDownloadFailedView();
				}else{
					setEmptyView();
				}
			}
			
		}
	}
	
	protected void onCatalogRefresh() {
		mOnlineDownload = true;
		mOnlineDownloadFailed = false;
		mStorage.requestOnlineCatalog(true);
		super.onCatalogRefresh();
	}
	
	private void setDownloadFailedView() {
		setContentView(R.layout.catalog_empty);
		((TextView) findViewById(R.id.text)).setText(R.string.msg_catalog_download_failed);
		mMode = MODE_DOWNLOAD_FAILED;
	}

	protected void onOnlineCatalogLoaded(Catalog catalog) {
		mOnline = catalog;
		mOnlineDownload = false;
		if(catalog==null){
			mOnline = CatalogStorage.getStorage().getOnlineCatalog();
		}
		mOnlineDownloadFailed = catalog!=null;
		onCatalogsUpdate(true);
		super.onOnlineCatalogLoaded(catalog);
	}
	
	protected void onLocalCatalogLoaded(Catalog catalog) {
		mLocal = catalog;
		onCatalogsUpdate(true);
		super.onLocalCatalogLoaded(catalog);
	}
	
	protected void onOnlineCatalogFailed() {
		//mOnlineDownload = false;
		//mOnline = CatalogStorage.getStorage().getOnlineCatalog();
		//mOnlineDownloadFailed = mOnline!=null;
		//onCatalogsUpdate(true);
		mOnlineDownloadFailed = true;
		super.onOnlineCatalogFailed();
	}

	protected int getEmptyListMessage() {
		return R.string.msg_no_maps_in_online;
	}

	protected CatalogExpandableAdapter getListAdapter() {
		return new CatalogExpandableAdapter(this,mLocal, mOnline, Catalog.DIFF_MODE_RIGHT, R.array.online_catalog_map_state_colors,this);
	}

	protected boolean isCatalogProgressEnabled(int catalogId) {
		return false;
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return CatalogMapState.getLocalToOnlineState(local,remote);
	}
}

