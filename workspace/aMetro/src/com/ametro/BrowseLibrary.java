package com.ametro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.Toast;

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
		Intent intent = getIntent();
		Uri uri = intent!= null ? intent.getData() : null;
		if(uri!=null){
			mDefaultPackageFileName = MapUri.getMapName(uri) + ".pmz";
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
		String mapName = fileName.replace(".pmz", "" );
		Intent i = new Intent();
		i.setData( MapUri.create(mapName));
		setResult(RESULT_OK, i);
		finish();
		return true;
	}

	private final Handler mHandler = new Handler();

	private final Runnable mUpdateContentView = new Runnable() {
		public void run() {
			if(mAdapter.getGroupCount()>0){
				setProgressBarIndeterminateVisibility(false);
				mListView = new ExpandableListView(BrowseLibrary.this);
				mListView.setAdapter(mAdapter);
				mListView.setOnChildClickListener(BrowseLibrary.this);
				if(mDefaultPackageFileName!=null){
					mAdapter.setSelectedFile(mDefaultPackageFileName);
					int groupPosition = mAdapter.getSelectedGroupPosition();
					int childPosition = mAdapter.getSelectChildPosition();
					mListView.expandGroup(groupPosition);
					mListView.setSelectedChild(groupPosition, childPosition, true);
				}

				setContentView(mListView);
				registerForContextMenu(mListView);
			}else{
				Toast.makeText(BrowseLibrary.this, "No maps in " + MapSettings.CATALOG_PATH, Toast.LENGTH_LONG).show();
				setResult(RESULT_CANCELED, null);
				finish();
			}
		}
	};

	private final Thread mMapListLoader = new Thread() {
		public void run() {
			mAdapter.init(MapSettings.CATALOG_PATH);
			mHandler.post(mUpdateContentView);
		}
	};


}
