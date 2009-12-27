package com.ametro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;

import android.widget.ExpandableListView;

public class BrowseLibrary extends Activity implements ExpandableListView.OnChildClickListener {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */

	private MapListAdapter mAdapter;
	private ExpandableListView mListView;
	private String mDefaultPackageFileName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new MapListAdapter(this);
		mDefaultPackageFileName = null;

		Intent data = getIntent();
		if(data!=null){
			Uri uri = data.getData();
			if(uri!=null){
				mDefaultPackageFileName = uri.toString().replace("ametro://", "");
			}
		}

		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.waiting);
		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);

		mMapListLoader.start();
	}


	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		String fileName = mAdapter.getFileName(groupPosition, childPosition);
		Intent i = new Intent();
		i.setData( Uri.parse( "ametro://" + fileName ));
		setResult(RESULT_OK, i);
		finish();
		return true;
	}

	private final Handler mHandler = new Handler();

	private final Runnable mUpdateContentView = new Runnable() {
		public void run() {
			setProgressBarIndeterminateVisibility(false);
			mListView = new ExpandableListView(BrowseLibrary.this);
			mListView.setAdapter(mAdapter);
			mListView.setOnChildClickListener(BrowseLibrary.this);
			
			if(mDefaultPackageFileName!=null){
				int groupPosition = mAdapter.getGroupByFileName(mDefaultPackageFileName);
				int childPosition = mAdapter.getChildByFileName(groupPosition, mDefaultPackageFileName);
				Log.e("aMetro","Group: " + groupPosition + ", Child:" + childPosition);
				mListView.expandGroup(groupPosition);
				mListView.setSelectedChild(groupPosition, childPosition, true);
			}
			
			setContentView(mListView);
			registerForContextMenu(mListView);
		}
	};

	private final Thread mMapListLoader = new Thread() {
		public void run() {
			mAdapter.init(MapSettings.CATALOG_PATH);
			mHandler.post(mUpdateContentView);
		}
	};

}
