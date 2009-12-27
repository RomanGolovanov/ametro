package com.ametro;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;

import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class BrowseLibrary extends Activity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */

	MapListAdapter mAdapter;
	ExpandableListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new MapListAdapter(this);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.waiting);
		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		setProgressBarVisibility(true);

		mMapListLoader.start();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		String title = ((TextView) info.targetView).getText().toString();

		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Toast.makeText(
					this,
					title + ": Child " + childPos + " clicked in group "
					+ groupPos, Toast.LENGTH_SHORT).show();
			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView
			.getPackedPositionGroup(info.packedPosition);
			Toast.makeText(this, title + ": Group " + groupPos + " clicked",
					Toast.LENGTH_SHORT).show();
			return true;
		}

		return false;
	}


	private final Handler mHandler = new Handler();

	private final Runnable mUpdateContentView = new Runnable() {
		public void run() {
			setProgressBarIndeterminateVisibility(false);
			//setListAdapter(mAdapter);
			//registerForContextMenu(getExpandableListView());
			mListView = new ExpandableListView(BrowseLibrary.this);
			mListView.setAdapter(mAdapter);
			setContentView(mListView);
			registerForContextMenu(mListView);
		}
	};

	private final Thread mMapListLoader = new Thread() {
		public void run() {
			mAdapter.Initialize("/sdcard/ametro");
			mHandler.post(mUpdateContentView);
		}
	};
}
