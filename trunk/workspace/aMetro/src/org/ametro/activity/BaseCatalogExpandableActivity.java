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

import org.ametro.GlobalSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.adapter.CatalogExpandableAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.ICatalogStatusProvider;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.ICatalogStorageListener;
import org.ametro.dialog.LocationSearchDialog;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

public abstract class BaseCatalogExpandableActivity extends Activity implements ICatalogStorageListener, ICatalogStatusProvider, OnChildClickListener {

	protected static final int MODE_WAIT_NO_PROGRESS = 1;
	protected static final int MODE_WAIT = 2;
	protected static final int MODE_LIST = 3;
	protected static final int MODE_EMPTY = 4;
	
	protected int mMode;
	
	protected CatalogStorage mStorage;
	
	protected CatalogExpandableAdapter mAdapter;
	protected ExpandableListView mList;

	protected TextView mCounterTextView;
	protected TextView mMessageTextView;
	protected ProgressBar mProgressBar;
	
	protected int mProgress;
	protected int mTotal;
	protected String mMessage;
	
	protected String mErrorMessage;

	protected Handler mUIEventDispacher = new Handler();
	
	private final int MAIN_MENU_REFRESH = 997;
	private final int MAIN_MENU_LOCATION = 998;
	private final int MAIN_MENU_SETTINGS = 999;
	private final int MAIN_MENU_ABOUT = 1000;

