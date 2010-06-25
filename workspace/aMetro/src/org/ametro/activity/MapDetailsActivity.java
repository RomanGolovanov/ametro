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
import org.ametro.catalog.CatalogMapState;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
	
	private Button mUpdateButton;
	private Button mImportButton;
	private Button mDownloadButton;
	private Button mDeleteButton;
	private Button mOpenButton;
	private Button mCloseButton;
	
	private Intent mIntent;
	
	private String mSystemName;
	private String mLocalUrl;
	private String mRemoteUrl;
	private int mState;
	
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
        mDeleteButton = (Button)findViewById(R.id.btn_delete);
        mOpenButton = (Button)findViewById(R.id.btn_open);
        mCloseButton = (Button)findViewById(R.id.btn_close);

        mUpdateButton.setOnClickListener(this);
        mImportButton.setOnClickListener(this);
        mDownloadButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mOpenButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        
        mUpdateButton.setVisibility(mState == CatalogMapState.UPDATE ? View.VISIBLE : View.GONE);
        mImportButton.setVisibility(mState == CatalogMapState.IMPORT ? View.VISIBLE : View.GONE);
        mDownloadButton.setVisibility(mState == CatalogMapState.DOWNLOAD ? View.VISIBLE : View.GONE);
        mDeleteButton.setVisibility(mLocalUrl!=null ? View.VISIBLE : View.GONE);
        mOpenButton.setVisibility(mLocalUrl!=null ? View.VISIBLE : View.GONE);
        
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
		}else if(v == mDeleteButton){
        	finishWithResult(EXTRA_RESULT_DELETE);
		}else if(v == mUpdateButton){
			finishWithResult(EXTRA_RESULT_UPDATE);
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
