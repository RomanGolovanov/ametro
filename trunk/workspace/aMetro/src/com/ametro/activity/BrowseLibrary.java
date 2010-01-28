package com.ametro.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.ametro.MapSettings;
import com.ametro.MapUri;
import com.ametro.R;
import com.ametro.libs.FileGroupsDictionary;
import com.ametro.model.MapBuilder;
import com.ametro.model.ModelDescription;

public class BrowseLibrary extends Activity implements ExpandableListView.OnChildClickListener {

	public static class MapListAdapter extends BaseExpandableListAdapter {

		private String[] mCountries;
		private String[][] mCities; 
		private String[][] mFiles;

		private Context mContext;
		private int mSelectedGroup;
		private int mSelectedChild;

		public String getFileName(int groupId, int childId){
			return mFiles[groupId][childId];
		}

		public MapListAdapter(Context context){
			mContext = context;
		}

		private static void writeMaps(FileGroupsDictionary data) {
			String fileName = MapSettings.MAPS_PATH + MapSettings.MAPS_LIST;
			ObjectOutputStream strm = null;
			try{
				strm = new ObjectOutputStream(new FileOutputStream(fileName));
				strm.writeObject(data);
				strm.flush();
			}catch(Exception ex){
				Log.e("aMetro", "Failed write map cache", ex);
			}finally{
				if(strm!=null){
					try {
						strm.close();
					} catch (IOException e) {
					}
				}
			}
		}

		private static FileGroupsDictionary readMaps()
		{
			String fileName = MapSettings.MAPS_PATH + MapSettings.MAPS_LIST;
			ObjectInputStream strm = null;
			try{
				try {
					strm = new ObjectInputStream(new FileInputStream(fileName));
					FileGroupsDictionary map = (FileGroupsDictionary) strm.readObject();
					Log.i("aMetro", "Loaded map cache");
					return map;
				} catch (Exception ex) {
					Log.i("aMetro", "Cannot load map cache");
					return null;
				}
			} finally{
				if(strm!=null){
					try {
						strm.close();
					} catch (IOException e) {
					}
				}
			}

		}
		
		public void init(String path, boolean refresh){
			FileGroupsDictionary map = null;
			if(!refresh) map = readMaps();
			File dir = new File(path);
			if(map == null || map.getTimestamp() < dir.lastModified() ){
				map = scanDirectory(path);
				writeMaps(map);
			}
			
			mCountries = map.getGroups();
			mCities = new String[mCountries.length][];
			mFiles = new String[mCountries.length][];
			for (int i = 0; i < mCountries.length; i++) {
				String country = mCountries[i];
				mCities[i] = map.getLabels(country);
				mFiles[i] = map.getPathes(country, mCities[i]);
			}

		}

