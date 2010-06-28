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
import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.ICatalogStorageListener;
import org.ametro.util.StringUtil;
import org.ametro.widget.TextStripView;
import org.ametro.widget.TextStripView.OnlineWidgetView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MapDetailsActivity extends Activity implements OnClickListener, ICatalogStorageListener {

	protected static final int MODE_WAIT = 1;
	protected static final int MODE_DETAILS = 2;
	
	protected int mMode;
	
	public static final String EXTRA_SYSTEM_NAME = "SYSTEM_NAME";
	
	public static final String EXTRA_RESULT = "EXTRA_RESULT";
	private static final int EXTRA_RESULT_OPEN = 1;
	
	private static final int MENU_DELETE = 1;

	private Button mOpenButton;
	private Button mCloseButton;
	
//	private Button mUpdateButton;
//	private Button mImportButton;
//	private Button mDownloadButton;
//	private Button mCancelButton;

	private ImageButton mFavoriteButton;

	private TextView mCityTextView;
	private TextView mCountryTextView;

	private Intent mIntent;

	private String mSystemName;

	private CatalogMap mLocal;
	private CatalogMap mOnline;
	private CatalogMap mImport;
	
	private Catalog mLocalCatalog;
	private Catalog mOnlineCatalog;
	private Catalog mImportCatalog;
	
	private boolean mOnlineDownload; 
	
	private TextStripView mContent;

	private CatalogStorage mStorage;
	
	private boolean mIsFavorite;

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_DELETE, 0, R.string.btn_delete).setIcon(android.R.drawable.ic_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_DELETE).setVisible(mLocal != null);
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DELETE:
			mStorage.deleteMap(mSystemName);
			finishWithoutResult();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIntent = getIntent();
		if (mIntent == null) {
			finishWithoutResult();
			return;
		}
		mSystemName = mIntent.getStringExtra(EXTRA_SYSTEM_NAME);
		mStorage = CatalogStorage.getStorage();
		setWaitNoProgressView();
	}
	
	protected void onResume() {
		mStorage.addCatalogChangedListener(this);
		mLocalCatalog = mStorage.getLocalCatalog();
		mOnlineCatalog = mStorage.getOnlineCatalog();
		mImportCatalog = mStorage.getImportCatalog();
		if (mLocalCatalog == null) {
			mStorage.requestLocalCatalog(false);
		}
		if (mOnlineCatalog == null && !mOnlineDownload) {
			mStorage.requestOnlineCatalog(false);
		}
		if (mImportCatalog == null) {
			mStorage.requestImportCatalog(false);
		}
		onCatalogsUpdate();
		super.onResume();
	}

	protected void onPause() {
		mStorage.removeCatalogChangedListener(this);
		super.onPause();
	}
	
	private void onCatalogsUpdate() {
		if(mLocalCatalog!=null && (mOnlineCatalog!=null || !mOnlineDownload) && mImportCatalog!=null){
			if(mLocalCatalog!=null){
				mLocal = mLocalCatalog.getMap(mSystemName);
			}
			if(mOnlineCatalog!=null){
				mOnline = mOnlineCatalog.getMap(mSystemName);
			}
			if(mImportCatalog!=null){
				mImport = mImportCatalog.getMap(mSystemName);
			}
			setDetailsView();
		}
	}	
	
	private CatalogMap preffered() {
		return mLocal != null ? mLocal : (mOnline != null ? mOnline : mImport);
	}

	public void onClick(View v) {
		if (v == mCloseButton) {
			finishWithoutResult();
		} else if (v == mOpenButton) {
			finishWithResult(EXTRA_RESULT_OPEN);
		} else if (v == mFavoriteButton) {
			mIsFavorite = !mIsFavorite;
			updateFavoriteButton();
		}
	}

	private void updateFavoriteButton() {
		if (mIsFavorite) {
			mFavoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
		} else {
			mFavoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
		}
	}

	
	protected void setWaitNoProgressView() {
		if(mMode!=MODE_WAIT){
			setContentView(R.layout.operation_wait_no_progress);
			mMode = MODE_WAIT;
		}
	}
		
	private void setDetailsView() {
		if(mMode!=MODE_DETAILS){
			setContentView(R.layout.map_details);
		
			mOpenButton = (Button) findViewById(R.id.btn_open);
			mCloseButton = (Button) findViewById(R.id.btn_close);
			mFavoriteButton = (ImageButton) findViewById(R.id.btn_favorite);
	
			mCityTextView = (TextView) findViewById(R.id.firstLine);
			mCountryTextView = (TextView) findViewById(R.id.secondLine);
	
			mContent = (TextStripView) findViewById(R.id.content);
	
			mOpenButton.setOnClickListener(this);
			mCloseButton.setOnClickListener(this);
			mFavoriteButton.setOnClickListener(this);
			
			String code = GlobalSettings.getLanguage();
			
			mCityTextView.setText(preffered().getCity(code));
			mCountryTextView.setText(preffered().getCountry(code));
	
			mContent.removeAllViews();
			
			final Resources res = getResources();
			
			String[] states = res.getStringArray(R.array.catalog_map_states);
			
			if(mOnline!=null){
				int stateId = CatalogMapState.getLocalToOnlineState(mLocal, mOnline);
				String stateName = states[stateId];
				int stateColor = (res.getIntArray(R.array.online_catalog_map_state_colors))[stateId];
				
				mContent.createHeader().setTextLeft("Online")
					.setTextRight(stateName).setTextRightColor(stateColor);
				//StringUtil.formatFileSize( mOnline.getSize(), 3 )
				
				final OnlineWidgetView v = mContent.createOnlineWidget();
				v.setSize(mOnline.getSize());
				v.setVersion("v." + mOnline.getVersion());
				v.setVisibility(CatalogMapState.DOWNLOADING);
				

				
			}
			if(mImport!=null){
				int stateId = CatalogMapState.getLocalToImportState(mLocal, mImport);
				String stateName = states[stateId];
				int stateColor = (res.getIntArray(R.array.import_catalog_map_state_colors))[stateId];
				
				mContent.createHeader().setTextLeft("Import")
					.setTextRight(stateName).setTextRightColor(stateColor);
				mContent.createText().setText("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
				//mContent.createWidget();
			}
			
			mContent.createHeader().setTextLeft("Transport");
			mContent.createText().setText("Metro, Tram, Bus");
	
			mContent.createHeader().setTextLeft("Description");
			mContent.createText().setText(preffered().getDescription(code));
		
			updateFavoriteButton();
			mMode = MODE_DETAILS;
		}
	}

	private void finishWithoutResult() {
		setResult(RESULT_CANCELED);
		finish();
	}

	private void finishWithResult(int mode) {
		Intent i = new Intent();
		i.putExtra(EXTRA_RESULT, mode);
		setResult(RESULT_OK, i);
		finish();
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		if(catalogId == CatalogStorage.CATALOG_LOCAL){
			mLocalCatalog = catalog;
		}
		if(catalogId == CatalogStorage.CATALOG_ONLINE){
			mOnlineCatalog = catalog;
			mOnlineDownload = false;
		}
		if(catalogId == CatalogStorage.CATALOG_IMPORT){
			mImportCatalog = catalog;
		}
		mUIEventDispacher.post(mCatalogsUpdate);
	}

	public void onCatalogOperationFailed(int catalogId, String message) {
	}

	public void onCatalogOperationProgress(int catalogId, int progress, int total, String message) {
	}

	protected Handler mUIEventDispacher = new Handler();
	
	private Runnable mCatalogsUpdate = new Runnable() {
		
		public void run() {
			onCatalogsUpdate();
		}
	};
	
}
