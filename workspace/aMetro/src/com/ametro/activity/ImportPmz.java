package com.ametro.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

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



		public void setSeverity(int mSeverity) {
			this.mSeverity = mSeverity;
		}

		public void setStatus(String mStatus) {
			this.mStatus = mStatus;
		}

		public void setStatusColor(int mStatusColor) {
			this.mStatusColor = mStatusColor;
		}

		@Override
		public int compareTo(ImportRecord another) {
			final int x = another.mSeverity - mSeverity; 
			return x != 0 ? x : this.mMapName.compareTo(another.mMapName);
		}

		public boolean isCheckable() {
			return mSeverity>0;
		}

	}

	private static class ProgressInfo
	{
		public final int Progress;
		public final int Maximum;
		public final String Message;
		public final String Title;

		public ProgressInfo(int progress, int maximum, String message, String title) {
			super();
			this.Progress = progress;
			this.Maximum = maximum;
			this.Message = message;
			this.Title = title;
		}

		public static void ChangeProgress(ProgressInfo pi, ProgressBar bar, TextView title, TextView msg){
			if(bar == null || pi == null) return;
			bar.setMax(pi.Maximum);
			bar.setProgress(pi.Progress);
			if(pi.Message!=null && msg!=null){
				msg.setText(pi.Message);
			}
			if(pi.Title!=null && title!=null){
				title.setText(pi.Title);
			}
		}
	}

	private class ImportListAdapter extends ArrayAdapter<ImportRecord> implements OnClickListener
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
			holder.mCheckbox .setVisibility(data.isCheckable() ? View.VISIBLE : View.INVISIBLE );
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
				ImportPmz.this.updateMenuStatus(null);
			}
		}

		public List<ImportRecord> getData(){
			return mData;
		}

		public List<ImportRecord> getCheckedData(){
			ArrayList<ImportRecord> lst = new ArrayList<ImportRecord>();
			for (Iterator<ImportRecord> iterator = mAdapter.getData().iterator(); iterator.hasNext();) {
				ImportRecord record = iterator.next();
				if(record.isChecked()){
					lst.add(record);
				}
			}			
			return lst;
		}

		public List<ImportRecord> getInvalidData() {
			ArrayList<ImportRecord> lst = new ArrayList<ImportRecord>();
			for (Iterator<ImportRecord> iterator = mAdapter.getData().iterator(); iterator.hasNext();) {
				ImportRecord record = iterator.next();
				final int severity = record.getSeverity();
				if(severity == 2 || severity == 0){
					lst.add(record);
				}
			}			
			return lst;
		}

		public List<ImportRecord> getObsoleteData() {
			ArrayList<ImportRecord> lst = new ArrayList<ImportRecord>();
			for (Iterator<ImportRecord> iterator = mAdapter.getData().iterator(); iterator.hasNext();) {
				ImportRecord record = iterator.next();
				final int severity = record.getSeverity();
				if(severity == 3 || severity == 1){
					lst.add(record);
				}
			}			
			return lst;
		}
		
		public void setCheckAll() {
			for (Iterator<ImportRecord> iterator = mData.iterator(); iterator.hasNext();) {
				ImportRecord record = iterator.next();
				if(record.isCheckable()){
					record.setChecked(true);
				}
			}		
		}

		public void setCheckNone() {
			for (Iterator<ImportRecord> iterator = mData.iterator(); iterator.hasNext();) {
				iterator.next().setChecked(false);
			}
		}


	}

	private class IndexTask extends AsyncTask<Void, ProgressInfo, List<ImportRecord>>
	{
		private boolean mIsCanceled = false;

		private void indexPmzFile(ArrayList<ImportRecord> imports, String fileName) {
			String mapFileName = MapSettings.getMapFileName(fileName.replace(MapSettings.PMZ_FILE_TYPE, ""));
			File mapFile = new File( mapFileName );
			String fullFileName = MapSettings.IMPORT_PATH + fileName;
			try {
				ModelDescription pmz = MapBuilder.indexPmz(fullFileName);
				String mapName = String.format("%s - %s (%s)" , pmz.getCountryName(), pmz.getCityName(), fileName );
				int severity = 5;
				int statusId = R.string.import_status_not_imported;
				int color = Color.RED;
				String statusText = ImportPmz.this.getString(statusId);
				if(mapFile.exists()){
					ModelDescription map = null;
					try{
						map = MapBuilder.loadModelDescription(mapFileName);
					}catch(Exception ex){
						map = null;
					}
					if(map != null && map.getSourceVersion() == MapSettings.getSourceVersion()){
						if( map.locationEqual(pmz) ){
							if( map.completeEqual(pmz) ){
								statusId = R.string.import_status_uptodate;
								statusText =  ImportPmz.this.getString(statusId);
								color = Color.GREEN;
								severity = 1;
							}else{
								if(map.getTimestamp() > pmz.getTimestamp()){
									statusId = R.string.import_status_old_version;
									statusText =  ImportPmz.this.getString(statusId);
									color = Color.CYAN;
									severity = 3;
								}else{ 
									statusId = R.string.import_status_deprecated;
									statusText =  ImportPmz.this.getString(statusId);
									color = Color.YELLOW;
									severity = 4;
								}
							}
						}else{
							statusId =  R.string.import_status_override;
							statusText = String.format( ImportPmz.this.getString(statusId), map.getCountryName(),map.getCityName() );
							color = Color.GRAY;
							severity = 2;
						}
					}
				}
				boolean checked = statusId == R.string.import_status_not_imported || statusId == R.string.import_status_deprecated;
				imports.add(new ImportRecord( severity, mapName, fullFileName, statusText, color, checked));
			}catch (Exception e) {
				Log.e("aMetro", "PMZ indexing error", (Throwable)e);
				imports.add(new ImportRecord(0, fileName, fullFileName, ImportPmz.this.getString(R.string.import_status_invalid), Color.RED, false ));
			} 
		}	

		@Override
		protected List<ImportRecord> doInBackground(Void... params) {
			File dir = new File(MapSettings.IMPORT_PATH);
			this.publishProgress(new ProgressInfo(0,100, null, "Search PMZ files..."));
			String[] files = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File f, String filename) {
					return filename.endsWith(MapSettings.PMZ_FILE_TYPE);
				}
			});
			ArrayList<ImportRecord> imports = new ArrayList<ImportRecord>();
			int count = files.length;
			this.publishProgress(new ProgressInfo(0,count, null, "Read PMZ files..."));
			if(files!=null){
				for(int i = 0; i < count && !mIsCanceled; i++){
					String fileName = files[i];
					this.publishProgress(new ProgressInfo(i,count, fileName, null));
					indexPmzFile(imports, fileName);
				}
			}
			Collections.sort(imports);	
			return imports;
		}

		@Override
		protected void onCancelled() {
			mIsCanceled = true;
			super.onCancelled();
		}

		@Override
		protected void onProgressUpdate(ProgressInfo... values) {
			ProgressInfo.ChangeProgress(values[0], mProgressBar, mProgressTitle, mProgressText);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPreExecute() {
			setupIndexMode();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(List<ImportRecord> result) {
			mFiles = result;
			setupSelectMode(result);
			super.onPostExecute(result);
		}

	}

	private class ImportTask extends AsyncTask<ImportRecord, ProgressInfo, List<ImportRecord>>
	{
		private boolean mIsCanceled = false;

		@Override
		protected List<ImportRecord> doInBackground(ImportRecord... imports) {
			ArrayList<ImportRecord> result = new ArrayList<ImportRecord>();
			final int count = imports.length;
			publishProgress(new ProgressInfo(0, imports.length, null, "Importing PMZ files..."));
			String updateStatus = getString(R.string.import_status_uptodate);
			for (int i = 0; i < count && !mIsCanceled; i++) {
				ImportRecord importRecord = imports[i];
				publishProgress(new ProgressInfo(i, imports.length,importRecord.getMapName(),null));
				try {
					Model map = MapBuilder.importPmz(importRecord.getFileName());
					MapBuilder.saveModel(map);
					importRecord.setChecked(false);
					importRecord.setStatus(updateStatus);
					importRecord.setStatusColor(Color.GREEN);
					importRecord.setSeverity(1);
					MapSettings.setRefreshOverride(ImportPmz.this, true);
				} catch (Throwable e) {
					Log.e("aMetro","Import failed",(Throwable)e);
					result.add(new ImportRecord(-1, importRecord.getMapName(), importRecord.getFileName(), "Import failed\n" + e.toString(), Color.RED, false));
				}
			} 
			Collections.sort(result);
			return result;
		}

		@Override
		protected void onCancelled() {
			mIsCanceled = true;
			super.onCancelled();
		}

		@Override
		protected void onProgressUpdate(ProgressInfo... values) {
			ProgressInfo.ChangeProgress(values[0], mProgressBar, mProgressTitle, mProgressText);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPreExecute() {
			setupImportMode();
			super.onPreExecute();
		}


		@Override
		protected void onPostExecute(List<ImportRecord> result) {
			if(result.size() == 0){
				setResult(RESULT_OK);
				finish();
			}
			setupReportMode(result);			
			super.onPostExecute(result);
		}


	}

	private enum Mode{
		Index,
		Select,
		Import,
		Report,
		Empty
	}
	
	private enum CleanupMode{
		Selected,
		Obsolete,
		Invalid,
		All
	}

	private Mode mMode;
	private CleanupMode mCleanupMode = CleanupMode.Obsolete;
	
	private ImportListAdapter mAdapter;
	private ListView mListView;

	private ProgressBar mProgressBar;
	private TextView mProgressTitle;
	private TextView mProgressText;

	private IndexTask mIndexTask;
	private ImportTask mImportTask;

	private List<ImportRecord> mFiles;

	private final int MAIN_MENU_REFRESH	= 1;
	private final int MAIN_MENU_IMPORT	= 2;
	private final int MAIN_MENU_CLEANUP = 3;
	private final int MAIN_MENU_SELECT_ALL	= 4;
	private final int MAIN_MENU_SELECT_NONE	= 5;
	private final int MAIN_MENU_STOP = 6;

	private MenuItem mMainMenuRefresh;
	private MenuItem mMainMenuImport;
	private MenuItem mMainMenuSelectAll;
	private MenuItem mMainMenuSelectNone;
	private MenuItem mMainMenuCleanup;
	private MenuItem mMainMenuStop;

	private boolean mMainMenuCreated = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		mMainMenuRefresh = menu.add(0, MAIN_MENU_REFRESH, 	0, R.string.menu_refresh).setIcon(android.R.drawable.ic_menu_rotate);
		mMainMenuImport = menu.add(0, MAIN_MENU_IMPORT, 	1, R.string.menu_import).setIcon(android.R.drawable.ic_menu_add);
		mMainMenuCleanup = menu.add(0, MAIN_MENU_CLEANUP, 	2, R.string.menu_cleanup).setIcon(android.R.drawable.ic_menu_delete);
		mMainMenuSelectAll = menu.add(0, MAIN_MENU_SELECT_ALL, 3, R.string.menu_select_all).setIcon(android.R.drawable.ic_menu_agenda);
		mMainMenuSelectNone = menu.add(0, MAIN_MENU_SELECT_NONE, 4, R.string.menu_select_none).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		mMainMenuStop = menu.add(0, MAIN_MENU_STOP, 5, R.string.menu_stop).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		mMainMenuCreated = true;
		updateMenuStatus(null);
		return true;
	}

	private void updateMenuStatus(Mode mode) {
		if(mode!=null){
			mMode = mode;
		}

		if(!mMainMenuCreated) return;

		mMainMenuRefresh.setVisible(mMode == Mode.Select || mMode == Mode.Empty);
		mMainMenuImport.setVisible(mMode == Mode.Select);
		mMainMenuSelectAll.setVisible(mMode == Mode.Select);
		mMainMenuSelectNone.setVisible(mMode == Mode.Select);
		mMainMenuCleanup.setVisible(mMode == Mode.Select || mMode == Mode.Report);
		mMainMenuStop.setVisible(mMode == Mode.Index || mMode == Mode.Import);

		if(mMode == Mode.Select){
			final List<ImportRecord> dataChecked = mAdapter.getCheckedData();
			final List<ImportRecord> data = mAdapter.getData();
			mMainMenuImport.setEnabled(dataChecked.size()>0);
			mMainMenuSelectAll.setEnabled(dataChecked.size() < data.size());
			mMainMenuSelectNone.setEnabled(dataChecked.size()>0);
		}else{
			mMainMenuImport.setEnabled(false);
			mMainMenuSelectAll.setEnabled(false);
			mMainMenuSelectNone.setEnabled(false);
		}
		mMainMenuRefresh.setEnabled(mMode == Mode.Select || mMode == Mode.Empty);
		mMainMenuCleanup.setEnabled(mMode == Mode.Select || mMode == Mode.Report);
		mMainMenuStop.setEnabled(mMode == Mode.Index || mMode == Mode.Import);
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_REFRESH:
			startIndexMode();
			return true;
		case MAIN_MENU_IMPORT:
			List<ImportRecord> checkedImports = mAdapter.getCheckedData();
			mImportTask = new ImportTask();
			mImportTask.execute((ImportRecord[]) checkedImports.toArray(new ImportRecord[checkedImports.size()]));
			return true;
		case MAIN_MENU_SELECT_ALL:
			mAdapter.setCheckAll();
			mListView.invalidateViews();
			updateMenuStatus(null);
			return true;
		case MAIN_MENU_SELECT_NONE:
			mAdapter.setCheckNone();
			mListView.invalidateViews();
			updateMenuStatus(null);
			return true;
		case MAIN_MENU_CLEANUP:
			if(mMode == Mode.Select){
				requestCleanupSelectMode();
			}
			if(mMode == Mode.Report){
				requestCleanupReportMode();
			}
			return true;
		case MAIN_MENU_STOP:
			if(mMode == Mode.Index){
				cancelIndexMode();
			}
			if(mMode == Mode.Import){
				cancelImportMode();				
			}
		}		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapSettings.checkPrerequisite(this);
		startIndexMode();
	}

	@Override
	protected void onStop() {
		if(mImportTask!=null && mImportTask.getStatus() != Status.FINISHED){
			mImportTask.cancel(false);
		}
		if(mIndexTask!=null && mIndexTask.getStatus() != Status.FINISHED){
			mIndexTask.cancel(false);
		}
		super.onStop();
	}

	private void startIndexMode() {
		mIndexTask = new IndexTask();
		mIndexTask.execute();
	}

	private void requestCleanupSelectMode() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		builder.setTitle(R.string.menu_cleanup); 
		builder.setSingleChoiceItems(R.array.import_pmz_cleanup_items, mCleanupMode.ordinal() , new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String itemName = getResources().getStringArray(R.array.import_pmz_cleanup_items)[which];
				mCleanupMode = CleanupMode.valueOf( itemName );
			}
		});        
		builder.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cleanupSelectMode();
			}
		}); 
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show(); 	
	}

	private void requestCleanupReportMode() {
	}	
	
	private void cleanupSelectMode(){
		List<ImportRecord> list = null;
		switch(mCleanupMode){
		case All:
			list = mAdapter.getData();
			break;
		case Invalid:
			list = mAdapter.getInvalidData();
			break;
		case Selected:
			list = mAdapter.getCheckedData();
			break;
		case Obsolete:
			list = mAdapter.getObsoleteData();
			break;
		}
		if(list!=null){
			for (Iterator<ImportRecord> records = list.iterator(); records.hasNext();) {
				ImportRecord record = records.next();
				File file = new File(record.getFileName());
				file.delete();
			}
		}
		startIndexMode();
	}

	private void cancelIndexMode() {
		mIndexTask.cancel(false);
		mIndexTask = null;
		setResult(RESULT_CANCELED);
		finish();
	}		

	private void cancelImportMode() {
		mImportTask.cancel(false);
		mImportTask = null;
		setupSelectMode( mFiles );
	}

	private void setupSelectMode(List<ImportRecord> result){
		if(result.size()>0){
			setContentView(R.layout.import_pmz_main);
			setTitle(R.string.import_title_confirm);
			mAdapter = new ImportListAdapter(ImportPmz.this, 0, result);
			mListView = (ListView)findViewById(R.id.import_pmz_list);
			mListView.setAdapter(mAdapter);
			updateMenuStatus(Mode.Select);
		}else{
			setContentView(R.layout.import_pmz_empty);
			setTitle(R.string.import_title_empty);
			updateMenuStatus(Mode.Empty);
		}
	}

	private void setupImportMode() {
		setContentView(R.layout.import_pmz_progress);
		setTitle(R.string.import_title_importing);
		mProgressBar  = (ProgressBar) findViewById(R.id.import_pmz_progress_bar);
		mProgressTitle = (TextView)findViewById(R.id.import_pmz_progress_title);
		mProgressText = (TextView)findViewById(R.id.import_pmz_progress_text);
		updateMenuStatus(Mode.Import);
	}

	private void setupIndexMode() {
		setContentView(R.layout.import_pmz_progress);
		setTitle(R.string.import_title_indexing);
		mProgressBar  = (ProgressBar) findViewById(R.id.import_pmz_progress_bar);
		mProgressTitle = (TextView)findViewById(R.id.import_pmz_progress_title);
		mProgressText = (TextView)findViewById(R.id.import_pmz_progress_text);
		updateMenuStatus(Mode.Index);
	}

	private void setupReportMode(List<ImportRecord> result) {
		setContentView(R.layout.import_pmz_main);
		setTitle(R.string.import_title_report);
		mAdapter = new ImportListAdapter(ImportPmz.this, 0, result);
		mListView = (ListView)findViewById(R.id.import_pmz_list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ImportPmz.this.setResult(RESULT_OK);
				ImportPmz.this.finish();

			}
		});
		updateMenuStatus(Mode.Report);
	}


}