		private FileGroupsDictionary scanDirectory(String path) {
			File dir = new File(path);
			FileGroupsDictionary map = new FileGroupsDictionary();
			map.setTimestamp( (new Date()).getTime() );
			
			String[] files = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File f, String filename) {
					return filename.endsWith(MapSettings.MAP_FILE_TYPE);
				}
			}); 

			if(files!=null){
				for(int i = 0; i < files.length; i++){
					String fileName = files[i];
					String fullFileName = path +'/' + files[i];
					//scanPmzFileContent(map, fileName, fullFileName);
					scanModelFileContent(map, fileName, fullFileName);
				}
			}
			return map;
		}

		private void scanModelFileContent(FileGroupsDictionary map, String fileName, String fullFileName) {
			try {
				ModelDescription modelDescription = MapBuilder.loadModelDescription(fullFileName);
				map.putFile(modelDescription.getCountryName(), modelDescription.getCityName(), fileName);
			} catch (Exception e) {
				Log.d("aMetro", "Map indexing failed for " + fileName, (Throwable)e);
				// skip this file
			} 
		
		}
		
		public Object getChild(int groupPosition, int childPosition) {
			return mCities[groupPosition][childPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return mCities[groupPosition].length;
		}

		public TextView getGenericView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);

			TextView textView = new TextView(mContext);
			textView.setLayoutParams(lp);
			// Center the text vertically
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			// Set the text starting position
			textView.setPadding(36, 0, 0, 0);
			return textView;
		}

		public TextView getSelectedView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);

			TextView textView = new TextView(mContext);
			textView.setLayoutParams(lp);
			textView.setBackgroundColor(Color.DKGRAY);

			// Center the text vertically
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			// Set the text starting position
			textView.setPadding(36, 0, 0, 0);
			return textView;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = groupPosition==mSelectedGroup && childPosition==mSelectedChild ?
					getSelectedView() :
						getGenericView() ;
					textView.setText(getChild(groupPosition, childPosition).toString());
					return textView;
		}

		public Object getGroup(int groupPosition) {
			return mCountries[groupPosition];
		}

		public int getGroupCount() {
			return mCountries.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getGroup(groupPosition).toString());
			return textView;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public boolean hasStableIds() {
			return true;
		}


		public int getSelectedGroupPosition() {
			return mSelectedGroup;
		}

		public int getSelectChildPosition() {
			return mSelectedChild;
		}

		public void setSelectedFile(String fileName){
			for(int i = 0; i < mFiles.length; i++){
				String[] cols = mFiles[i];
				for(int j = 0; j < cols.length; j++){
					if( cols[j].equals(fileName) ){
						mSelectedGroup = i;
						mSelectedChild = j;
						return;
					}
				}
			}
			mSelectedGroup = -1;
			mSelectedChild = -1;
		}

	}

	private MapListAdapter mAdapter;
	private ExpandableListView mListView;
	private String mDefaultPackageFileName;

	private final int MAIN_MENU_REFRESH		= 1;
	private final int MAIN_MENU_ALL_MAPS	= 2;
	private final int MAIN_MENU_MY_MAPS		= 3;
	private final int MAIN_MENU_LOCATION	= 4;
	private final int MAIN_MENU_IMPORT		= 5;
	
	private MenuItem mMainMenuAllMaps;
	private MenuItem mMainMenuMyMaps;
	
	private final static int REQUEST_IMPORT = 1;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_REFRESH, 	0, R.string.menu_refresh).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, MAIN_MENU_LOCATION, 3, R.string.menu_location).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, MAIN_MENU_IMPORT, 	4, R.string.menu_import).setIcon(android.R.drawable.ic_menu_add);

		mMainMenuAllMaps = menu.add(0, MAIN_MENU_ALL_MAPS, 	1, R.string.menu_all_maps ).setIcon(android.R.drawable.ic_menu_mapmode).setVisible(false);
		mMainMenuMyMaps = menu.add(0, MAIN_MENU_MY_MAPS, 	2, R.string.menu_my_maps).setIcon(android.R.drawable.ic_menu_myplaces);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_REFRESH:
			initializeControls(true);
			return true;
		case MAIN_MENU_ALL_MAPS:
			mMainMenuAllMaps.setVisible(false);
			mMainMenuMyMaps.setVisible(true);
			return true;
		case MAIN_MENU_MY_MAPS:
			mMainMenuAllMaps.setVisible(true);
			mMainMenuMyMaps.setVisible(false);
			return true;
		case MAIN_MENU_LOCATION:
			return true;
		case MAIN_MENU_IMPORT:
			startActivityForResult(new Intent(this,ImportPmz.class), REQUEST_IMPORT);
			return true;

		}		
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_IMPORT:
			if(resultCode == RESULT_OK){
				initializeControls(true);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeControls(false);
	}

	private void initializeControls(final boolean refresh) {
		setContentView(R.layout.waiting);
		mAdapter = new MapListAdapter(this);
		mDefaultPackageFileName = null;
		Intent intent = getIntent();
		Uri uri = intent!= null ? intent.getData() : null;
		if(uri!=null){
			mDefaultPackageFileName = MapUri.getMapName(uri) + MapSettings.MAP_FILE_TYPE;
		}
		mMapListLoader = new Thread() {
			public void run() {
				mAdapter.init(MapSettings.MAPS_PATH, refresh);
				mHandler.post(mUpdateContentView);
			}
		};
		mMapListLoader.start();
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		String fileName = mAdapter.getFileName(groupPosition, childPosition);
		String mapName = fileName.replace(".ametro", "");
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
					if(groupPosition!=-1){ 
						mListView.expandGroup(groupPosition);
						if(childPosition!=-1){
							mListView.setSelectedChild(groupPosition, childPosition, true);
						}
					}
				}

				setContentView(mListView);
				registerForContextMenu(mListView);
			}else{
				setContentView(R.layout.no_map_loaded);
			}
		}
	};

	private Thread mMapListLoader;


}