	private final static int REQUEST_LOCATION = 998;
	private final static int REQUEST_SETTINGS = 999;
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_REFRESH, Menu.NONE, R.string.menu_refresh).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, MAIN_MENU_LOCATION, Menu.NONE, R.string.menu_location).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, MAIN_MENU_SETTINGS, 999, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 1000, R.string.menu_about).setIcon(android.R.drawable.ic_menu_help);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_LOCATION).setEnabled(mMode == MODE_LIST);
		menu.findItem(MAIN_MENU_REFRESH).setEnabled( (mMode != MODE_WAIT) && (mMode != MODE_WAIT_NO_PROGRESS) );
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_REFRESH:
			onCatalogRefresh();
			return true;
		case MAIN_MENU_LOCATION:
			startActivityForResult(new Intent(this, LocationSearchDialog.class), REQUEST_LOCATION);
			return true;
		case MAIN_MENU_SETTINGS:
			startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
			return true;
		case MAIN_MENU_ABOUT:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SETTINGS:
			onSettingsChanged();
			break;
		case REQUEST_LOCATION:
			if(resultCode == RESULT_OK){
				Location location = data.getParcelableExtra(LocationSearchDialog.LOCATION);
				if(location!=null){
					onLocationSearch(location);
				}else{
					onLocationSearchUnknown();
				}
			}
			if(resultCode == RESULT_CANCELED){
				onLocationSearchCanceled();	
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GlobalSettings.checkPrerequisite(this);
		mStorage = CatalogStorage.getStorage();
		setWaitNoProgressView();
		onInitialize();
	}

	protected void onResume() {
		mStorage.addCatalogChangedListener(this);
		onPrepareView();
		super.onResume();
	}

	protected void onPause() {
		mStorage.removeCatalogChangedListener(this);
		super.onPause();
	}
		
	protected void setEmptyView() {
		if(mMode!=MODE_EMPTY){
			setContentView(R.layout.catalog_empty);
			((TextView)findViewById(R.id.maps_message)).setText(getEmptyListMessage());
			mMode = MODE_EMPTY;
		}
	} 

	protected void setListView() {
		setContentView(R.layout.catalog_expandable_list);
		mList = (ExpandableListView)findViewById(R.id.browse_catalog_list);
		mAdapter = getListAdapter(); 
		mList.setAdapter(mAdapter);
		mList.setOnChildClickListener(this);
		mMode = MODE_LIST;
	}
	
	protected void setWaitView() {
		if(mMode!=MODE_WAIT){
			setContentView(R.layout.operatoins_wait);
			mMessageTextView = (TextView)findViewById(R.id.message);
			mCounterTextView = (TextView)findViewById(R.id.counter);
			mProgressBar = (ProgressBar)findViewById(R.id.progress);
			mMode = MODE_WAIT;
		}
	}
	
	protected void setWaitNoProgressView() {
		if(mMode!=MODE_WAIT_NO_PROGRESS){
			setContentView(R.layout.operation_wait_no_progress);
			mMode = MODE_WAIT_NO_PROGRESS;
		}
	}
	
	public void onCatalogOperationFailed(int catalogId, String message)
	{
		if(GlobalSettings.isDebugMessagesEnabled()){
			mErrorMessage = message;
			mUIEventDispacher.post(mCatalogError);
		}
		if(catalogId == CatalogStorage.CATALOG_LOCAL){
			onLocalCatalogFailed();
		}
		if(catalogId == CatalogStorage.CATALOG_IMPORT){
			onImportCatalogFailed();
		}
		if(catalogId == CatalogStorage.CATALOG_ONLINE){
			onOnlineCatalogFailed();
		}		
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		if(catalogId == CatalogStorage.CATALOG_LOCAL){
			onLocalCatalogLoaded(catalog);
		}
		if(catalogId == CatalogStorage.CATALOG_IMPORT){
			onImportCatalogLoaded(catalog);
		}
		if(catalogId == CatalogStorage.CATALOG_ONLINE){
			onOnlineCatalogLoaded(catalog);
		}
	}
	
	public void onCatalogOperationProgress(int catalogId, int progress, int total, String message)
	{
		if(isCatalogProgressEnabled(catalogId)){
			mProgress = progress;
			mTotal = total;
			mMessage = message;
			mUIEventDispacher.post(mUpdateProgress);
		}
	}
	
	protected Runnable mCatalogError = new Runnable() {
		public void run() {
			Toast.makeText(BaseCatalogExpandableActivity.this, mErrorMessage, Toast.LENGTH_LONG).show();
		}
	};
	
	protected Runnable mUpdateProgress = new Runnable() {
		public void run() {
			if(mMode!=MODE_WAIT){
				setWaitView();
			}
			mProgressBar.setMax(mTotal);
			mProgressBar.setProgress(mProgress);
			mMessageTextView.setText( mMessage );
			mCounterTextView.setText( mProgress + " / " + mTotal );
		}
	};

	protected abstract int getEmptyListMessage();
	protected abstract CatalogExpandableAdapter getListAdapter();

	protected void onInitialize() {};
	protected void onPrepareView() {};
	
	protected void onCatalogRefresh() { 
		setWaitNoProgressView();
	};
	protected void onLocationSearch(Location location) {};

	
	protected void onSettingsChanged() {
		if(mMode == MODE_LIST){
			String oldLanguage = mAdapter.getLanguage();
			String newLanguage = GlobalSettings.getLanguage();
			if(!oldLanguage.equalsIgnoreCase(newLanguage)){
				mAdapter = getListAdapter(); 
				mList.setAdapter(mAdapter);
			}
		}
	}

	protected void onLocalCatalogLoaded(Catalog catalog) {};
	protected void onOnlineCatalogLoaded(Catalog catalog) {};
	protected void onImportCatalogLoaded(Catalog catalog) {};
	
	protected abstract boolean isCatalogProgressEnabled(int catalogId);
	//protected abstract Catalog

	protected void onLocalCatalogFailed() {};
	protected void onOnlineCatalogFailed() {};
	protected void onImportCatalogFailed() {};
	
	protected void onLocationSearchCanceled() {}		
	protected void onLocationSearchUnknown() {
		Toast.makeText(this,R.string.msg_location_unknown, Toast.LENGTH_SHORT).show();			
	}		
	
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		CatalogMapPair diff = (CatalogMapPair)mAdapter.getChild(groupPosition, childPosition);
//		if(diff.isUpdateAvailable()){
//			Intent i = new Intent(this, MapDetailsActivity.class);
//			i.putExtra(MapDetailsActivity.REMOTE_MAP_URL, diff.getRemoteUrl());
//			startActivity(i);
//		}else if(diff.isLocalAvailable()){
//			CatalogMap local = diff.getLocal();
//			Intent i = new Intent();
//			i.setData(MapUri.create(local.getAbsoluteUrl()));
//			CatalogTabHostActivity.getInstance().setResult(RESULT_OK, i);
//			CatalogTabHostActivity.getInstance().finish();
//		}
		
		return onCatalogMapClick(diff.getLocal(), diff.getRemote());		
	}

	public boolean onCatalogMapClick(CatalogMap local, CatalogMap remote) {
		int state =  getCatalogStatus(local, remote);
		switch(state){
		case OFFLINE:
		case INSTALLED:
			invokeFinish(local);
			return true;
		case UPDATE:
		case IMPORT:
		case DOWNLOAD:
			invokeMapDetails(remote);
			return true;
		case NOT_SUPPORTED:
		case UPDATE_NOT_SUPPORTED:
		case CORRUPTED:
			// do nothing
			return true;
		}		
		return false;
	}

	protected void invokeMapDetails(CatalogMap remote) {
		Intent detailsIntent = new Intent(this, MapDetailsActivity.class);
		detailsIntent.putExtra(MapDetailsActivity.REMOTE_MAP_URL, remote.getUrl());
		startActivity(detailsIntent);
	}

	protected void invokeFinish(CatalogMap local) {
		Intent viewIntent = new Intent();
		viewIntent.setData(MapUri.create(local.getAbsoluteUrl()));
		Activity parent =  CatalogTabHostActivity.getInstance();
		if(parent!=null){
			parent.setResult(RESULT_OK, viewIntent);
			parent.finish();
		}else{
			setResult(RESULT_OK, viewIntent);
			finish();
		}
	}
	
	
}
