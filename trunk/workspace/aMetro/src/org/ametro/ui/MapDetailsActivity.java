/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.ui;

import java.io.File;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapState;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.CatalogStorageStateProvider;
import org.ametro.catalog.storage.ICatalogStorageListener;
import org.ametro.directory.CountryDirectory;
import org.ametro.model.TransportType;
import org.ametro.ui.view.TextStripView;
import org.ametro.ui.view.TextStripView.ImportWidgetView;
import org.ametro.ui.view.TextStripView.OnlineWidgetView;
import org.ametro.ui.view.TextStripView.TextBlockView;
import org.ametro.util.BitmapUtil;
import org.ametro.util.DateUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MapDetailsActivity extends Activity implements OnClickListener, ICatalogStorageListener {

	protected static final int MODE_WAIT = 1;
	protected static final int MODE_DETAILS = 2;

	protected int mMode;

	public static final String EXTRA_SYSTEM_NAME = "SYSTEM_NAME";

	public static final String EXTRA_RESULT = "EXTRA_RESULT";
	public static final String EXTRA_HIDE_OPEN = "EXTRA_HIDE_OPEN";
	public static final int EXTRA_RESULT_OPEN = 1;

	private static final int MENU_DELETE = 1;
	private static final int MENU_DELETE_PMZ = 2;

	private String mErrorMessage;

	private Button mOpenButton;
	private Button mCloseButton;

	private ImageView mCountryImageView;
	
	private TextView mCityTextView;
	private TextView mCountryTextView;
	private TextView mVersionTextView;

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
	private CatalogStorageStateProvider mStorageState;

	private HashMap<Integer, Drawable> mTransportTypes;
	
	private OnlineWidgetView mOnlineWidget;
	private ImportWidgetView mImportWidget;
	
	/*package*/ int mProgress;
	/*package*/ int mTotal;
	/*package*/ String mMessage;
	
	private boolean mForceOpenButtonDisable;
	
	protected Handler mUIEventDispacher = new Handler();
	private float mDisplayScale;
	
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
		String code, msg;
		AlertDialog.Builder builder;
		AlertDialog alertDialog;
		switch (item.getItemId()) {

		case MENU_DELETE:
			code = GlobalSettings.getLanguage(this);
			msg = String.format(getString(R.string.msg_delete_local_map_confirmation), mLocal.getCity(code),mLocal.getCountry(code));
			builder = new AlertDialog.Builder(this);
			builder.setMessage(msg)
			       .setCancelable(false)
			       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
				   			mStorage.deleteLocalMap(mSystemName);
							mLocal = null;
							if(mImport==null && mOnline==null){
								finishWithoutResult();
							}else{
								bindData();
							}
			           }
			       })
			       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   // put your code here 
			        	   dialog.cancel();
			           }
			       });
			alertDialog = builder.create();
			alertDialog.show();
			return true;
		
		case MENU_DELETE_PMZ:
			code = GlobalSettings.getLanguage(this);
			msg = String.format(getString(R.string.msg_delete_import_map_confirmation), mLocal.getCity(code),mLocal.getCountry(code));
			builder = new AlertDialog.Builder(this);
			builder.setMessage(msg)
			       .setCancelable(false)
			       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
				   			mStorage.deleteImportMap(mSystemName);
							mImport = null;
							if(mLocal==null && mOnline==null){
								finishWithoutResult();
							}else{
								bindData();
							}
			           }
			       })
			       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   // put your code here 
			        	   dialog.cancel();
			           }
			       });
			alertDialog = builder.create();
			alertDialog.show();
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
		mTransportTypes = TransportType.getIconsMap(this);
		mDisplayScale = getResources().getDisplayMetrics().density;
		mSystemName = mIntent.getStringExtra(EXTRA_SYSTEM_NAME);
		mForceOpenButtonDisable = mIntent.getBooleanExtra(EXTRA_HIDE_OPEN, false); 
		mStorage =  ((ApplicationEx)getApplicationContext()).getCatalogStorage();
		mStorageState = new CatalogStorageStateProvider(mStorage);
		setWaitNoProgressView();
	}

	protected void onResume() {
		mStorage.addCatalogStorageListener(this);
		mLocalCatalog = mStorage.getCatalog(CatalogStorage.LOCAL);
		mOnlineCatalog = mStorage.getCatalog(CatalogStorage.ONLINE);
		mImportCatalog = mStorage.getCatalog(CatalogStorage.IMPORT);
		if (mLocalCatalog == null) {
			mStorage.requestCatalog(CatalogStorage.LOCAL, false);
		}
		if (mOnlineCatalog == null && !mOnlineDownload) {
			mStorage.requestCatalog(CatalogStorage.ONLINE, false);
		}
		if (mImportCatalog == null) {
			mStorage.requestCatalog(CatalogStorage.IMPORT, false);
		}
		onCatalogsUpdate();
		super.onResume();
	}

	protected void onPause() {
		mStorage.removeCatalogStorageListener(this);
		super.onPause();
	}

	private void onCatalogsUpdate() {
		if (mLocalCatalog != null && (mOnlineCatalog != null || !mOnlineDownload) && mImportCatalog != null) {
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
		} 
		if (v == mOpenButton) { 
			ComponentName callingActivity = getCallingActivity();
			if(callingActivity!=null){
				Intent i = new Intent();
				i.putExtra(EXTRA_RESULT, EXTRA_RESULT_OPEN);
				i.putExtra(EXTRA_SYSTEM_NAME, mSystemName);
				setResult(RESULT_OK, i);
				finish();				
			}else{
				Intent i = new Intent(this, MapViewActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Constants.EXTRA_SYSTEM_MAP_NAME, mSystemName);
				startActivity(i);
			}
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

	protected void setWaitNoProgressView() {
		if (mMode != MODE_WAIT) {
			setContentView(R.layout.operatoins_wait);
			ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress);
			progressBar.setIndeterminate(true);
			mMode = MODE_WAIT;
		}
	}

	private void setDetailsView() {
		if (mMode != MODE_DETAILS) {
			setContentView(R.layout.map_details);
			mCountryImageView = (ImageView)findViewById(R.id.iso_icon);
			mOpenButton = (Button) findViewById(R.id.btn_open);
			mCloseButton = (Button) findViewById(R.id.btn_close);
			mCityTextView = (TextView) findViewById(R.id.firstLine);
			mCountryTextView = (TextView) findViewById(R.id.secondLine);
			mVersionTextView = (TextView)findViewById(R.id.version);
			mContent = (TextStripView) findViewById(R.id.content);
			mOpenButton.setOnClickListener(this);
			mCloseButton.setOnClickListener(this);
			bindData();
			mMode = MODE_DETAILS;
		}else{
			bindData();
		}
	}

	private void bindData(){

		String code = GlobalSettings.getLanguage(this);

		mCityTextView.setText(preffered().getCity(code));
		mCountryTextView.setText(preffered().getCountry(code));
		if(mLocal!=null){
			mVersionTextView.setText(DateUtil.getLocalDate(mLocal.getTimestamp()));
		}else{
			mVersionTextView.setText("");
		}

		final Resources res = getResources();
		final String[] states = res.getStringArray(R.array.catalog_map_states);
		final String[] transportNames = res.getStringArray(R.array.transport_types);
		
		if(GlobalSettings.isCountryIconsEnabled(this)){
			mCountryImageView.setVisibility(View.VISIBLE);
	        final CountryDirectory countryDirectory = ApplicationEx.getInstance().getCountryDirectory();
	    	CountryDirectory.Entity entity = countryDirectory.getByName(preffered().getCountry(code));
	    	if(entity!=null){
				File file = new File(Constants.ICONS_PATH, entity.getISO2() + ".png");
				if(file.exists()){
					Bitmap bmp = BitmapUtil.createScaledBitmap(file.getAbsolutePath(), mDisplayScale, false);
		    		mCountryImageView.setImageDrawable(new BitmapDrawable(bmp));
				}else{
		    		mCountryImageView.setImageResource(R.drawable.no_country);
				}
	    	}else{
	    		mCountryImageView.setImageResource(R.drawable.no_country);
	    	}
		}else{
			mCountryImageView.setVisibility(View.GONE);
		}
		
		
		mOpenButton.setVisibility( (mLocal!=null && !mForceOpenButtonDisable) ? View.VISIBLE : View.GONE);
		
		mContent.removeAllViews();
		if (mOnline != null) {
			int stateId = mStorageState.getOnlineCatalogState(mLocal, mOnline);
			String stateName = states[stateId];
			int stateColor = (res.getIntArray(R.array.online_catalog_map_state_colors))[stateId];
			if(stateId == CatalogMapState.IMPORTING || stateId == CatalogMapState.IMPORT_PENDING){
				stateName = "";
			}
			mContent.createHeader().setTextLeft(getString(R.string.msg_online)).setTextRight(stateName).setTextRightColor(stateColor);
			mOnlineWidget = mContent.createOnlineWidget();
			mOnlineWidget.setSize(mOnline.getSize());
			mOnlineWidget.setVersion(DateUtil.getLocalDate(mOnline.getTimestamp()));
			mOnlineWidget.setVisibility(stateId);
			mOnlineWidget.getDownloadButton().setOnClickListener(this);
			mOnlineWidget.getUpdateButton().setOnClickListener(this);
			mOnlineWidget.getCancelButton().setOnClickListener(this);
		}
		if (mImport != null) {
			int stateId = mStorageState.getImportCatalogState(mLocal, mImport);
			String stateName = states[stateId];
			int stateColor = (res.getIntArray(R.array.import_catalog_map_state_colors))[stateId];
			if(stateId == CatalogMapState.DOWNLOADING || stateId == CatalogMapState.DOWNLOAD_PENDING){
				stateName = "";
			}
			mContent.createHeader().setTextLeft(getString(R.string.msg_import)).setTextRight(stateName).setTextRightColor(stateColor);
			mImportWidget = mContent.createImportWidget();
			mImportWidget.setSize(mImport.getSize());
			mImportWidget.setVersion(DateUtil.getLocalDate(mImport.getTimestamp()));
			mImportWidget.setVisibility(stateId);
			mImportWidget.getImportButton().setOnClickListener(this);
			mImportWidget.getUpdateButton().setOnClickListener(this);
			mImportWidget.getCancelButton().setOnClickListener(this);
		}

		boolean transportHeaderCreated = false;
		
		long transports = preffered().getTransports();
		long transportCode = 1;
		int transportId = 0;
		while (transports > 0) {
			if ((transports % 2) > 0) {
				Drawable d = mTransportTypes.get((int) transportCode);
				if(!transportHeaderCreated){
					mContent.createHeader().setTextLeft(getString(R.string.msg_transports));
					transportHeaderCreated = true;
				}
				mContent.createTransportWidget().setImageDrawable(d).setText(transportNames[transportId]);
			}
			transports = transports >> 1;
			transportCode = transportCode << 1;
			transportId++;
		}

		String description = preffered().getDescription(code);
		if(description!=null){
			if(Constants.LOCALE_RU.equals(code)){
				description = description.replaceAll("\n", "<br/>").replaceAll("\\\\n", "<br/>");
			}else{
				description = getString(R.string.msg_no_description);
			}
		}else{
			description = getString(R.string.msg_no_description);
		}
		mContent.createHeader().setTextLeft(getString(R.string.msg_description));
		TextBlockView text = mContent.createText();
		text.setText(Html.fromHtml(description));
		Linkify.addLinks(text.getText(), Linkify.WEB_URLS);
	}
	
	private void finishWithoutResult() {
		setResult(RESULT_CANCELED);
		finish();
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		if (catalogId == CatalogStorage.LOCAL) {
			mLocalCatalog = catalog;
		}
		if (catalogId == CatalogStorage.ONLINE) {
			mOnlineCatalog = catalog;
			mOnlineDownload = false;
		}
		if (catalogId == CatalogStorage.IMPORT) {
			mImportCatalog = catalog;
		}
		mUIEventDispacher.post(mCatalogsUpdateRunnable);
	}

	public void onCatalogFailed(int catalogId, String message) {
		if (GlobalSettings.isDebugMessagesEnabled(this)) {
			mErrorMessage = message;
			mUIEventDispacher.post(mCatalogError);
		}
	}

	public void onCatalogProgress(int catalogId, int progress, int total, String message) {
	}

	public void onCatalogMapChanged(String systemName) {
		if(mSystemName.equals(systemName) ){
			mUIEventDispacher.post(mDataBindRunnable);
		}
	}

	public void onCatalogMapDownloadFailed(String systemName, Throwable ex){
		if(mSystemName.equals(systemName) ){
			mMessage = "Failed download map " + systemName;
			mUIEventDispacher.post(mShowErrorRunnable);
			mUIEventDispacher.post(mDataBindRunnable);
		}
	}

	public void onCatalogMapImportFailed(String systemName, Throwable ex){
		if(mSystemName.equals(systemName) ){
			mMessage = "Failed import map " + systemName;
			mUIEventDispacher.post(mShowErrorRunnable);
			mUIEventDispacher.post(mDataBindRunnable);
		}
	}

	public void onCatalogMapDownloadProgress(String systemName, int progress, int total) {
		if(mOnlineWidget!=null && mSystemName.equals(systemName) && (progress!=0 && total!=0)){
			mTotal = total;
			mProgress = progress;
			mUIEventDispacher.post(mDownloadProgressUpdateRunnable);
		}
	}

	public void onCatalogMapImportProgress(String systemName, int progress, int total) {
		if(mImportWidget!=null && mSystemName.equals(systemName) && (progress!=0 && total!=0)){
			mTotal = total;
			mProgress = progress;
			mUIEventDispacher.post(mImportProgressUpdateRunnable);
		}
	}

	public void onCatalogMapDownloadDone(String systemName) {
		if(mSystemName.equals(systemName) ){
			if(mMode == MODE_DETAILS){
				mUIEventDispacher.post(mDataBindRunnable);
			}
		}
	}

	public void onCatalogMapImportDone(String systemName) {
		if(mSystemName.equals(systemName) ){
			if(mMode == MODE_DETAILS){
				mUIEventDispacher.post(mDataBindRunnable);
			}
		}
	}
	
	private Runnable mImportProgressUpdateRunnable = new Runnable() {
		public void run() {
			mImportWidget.setProgress(mProgress, mTotal);
		}
	};
	
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
