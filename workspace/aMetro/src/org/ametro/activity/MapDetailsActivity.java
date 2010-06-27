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
import org.ametro.widget.TextStripView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MapDetailsActivity extends Activity implements OnClickListener {

	
    public static final String EXTRA_SYSTEM_NAME = "SYSTEM_NAME";
    public static final String EXTRA_LOCAL_MAP_URL = "LOCAL_URL";
	public static final String EXTRA_REMOTE_MAP_URL = "REMOTE_URL";
	public static final String EXTRA_STATE = "STATE";
	public static final String EXTRA_RESULT = "RESULT";

	public static final int EXTRA_RESULT_UPDATE = 1;
	public static final int EXTRA_RESULT_IMPORT = 2;
	public static final int EXTRA_RESULT_DOWNLOAD = 3;
	public static final int EXTRA_RESULT_OPEN = 4;
	public static final int EXTRA_RESULT_DELETE = 5;
	
	private static final int MENU_DELETE = 1;
	
	private Button mUpdateButton;
	private Button mImportButton;
	private Button mDownloadButton;
	private Button mOpenButton;
	private Button mCloseButton;
	
	private ImageButton mFavoriteButton;
	
	
	private TextView mCityTextView;
	private TextView mCountryTextView;
	
	private Intent mIntent;
	
	private String mSystemName;
	private String mLocalUrl;
	private String mRemoteUrl;
	private int mState;
	
	private CatalogMap mLocal;
	private CatalogMap mRemote;
	
	private TextStripView mContent;
	
	private String mCityName;
	private String mCountryName;
	//private int[] mTransports;
	
	private boolean mIsFavorite;
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,MENU_DELETE, 0, R.string.btn_delete);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_DELETE).setVisible(mLocalUrl!=null);
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case MENU_DELETE:
			finishWithResult(EXTRA_RESULT_DELETE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mIntent = getIntent();
        if(mIntent == null){
        	finishWithoutResult();
        	return;
        }
        mSystemName = mIntent.getStringExtra(EXTRA_SYSTEM_NAME);
        mLocalUrl = mIntent.getStringExtra(EXTRA_LOCAL_MAP_URL);        
        mRemoteUrl = mIntent.getStringExtra(EXTRA_REMOTE_MAP_URL);        
        mState = mIntent.getIntExtra(EXTRA_STATE, -1);        

        setContentView(R.layout.map_details);
        
        mUpdateButton = (Button)findViewById(R.id.btn_update);
        mImportButton = (Button)findViewById(R.id.btn_import);
        mDownloadButton = (Button)findViewById(R.id.btn_download);
        mOpenButton = (Button)findViewById(R.id.btn_open);
        mCloseButton = (Button)findViewById(R.id.btn_close);
        
        mFavoriteButton = (ImageButton)findViewById(R.id.btn_favorite);

        mCityTextView = (TextView)findViewById(R.id.firstLine);
        mCountryTextView = (TextView)findViewById(R.id.secondLine);
        
        mContent = (TextStripView)findViewById(R.id.content);
        
        mUpdateButton.setOnClickListener(this);
        mImportButton.setOnClickListener(this);
        mDownloadButton.setOnClickListener(this);
        mOpenButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mFavoriteButton.setOnClickListener(this);
        
        mUpdateButton.setVisibility(mState == CatalogMapState.UPDATE ? View.VISIBLE : View.GONE);
        mImportButton.setVisibility(mState == CatalogMapState.IMPORT ? View.VISIBLE : View.GONE);
        mDownloadButton.setVisibility(mState == CatalogMapState.DOWNLOAD ? View.VISIBLE : View.GONE);
        mOpenButton.setVisibility(mLocalUrl!=null ? View.VISIBLE : View.GONE);
        
        final CatalogStorage storage = CatalogStorage.getStorage();
        
        if(mLocalUrl!=null){
        	Catalog catalog = storage.getLocalCatalog();
        	if(catalog!=null){
        		mLocal = catalog.getMap(mSystemName);
        	}
        }

        if(mRemoteUrl!=null && mRemoteUrl.endsWith(GlobalSettings.PMZ_FILE_TYPE)){
        	Catalog catalog = storage.getImportCatalog();
        	if(catalog!=null){
        		mRemote = catalog.getMap(mSystemName);
        	}
        }
        
        if(mRemoteUrl!=null && mRemoteUrl.endsWith(GlobalSettings.MAP_FILE_TYPE)){
        	Catalog catalog = storage.getOnlineCatalog();
        	if(catalog!=null){
        		mRemote = catalog.getMap(mSystemName);
        	}
        }
        
    	String code = GlobalSettings.getLanguage();
    	mCityName = preffered().getCity(code);
    	mCountryName = preffered().getCountry(code);
        
        updateFavoriteButton();
        updateContent();
    }

	private CatalogMap preffered(){
		return mLocal!=null ? mLocal : mRemote;
	}
	
	public void onClick(View v) {
		if(v == mCloseButton){
        	finishWithoutResult();
		}else if(v == mOpenButton){
        	finishWithResult(EXTRA_RESULT_OPEN);
		}else if(v == mImportButton){
        	finishWithResult(EXTRA_RESULT_IMPORT);
		}else if(v == mDownloadButton){
        	finishWithResult(EXTRA_RESULT_DOWNLOAD);
		}else if(v == mUpdateButton){
			finishWithResult(EXTRA_RESULT_UPDATE);
		}else if(v == mFavoriteButton){
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

	private void updateContent() {
		mCityTextView.setText(mCityName);
		mCountryTextView.setText(mCountryName);

		mContent.removeAllViews();
		
		Button b = new Button(this);
		b.setText("press me!");
		
		mContent.addHeader("Button block");
		mContent.addWidgetBlock(b);
		
		for(int i = 0; i < 3; i++){
			mContent.addHeader("header " + i);
			for(int j = 0; j < 4; j++){
				mContent.addText("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
			}
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

	
}
