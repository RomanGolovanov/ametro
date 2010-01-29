package com.ametro.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ametro.MapSettings;
import com.ametro.R;
import com.ametro.model.MapBuilder;
import com.ametro.model.Model;
import com.ametro.model.ModelDescription;

public class ImportPmz extends Activity {

	private static class ImportRecord implements Comparable<ImportRecord>{

		private int mSeverity;
		private String mMapName;
		private String mFileName;
		private String mStatus;
		private int mStatusColor;
		private boolean mChecked;

		public ImportRecord(int severity, String mapName, String fileName, String status, int statusColor, boolean checked) {
			super();
			this.mSeverity = severity;
			this.mMapName = mapName;
			this.mFileName = fileName;
			this.mStatus = status;
			this.mStatusColor = statusColor;
			this.mChecked = checked;
		}

		public boolean isChecked() {
			return mChecked;
		}

		public void setChecked(boolean checked) {
			this.mChecked = checked;
		}

		public String getMapName() {
			return mMapName;
		}

		public String getFileName() {
			return mFileName;
		}

		public String getStatus() {
			return mStatus;
		}

		public int getColor(){
			return mStatusColor;
		}

		public int getSeverity(){
			return mSeverity;
		}

		@Override
		public int compareTo(ImportRecord another) {
			final int x = another.mSeverity - mSeverity; 
			return x != 0 ? x : this.mMapName.compareTo(another.mMapName);
		}

	}

