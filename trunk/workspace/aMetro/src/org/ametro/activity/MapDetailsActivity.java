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

import java.util.HashMap;

import org.ametro.ApplicationEx;
import org.ametro.GlobalSettings;
import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.ICatalogStorageListener;
import org.ametro.model.TransportType;
import org.ametro.widget.TextStripView;
import org.ametro.widget.TextStripView.ImportWidgetView;
import org.ametro.widget.TextStripView.OnlineWidgetView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MapDetailsActivity extends Activity implements OnClickListener, ICatalogStorageListener {

	protected static final int MODE_WAIT = 1;
	protected static final int MODE_DETAILS = 2;

	protected int mMode;

	public static final String EXTRA_SYSTEM_NAME = "SYSTEM_NAME";

	public static final String EXTRA_RESULT = "EXTRA_RESULT";
	private static final int EXTRA_RESULT_OPEN = 1;

	private static final int MENU_DELETE = 1;
	private static final int MENU_DELETE_PMZ = 2;

	private String mErrorMessage;

	private Button mOpenButton;
	private Button mCloseButton;

	// private Button mUpdateButton;
	// private Button mImportButton;
	// private Button mDownloadButton;
	// private Button mCancelButton;

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
	private HashMap<Integer, Drawable> mTransportTypes;

	private OnlineWidgetView mOnlineWidget;
	private ImportWidgetView mImportWidget;
	
	/*package*/ int mProgress;
	/*package*/ int mTotal;
	/*package*/ String mMessage;
	
	protected Handler mUIEventDispacher = new Handler();

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_DELETE, 0, R.string.btn_delete).setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, MENU_DELETE_PMZ, 0, R.string.btn_delete_pmz).setIcon(android.R.drawable.ic_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_DELETE).setVisible(mLocal != null);
		menu.findItem(MENU_DELETE_PMZ).setVisible(mImport != null);
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DELETE:
			mStorage.deleteLocalMap(mSystemName);
			mLocal = null;
			if(mImport==null && mOnline==null){
				finishWithoutResult();
			}
			bindData();
			return true;
		case MENU_DELETE_PMZ:
			mStorage.deleteImportMap(mSystemName);
			mImport = null;
			if(mLocal==null && mOnline==null){
				finishWithoutResult();
			}
			bindData();
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

		mTransportTypes = new HashMap<Integer, Drawable>();
		final Resources res = getResources();
		
		mTransportTypes.put(TransportType.UNKNOWN_ID, res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.UNKNOWN_ID)));
		mTransportTypes.put(TransportType.METRO_ID, res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.METRO_ID)));
		mTransportTypes.put(TransportType.TRAM_ID, res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.TRAM_ID)));
		mTransportTypes.put(TransportType.BUS_ID, res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.BUS_ID)));
		mTransportTypes.put(TransportType.TRAIN_ID, res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.TRAIN_ID)));
		mTransportTypes.put(TransportType.WATER_BUS_ID, res .getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.WATER_BUS_ID)));
		mTransportTypes.put(TransportType.TROLLEYBUS_ID,res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.TROLLEYBUS_ID)));

		mSystemName = mIntent.getStringExtra(EXTRA_SYSTEM_NAME);
		mStorage =  ((ApplicationEx)getApplicationContext()).getCatalogStorage();
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
		if (mLocalCatalog != null
				&& (mOnlineCatalog != null || !mOnlineDownload)
				&& mImportCatalog != null) {
			if (mLocalCatalog != null) {
				mLocal = mLocalCatalog.getMap(mSystemName);
			}
			if (mOnlineCatalog != null) {
				mOnline = mOnlineCatalog.getMap(mSystemName);
			}
			if (mImportCatalog != null) {
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
		if (mOnlineWidget != null) {
			if (v == mOnlineWidget.getCancelButton()) {
				mStorage.cancelDownload(mSystemName);
			} else if (v == mOnlineWidget.getDownloadButton()) {
				mStorage.requestDownload(mSystemName);
			} else if (v == mOnlineWidget.getUpdateButton()) {
				mStorage.requestDownload(mSystemName);
			}
		}
		if (mImportWidget != null) {
			if (v == mImportWidget.getCancelButton()) {
				mStorage.cancelImport(mSystemName);
			} else if (v == mImportWidget.getImportButton()) {
				mStorage.requestImport(mSystemName);
			} else if (v == mImportWidget.getUpdateButton()) {
				mStorage.requestImport(mSystemName);
			}
		}
	}

	private void updateFavoriteButton() {
		mFavoriteButton.setVisibility(mLocal != null ? View.VISIBLE : View.GONE);
		mFavoriteButton.setImageResource(mIsFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
	}

	protected void setWaitNoProgressView() {
		if (mMode != MODE_WAIT) {
			setContentView(R.layout.operation_wait_no_progress);
			mMode = MODE_WAIT;
		}
	}

	private void setDetailsView() {
		if (mMode != MODE_DETAILS) {
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
			bindData();
			mMode = MODE_DETAILS;
		}
	}

	private void bindData(){

		String code = GlobalSettings.getLanguage(this);

		mCityTextView.setText(preffered().getCity(code));
		mCountryTextView.setText(preffered().getCountry(code));

		final Resources res = getResources();
		final String[] states = res.getStringArray(R.array.catalog_map_states);
		final String[] transportNames = res.getStringArray(R.array.transport_types);
		
		mContent.removeAllViews();
		if (mOnline != null) {
			int stateId = mStorage.getOnlineCatalogState(mLocal,mOnline);
			String stateName = states[stateId];
			int stateColor = (res
					.getIntArray(R.array.online_catalog_map_state_colors))[stateId];
			mContent.createHeader().setTextLeft("Online").setTextRight(
					stateName).setTextRightColor(stateColor);
			mOnlineWidget = mContent.createOnlineWidget();
			mOnlineWidget.setSize(mOnline.getSize());
			mOnlineWidget.setVersion("v." + mOnline.getVersion());
			mOnlineWidget.setVisibility(stateId);
			mOnlineWidget.getDownloadButton().setOnClickListener(this);
			mOnlineWidget.getUpdateButton().setOnClickListener(this);
			mOnlineWidget.getCancelButton().setOnClickListener(this);
		}
		if (mImport != null) {
			int stateId = mStorage.getImportCatalogState(mLocal,
					mImport);
			String stateName = states[stateId];
			int stateColor = (res
					.getIntArray(R.array.import_catalog_map_state_colors))[stateId];
			mContent.createHeader().setTextLeft("Import").setTextRight(
					stateName).setTextRightColor(stateColor);
			mImportWidget = mContent.createImportWidget();
			mImportWidget.setSize(mImport.getSize());
			mImportWidget.setVersion("v." + mImport.getVersion());
			mImportWidget.setVisibility(stateId);
			mImportWidget.getImportButton().setOnClickListener(this);
			mImportWidget.getUpdateButton().setOnClickListener(this);
			mImportWidget.getCancelButton().setOnClickListener(this);
		}

		mContent.createHeader().setTextLeft("Transports");
		long transports = preffered().getTransports();
		long transportCode = 1;
		int transportId = 0;
		while (transports > 0) {
			if ((transports % 2) > 0) {
				Drawable d = mTransportTypes.get((int) transportCode);
				mContent.createTransportWidget().setImageDrawable(d)
						.setText(transportNames[transportId]);
			}
			transports = transports >> 1;
			transportCode = transportCode << 1;
			transportId++;
		}

		mContent.createHeader().setTextLeft("Description");
		mContent.createText().setText(preffered().getDescription(code));

		updateFavoriteButton();
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
		if (catalogId == CatalogStorage.CATALOG_LOCAL) {
			mLocalCatalog = catalog;
		}
		if (catalogId == CatalogStorage.CATALOG_ONLINE) {
			mOnlineCatalog = catalog;
			mOnlineDownload = false;
		}
		if (catalogId == CatalogStorage.CATALOG_IMPORT) {
			mImportCatalog = catalog;
		}
		mUIEventDispacher.post(mCatalogsUpdateRunnable);
	}

	public void onCatalogOperationFailed(int catalogId, String message) {
		if (GlobalSettings.isDebugMessagesEnabled(this)) {
			mErrorMessage = message;
			mUIEventDispacher.post(mCatalogError);
		}
	}

	public void onCatalogOperationProgress(int catalogId, int progress, int total, String message) {
	}

	public void onCatalogMapChanged(String systemName) {
		if(mSystemName.equals(systemName) ){
			if(mMode == MODE_DETAILS){
				mUIEventDispacher.post(mDataBindRunnable);
			}
		}
	}

	public void onCatalogMapDownloadFailed(String systemName, Throwable ex){
		mMessage = "Failed download map " + systemName;
		if(GlobalSettings.isDebugMessagesEnabled(this)){
			mMessage += " due error: " + ex.getMessage();
		}
		mUIEventDispacher.post(mShowErrorRunnable);
	}

	public void onCatalogMapImportFailed(String systemName, Throwable ex){
		mMessage = "Failed import map " + systemName;
		if(GlobalSettings.isDebugMessagesEnabled(this)){
			mMessage += " due error: " + ex.getMessage();
		}
		mUIEventDispacher.post(mShowErrorRunnable);
	}

	public void onCatalogMapDownloadProgress(String systemName, int progress, int total) {
		if(mOnlineWidget!=null && mSystemName.equals(systemName)){
			mTotal = total;
			mProgress = progress;
			mUIEventDispacher.post(mDownloadProgressUpdateRunnable);
		}
	}

	private Runnable mDownloadProgressUpdateRunnable = new Runnable() {
		public void run() {
			mOnlineWidget.setProgress(mProgress, mTotal);
		}
	};
	
	private Runnable mCatalogsUpdateRunnable = new Runnable() {
		public void run() {
			onCatalogsUpdate();
		}
	};

	private Runnable mDataBindRunnable = new Runnable() {
		public void run() {
			if(mMode == MODE_DETAILS){
				bindData();
			}
		}
	};

	private Runnable mShowErrorRunnable = new Runnable() {
		public void run() {
			Toast.makeText(MapDetailsActivity.this, mMessage, Toast.LENGTH_LONG).show();
		}
	};
	
	private Runnable mCatalogError = new Runnable() {
		public void run() {
			Toast.makeText(MapDetailsActivity.this, mErrorMessage,
					Toast.LENGTH_LONG).show();
		}
	};


}
