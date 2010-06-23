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

import org.ametro.MapSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.adapter.GenericCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapDifference;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.ICatalogStorageListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

public class OnlineMaps extends Activity implements ICatalogStorageListener,
		OnChildClickListener {

	private static final int MODE_WAIT = 0;
	private static final int MODE_LIST = 1;
	private static final int MODE_EMPTY = 2;
	private static final int MODE_DOWNLOAD_FAILED = 3;

	private CatalogStorage mStorage;
	private Catalog mLocal;
	private Catalog mOnline;

	private boolean mOnlineDownload;
	private boolean mOnlineDownloadFailed;

	private int mMode;

	private GenericCatalogAdapter mAdapter;
	private ArrayList<CatalogMapDifference> mCatalogDifferences;
	private ExpandableListView mList;

	private Handler mUIEventDispacher = new Handler();

	private String mErrorMessage;

	private final int MAIN_MENU_REFRESH = 1;
	private final int MAIN_MENU_LOCATION = 2;
	private final int MAIN_MENU_SETTINGS = 3;
	private final int MAIN_MENU_ABOUT = 4;

	private final static int REQUEST_SETTINGS = 1;
	private final static int REQUEST_LOCATION = 2;

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_REFRESH, 0, R.string.menu_refresh).setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(0, MAIN_MENU_LOCATION, 1, R.string.menu_location).setIcon(
				android.R.drawable.ic_menu_mylocation);
		menu.add(0, MAIN_MENU_SETTINGS, 2, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 3, R.string.menu_about).setIcon(
				android.R.drawable.ic_menu_help);

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_LOCATION).setVisible(true);
		menu.findItem(MAIN_MENU_REFRESH).setEnabled(mMode != MODE_WAIT);
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_REFRESH:
			setWaitView();
			invokeRequestOnlineCatalog(true);
			return true;
		case MAIN_MENU_LOCATION:
			startActivityForResult(new Intent(this, SearchLocation.class),
					REQUEST_LOCATION);
			return true;
		case MAIN_MENU_SETTINGS:
			startActivityForResult(new Intent(this, Settings.class),
					REQUEST_SETTINGS);
			return true;
		case MAIN_MENU_ABOUT:
			startActivity(new Intent(this, About.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapSettings.checkPrerequisite(this);
		mStorage = AllMaps.Instance.getStorage();
		setWaitView();
	}

	protected void onResume() {
		mLocal = mStorage.getLocalCatalog();
		mOnline = mStorage.getOnlineCatalog();
		mStorage.addCatalogChangedListener(this);
		if (mLocal == null) {
			mStorage.requestLocalCatalog(false);
		}
		if (mOnline == null && !mOnlineDownloadFailed) {
			invokeRequestOnlineCatalog(false);
		}
		invalidateCatalogs();
		super.onResume();
	}

	private void invokeRequestOnlineCatalog(boolean refresh) {
		mOnlineDownload = true;
		mOnlineDownloadFailed = false;
		mStorage.requestOnlineCatalog(refresh);
	}

	protected void onPause() {
		mStorage.removeCatalogChangedListener(this);
		super.onPause();
	}

	private void invalidateCatalogs() {
		if (mMode != MODE_LIST) {
			if (mOnlineDownload) {
				setWaitView();
			} else {
				if (mOnline == null && mOnlineDownloadFailed) {
					setDownloadFailedView();
				} else if (mLocal == null) {
					setWaitView();
				} else if (mLocal != null && mOnline != null
						&& mOnline.getMaps().size() > 0) {
					setListView();
				} else {
					setEmptyView();
				}
			}
		}
	}

	private void setEmptyView() {
		setContentView(R.layout.maps_list_empty);
		((TextView) findViewById(R.id.maps_message))
				.setText(R.string.msg_no_maps_in_online);
		mMode = MODE_EMPTY;
	}

	private void setListView() {
		setContentView(R.layout.browse_catalog_list_item);
		mCatalogDifferences = Catalog.diff(mLocal, mOnline,
				Catalog.MODE_RIGHT_JOIN);
		setContentView(R.layout.browse_catalog_main);
		mList = (ExpandableListView) findViewById(R.id.browse_catalog_list);
		mAdapter = new GenericCatalogAdapter(this, mCatalogDifferences, Locale
				.getDefault().getLanguage());
		mList.setAdapter(mAdapter);
		mList.setOnChildClickListener(this);
		mMode = MODE_LIST;
	}

	private void setWaitView() {
		setContentView(R.layout.maps_wait_indeterminate);
		mMode = MODE_WAIT;
	}

	private void setDownloadFailedView() {
		setContentView(R.layout.maps_list_empty);
		((TextView) findViewById(R.id.maps_message))
				.setText(R.string.msg_catalog_download_failed);
		mMode = MODE_DOWNLOAD_FAILED;
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		if (catalogId == CatalogStorage.CATALOG_LOCAL) {
			mLocal = catalog;
			invalidateCatalogs();
		}
		if (catalogId == CatalogStorage.CATALOG_ONLINE) {
			mOnline = catalog;
			mOnlineDownload = false;
			invalidateCatalogs();
		}
	}

	public void onCatalogOperationFailed(int catalogId, String message) {
		if (catalogId == CatalogStorage.CATALOG_ONLINE) {
			mOnlineDownloadFailed = true;
		}
		if (MapSettings.isDebugMessagesEnabled()) {
			mErrorMessage = message;
			mUIEventDispacher.post(mCatalogError);
		}
	}

	public void onCatalogOperationProgress(int catalogId, int progress,
			int total, String message) {
	}

	private Runnable mCatalogError = new Runnable() {
		public void run() {
			Toast.makeText(OnlineMaps.this, mErrorMessage, Toast.LENGTH_LONG)
					.show();
		}
	};

	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		CatalogMapDifference diff = (CatalogMapDifference) mAdapter.getChild(
				groupPosition, childPosition);
		if (diff.isLocalAvailable()) {
			String fileName = diff.getLocalUrl();
			Intent i = new Intent();
			i.setData(MapUri.create(mLocal.getBaseUrl() + "/" + fileName));

			AllMaps.Instance.setResult(RESULT_OK, i);
			AllMaps.Instance.finish();
		} else {
			Intent i = new Intent(this, MapDetails.class);
			i.putExtra(MapDetails.ONLINE_MAP_URL, diff.getRemoteUrl());
			startActivity(i);
		}
		return true;
	}

}