	private class ImportListAdapter extends ArrayAdapter<ImportRecord> 
	implements OnClickListener
	{

		private LayoutInflater mInflater;
		private List<ImportRecord> mData;

		public ImportListAdapter (Context context, int textViewResourceId, List<ImportRecord> data) {
			super(context, textViewResourceId, data);
			mData = data;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public class ViewHolder {
			TextView mText;
			TextView mStatus;
			CheckBox mCheckbox;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if ( convertView == null ) {
				convertView = mInflater.inflate(R.layout.import_pmz_row, null);
				convertView.setClickable(true);
				convertView.setOnClickListener(this);
				convertView.setFocusable(true);

				holder = new ViewHolder();
				holder.mText = (TextView) convertView.findViewById(R.id.import_row_text);
				holder.mStatus = (TextView) convertView.findViewById(R.id.import_row_status);
				holder.mCheckbox = (CheckBox) convertView.findViewById(R.id.import_row_checkbox);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final ImportRecord data = mData.get(position);
			holder.mText.setText(data.getMapName());
			holder.mStatus.setText(data.getStatus());
			holder.mStatus.setTextColor(data.getColor());
			holder.mCheckbox.setTag(data);
			holder.mCheckbox.setChecked(data.isChecked());
			holder.mCheckbox.setClickable(false);
			holder.mCheckbox .setVisibility(data.getSeverity()>0 ? View.VISIBLE : View.INVISIBLE );
			return convertView;
		}

		@Override
		public void onClick(View v) {
			final ViewHolder holder = (ViewHolder)v.getTag();
			final CheckBox checkbox = holder.mCheckbox;
			ImportRecord data = (ImportRecord) checkbox.getTag();
			if(data.getSeverity()>0){
				data.setChecked(!data.isChecked());
				checkbox.setChecked(data.isChecked());
				ImportPmz.this.updateMenuStatus();
			}
		}

		public List<ImportRecord> getData(){
			return mData;
		}

		public List<ImportRecord> getCheckedData(){
			ArrayList<ImportRecord> lst = new ArrayList<ImportRecord>();
			for (Iterator<ImportRecord> iterator = mAdapter.getData().iterator(); iterator.hasNext();) {
				ImportRecord importRecord = iterator.next();
				if(importRecord.isChecked()){
					lst.add(importRecord);
				}
			}			
			return lst;
		}


	}

	private final Handler mHandler = new Handler();
	private Throwable mFailReason = null;

	private ImportListAdapter mAdapter;
	private ListView mListView;
	private boolean mPrepared;
	private TextView mProgressTitle;
	private TextView mProgressText;

	private final int MAIN_MENU_IMPORT	= 1;
	private final int MAIN_MENU_SELECT_ALL	= 2;
	private final int MAIN_MENU_SELECT_NONE	= 3;

	private MenuItem mMainMenuImport;
	private MenuItem mMainMenuSelectAll;
	private MenuItem mMainMenuSelectNone;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		mMainMenuImport = menu.add(0, MAIN_MENU_IMPORT, 	0, R.string.menu_import).setIcon(android.R.drawable.ic_menu_add);
		mMainMenuSelectAll = menu.add(0, MAIN_MENU_SELECT_ALL, 1, R.string.menu_select_all).setIcon(android.R.drawable.ic_menu_more);
		mMainMenuSelectNone = menu.add(0, MAIN_MENU_SELECT_NONE, 1, R.string.menu_select_none).setIcon(android.R.drawable.ic_menu_revert);

		updateMenuStatus();


		return true;
	}

	private void updateMenuStatus() {
		if(mMainMenuImport == null) return;
		if(mPrepared){
			final List<ImportRecord> dataChecked = mAdapter.getCheckedData();
			final List<ImportRecord> data = mAdapter.getCheckedData();
			mMainMenuImport.setEnabled(dataChecked.size()>0);
			mMainMenuSelectAll.setEnabled(dataChecked.size() < data.size());
			mMainMenuSelectNone.setEnabled(dataChecked.size()>0);
			return;
		}
		mMainMenuImport.setEnabled(false);
		mMainMenuSelectAll.setEnabled(false);
		mMainMenuSelectNone.setEnabled(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_IMPORT:
			setContentView(R.layout.import_pmz);
			mProgressTitle = (TextView)findViewById(R.id.import_pmz_progress_title);
			mProgressText = (TextView)findViewById(R.id.import_pmz_progress_text);
			mImport.start();
			return true;
		case MAIN_MENU_SELECT_ALL:
			for (Iterator<ImportRecord> iterator = mAdapter.getCheckedData().iterator(); iterator.hasNext();) {
				ImportRecord importRecord = iterator.next();
				importRecord.setChecked(true);
			}
			mListView.invalidateViews();
			mMainMenuSelectAll.setEnabled(false);
			mMainMenuSelectNone.setEnabled(true);
			return true;
		case MAIN_MENU_SELECT_NONE:
			for (Iterator<ImportRecord> iterator = mAdapter.getCheckedData().iterator(); iterator.hasNext();) {
				ImportRecord importRecord = iterator.next();
				importRecord.setChecked(false);
				
			}
			mListView.invalidateViews();
			mMainMenuSelectAll.setEnabled(true);
			mMainMenuSelectNone.setEnabled(false);
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapSettings.checkPrerequisite(this);
		mPrepared = false;
		setContentView(R.layout.waiting);
		mIndex.start();
	}

	private final Runnable mReturnOk = new Runnable() {
		public void run() {
			setResult(RESULT_OK, null);
			finish();
		}
	};

	private final Runnable mHandleException = new Runnable() {
		public void run() {
			Toast.makeText(ImportPmz.this, "Import failed: " + mFailReason.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED, null);
			finish();
		}
	};

	private int mImportCount;
	private int mImportPosition;
	private String mImportMapName;

	private final Runnable mHandleImportUpdateProgress = new Runnable() {
		public void run() {
			mProgressTitle.setText(
					String.format("Importing map %s/%s", Integer.toString(mImportPosition), Integer.toString(mImportCount))
			);
			mProgressText.setText(mImportMapName);
		}
	};

	private final Runnable mHandleIndexed = new Runnable() {
		public void run() {
			mListView = new ListView(ImportPmz.this);
			mListView.setAdapter(mAdapter);
			setContentView(mListView);
			mPrepared = true;
		}
	};	

	private final Thread mIndex = new Thread() {
		public void run() {
			try {
				File dir = new File(MapSettings.IMPORT_PATH);
				String[] files = dir.list(new FilenameFilter() {
					@Override
					public boolean accept(File f, String filename) {
						return filename.endsWith(MapSettings.PMZ_FILE_TYPE);
					}
				});
				ArrayList<ImportRecord> imports = new ArrayList<ImportRecord>();
				if(files!=null){
					for(int i = 0; i < files.length; i++){
						String fileName = files[i];
						indexPmzFile(imports, fileName);
					}
				}	
				Collections.sort(imports);
				mAdapter = new ImportListAdapter(ImportPmz.this, 0, imports);
				mHandler.post(mHandleIndexed);

			} catch (Exception e) {
				Log.e("aMetro","Failed import map", e);
				mFailReason = e;
				mHandler.post(mHandleException);
			}			
		}

		private void indexPmzFile(ArrayList<ImportRecord> imports, String fileName) {
			String mapFileName = MapSettings.getMapFileName(fileName.replace(MapSettings.PMZ_FILE_TYPE, ""));
			File mapFile = new File( mapFileName );
			File importFile = new File(fileName);
			String fullFileName = MapSettings.IMPORT_PATH + fileName;
			try {
				ModelDescription importDescription = MapBuilder.indexPmz(fullFileName);
				String mapName = String.format("%s - %s" , importDescription.getCountryName() , importDescription.getCityName() );
				int severity = 4;
				int statusId = R.string.import_status_not_imported;
				int color = Color.RED;
				if(mapFile.exists()){
					ModelDescription modelDescription = MapBuilder.loadModelDescription(mapFileName);
					if( modelDescription.equals(importDescription) ){
						if( importFile.lastModified() > mapFile.lastModified() ){
							statusId = R.string.import_status_deprecated;
							color = Color.YELLOW;
							severity = 3;
						}else{
							statusId = R.string.import_status_uptodate;
							color = Color.GREEN;
							severity = 1;
						}
					}else{
						statusId = R.string.import_status_override;
						color = Color.GRAY;
						severity = 2;
					}
				}

				imports.add(new ImportRecord(
						severity,
						mapName, 
						fullFileName, 
						ImportPmz.this.getString(statusId),
						color,
						statusId == R.string.import_status_not_imported ));

			} catch (Exception e) {
				Log.e("aMetro", "PMZ indexing error", (Throwable)e);
				imports.add(new ImportRecord(
						0,
						fileName, 
						fullFileName, 
						ImportPmz.this.getString(R.string.import_status_invalid),
						Color.RED,
						false ));
			} 
		}
	};

	private final Thread mImport = new Thread() {
		public void run() {
			List<ImportRecord> imports = mAdapter.getCheckedData();
			mImportCount = imports.size();
			mImportPosition = 0;
			for (Iterator<ImportRecord> iterator = imports.iterator(); iterator.hasNext();) {
				ImportRecord importRecord = iterator.next();
				mImportPosition++;
				mImportMapName = importRecord.getMapName();
				mHandler.post(mHandleImportUpdateProgress);
				try {
					Model map = MapBuilder.importPmz(importRecord.getFileName());
					MapBuilder.saveModel(map);
				} catch (IOException e) {
					Log.e("aMetro","Failed import map", e);
					mFailReason = e;
					mHandler.post(mHandleException);
				}

			}

			mHandler.post(mReturnOk);
		}
	};

}
