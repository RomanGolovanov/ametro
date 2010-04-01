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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ametro.Constants;
import org.ametro.MapSettings;
import org.ametro.R;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.other.ProgressInfo;
import org.ametro.util.FileUtil;

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


public class ImportPmz extends Activity {

    private static class ImportRecord implements Comparable<ImportRecord> {

        private int severity;
        private String mapName;
        private String fileName;
        private String status;
        private int statusColor;
        private boolean checked;


        public ImportRecord(
                int newSeverity, String newMapName, String newFileName,
                String newStatus, int newStatusColor, boolean newChecked) {
            super();
            severity = newSeverity;
            mapName = newMapName;
            fileName = newFileName;
            status = newStatus;
            statusColor = newStatusColor;
            checked = newChecked;
        }

        public int compareTo(ImportRecord another) {
            final int x = another.severity - severity;
            return x != 0 ? x : this.mapName.compareTo(another.mapName);
        }

        public boolean isCheckable() {
            return severity > 0;
        }

    }

    private class ImportListAdapter extends ArrayAdapter<ImportRecord> implements OnClickListener {

        private LayoutInflater mInflater;
        private List<ImportRecord> mData;

        public ImportListAdapter(Context context, int textViewResourceId, List<ImportRecord> data) {
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
            ViewHolder holder;
            if (convertView == null) {
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
            holder.mText.setText(data.mapName);
            holder.mStatus.setText(data.status);
            holder.mStatus.setTextColor(data.statusColor);
            holder.mCheckbox.setTag(data);
            holder.mCheckbox.setChecked(data.checked);
            holder.mCheckbox.setClickable(false);
            holder.mCheckbox.setVisibility(data.isCheckable() ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }

        public void onClick(View v) {
            final ViewHolder holder = (ViewHolder) v.getTag();
            final CheckBox checkbox = holder.mCheckbox;
            ImportRecord data = (ImportRecord) checkbox.getTag();
            if (data.severity > 0) {
                data.checked = !data.checked;
                checkbox.setChecked(data.checked);
                ImportPmz.this.updateMenuStatus(null);
            }
        }

        public List<ImportRecord> getData() {
            return mData;
        }

        public List<ImportRecord> getCheckedData() {
            ArrayList<ImportRecord> lst = new ArrayList<ImportRecord>();
            for (ImportRecord record : mAdapter.getData()) {
                if (record.checked) {
                    lst.add(record);
                }
            }
            return lst;
        }

        public List<ImportRecord> getInvalidData() {
            ArrayList<ImportRecord> lst = new ArrayList<ImportRecord>();
            for (ImportRecord record : mAdapter.getData()) {
                final int severity = record.severity;
                if (severity == 2 || severity == 0) {
                    lst.add(record);
                }
            }
            return lst;
        }

        public List<ImportRecord> getObsoleteData() {
            ArrayList<ImportRecord> lst = new ArrayList<ImportRecord>();
            for (ImportRecord record : mAdapter.getData()) {
                final int severity = record.severity;
                if (severity == 3 || severity == 1) {
                    lst.add(record);
                }
            }
            return lst;
        }

        public void setCheckAll() {
            for (ImportRecord record : mData) {
                if (record.isCheckable()) {
                    record.checked = true;
                }
            }
        }

        public void setCheckNone() {
            for (ImportRecord record : mData) {
                record.checked = false;
            }
        }


    }

    private class IndexTask extends AsyncTask<Void, ProgressInfo, List<ImportRecord>> {
        private boolean mIsCanceled = false;

        private void indexPmzFile(ArrayList<ImportRecord> imports, String fileName) {
            String mapFileName = MapSettings.getMapFileName(fileName.replace(MapSettings.PMZ_FILE_TYPE, ""));
            File mapFile = new File(mapFileName);
            String fullFileName = MapSettings.IMPORT_PATH + fileName;
            try {
                Model pmz = ModelBuilder.loadModelDescription(fullFileName);
                String mapName = pmz.getCountryName() + " - " + pmz.getCityName() + "(" + fileName + ")";
                int severity = 5;
                int statusId = R.string.import_status_not_imported;
                int color = Color.RED;
                String statusText = ImportPmz.this.getString(statusId);
                if (mapFile.exists()) {
                    Model map;
                    try {
                        map = ModelBuilder.loadModelDescription(mapFileName);
                    } catch (Exception ex) {
                        map = null;
                    }
                    if (map != null) {
                        if (map.locationEqual(pmz)) {
                            if (map.completeEqual(pmz)) {
                                statusId = R.string.import_status_uptodate;
                                statusText = ImportPmz.this.getString(statusId);
                                color = Color.GREEN;
                                severity = 1;
                            } else {
                                if (map.timestamp > pmz.timestamp) {
                                    statusId = R.string.import_status_old_version;
                                    statusText = ImportPmz.this.getString(statusId);
                                    color = Color.CYAN;
                                    severity = 3;
                                } else {
                                    statusId = R.string.import_status_deprecated;
                                    statusText = ImportPmz.this.getString(statusId);
                                    color = Color.YELLOW;
                                    severity = 4;
                                }
                            }
                        } else {
                            statusId = R.string.import_status_override;
                            statusText = String.format(ImportPmz.this.getString(statusId), map.getCountryName(), map.getCityName());
                            color = Color.GRAY;
                            severity = 2;
                        }
                    }
                }
                boolean checked = statusId == R.string.import_status_not_imported || statusId == R.string.import_status_deprecated;
                imports.add(new ImportRecord(severity, mapName, fullFileName, statusText, color, checked));
            } catch (Exception e) {
            	if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
            		Log.e(Constants.LOG_TAG_MAIN, getString(R.string.log_pmz_indexing_error), e);
            	}
                imports.add(new ImportRecord(0, fileName, fullFileName, ImportPmz.this.getString(R.string.import_status_invalid), Color.RED, false));
            }
        }

        @Override
        protected List<ImportRecord> doInBackground(Void... params) {
            File dir = new File(MapSettings.IMPORT_PATH);
            ProgressInfo pi = new ProgressInfo(0, 0, null, getString(R.string.msg_search_pmz_files));
            this.publishProgress(pi);
            String[] files = dir.list(new FilenameFilter() {
                public boolean accept(File f, String filename) {
                    return filename.endsWith(MapSettings.PMZ_FILE_TYPE);
                }
            });
            ArrayList<ImportRecord> imports = new ArrayList<ImportRecord>();
            if (files != null) {
                int count = files.length;
                pi.title = getString(R.string.msg_read_pmz_files);
                pi.maximum = count;
                pi.progress = 0;
                this.publishProgress(pi);
                for (int i = 0; i < count && !mIsCanceled; i++) {
                    String fileName = files[i];
                    pi.progress = i;
                    pi.message = fileName;
                    this.publishProgress(pi);
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
            ProgressInfo.ChangeProgress(
                    values[0],
                    mProgressBar,
                    mProgressTitle,
                    mProgressText,
                    mProgressCounter,
                    getString(R.string.template_progress_count)
            );
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

    private class ImportTask extends AsyncTask<ImportRecord, ProgressInfo, List<ImportRecord>> {
        private boolean mIsCanceled = false;

        protected List<ImportRecord> doInBackground(ImportRecord... imports) {
            ArrayList<ImportRecord> result = new ArrayList<ImportRecord>();
            final int count = imports.length;
            //final boolean isEnableAddons = BrowseVectorMap.Instance.isEnabledAddonsImport();
            ProgressInfo pi = new ProgressInfo(0, count, null, getString(R.string.msg_import_pmz_files));
            publishProgress(pi);
            String updateStatus = getString(R.string.import_status_uptodate);
            for (int i = 0; i < count && !mIsCanceled; i++) {
                ImportRecord record = imports[i];
                //new ProgressInfo(i, imports.length,importRecord.getMapName(),null)
                pi.progress = i;
                pi.message = record.mapName;
                publishProgress(pi);
                File mapFile = null;
                File mapFileTemp = null;
                try {
                	// building model from PMZ file
                    Model city = ModelBuilder.loadModel(record.fileName);
                    // define file names
                    String mapName = city.systemName;
                    String mapFileName = MapSettings.getMapFileName(mapName);
                    String mapFileNameTemp = MapSettings.getTemporaryMapFile(mapName);
                    mapFile = new File(mapFileName);
                    mapFileTemp = new File(mapFileNameTemp);
                    // remove temporary file is exists 
                    if(mapFileTemp.exists()){
                    FileUtil.delete(mapFileTemp);
                    }
                    // serialize model into temporary file
                    ModelBuilder.saveModel(mapFileTemp.getAbsolutePath(), city);
                    // remove old map file if exists
                    if(mapFile.exists()){
                    	FileUtil.delete(mapFile);
                    }
                    // move model from temporary to persistent file name
                    FileUtil.move(mapFileTemp, mapFile);
                     
                    record.checked = false;
                    record.status = updateStatus;
                    record.statusColor = Color.GREEN;
                    record.severity = 1;
                    MapSettings.refreshMapList();
                } catch (Throwable e) {
                	if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
                		Log.e(Constants.LOG_TAG_MAIN, getString(R.string.log_import_failed), e);
                	}
                    result.add(new ImportRecord(-1, 
                    		record.mapName, 
                    		record.fileName, 
                    		getString(R.string.msg_import_failed) + "\n" + e.toString(), 
                    		Color.RED, 
                    		false));
                    if(mapFileTemp!=null && mapFileTemp.exists()){
                    	FileUtil.delete(mapFileTemp);
                    }
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
            ProgressInfo.ChangeProgress(
                    values[0],
                    mProgressBar,
                    mProgressTitle,
                    mProgressText,
                    mProgressCounter,
                    getString(R.string.template_progress_count)
            );
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            setupImportMode();
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(List<ImportRecord> result) {
            if (result.size() == 0) {
                setResult(RESULT_OK);
                finish();
            }
            setupReportMode(result);
            super.onPostExecute(result);
        }


    }

    private enum Mode {
        Index,
        Select,
        Import,
        Report,
        Empty
    }

    private enum CleanupMode {
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
    private TextView mProgressCounter;

    private IndexTask mIndexTask;
    private ImportTask mImportTask;

    private List<ImportRecord> mFiles;

    private final int MAIN_MENU_REFRESH = 1;
    private final int MAIN_MENU_IMPORT = 2;
    private final int MAIN_MENU_CLEANUP = 3;
    private final int MAIN_MENU_SELECT_ALL = 4;
    private final int MAIN_MENU_SELECT_NONE = 5;
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
        mMainMenuImport = menu.add(0, MAIN_MENU_IMPORT, 0, R.string.menu_import).setIcon(android.R.drawable.ic_menu_add);
        mMainMenuSelectAll = menu.add(0, MAIN_MENU_SELECT_ALL, 1, R.string.menu_select_all).setIcon(android.R.drawable.ic_menu_agenda);
        mMainMenuSelectNone = menu.add(0, MAIN_MENU_SELECT_NONE, 2, R.string.menu_select_none).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        mMainMenuCleanup = menu.add(0, MAIN_MENU_CLEANUP, 3, R.string.menu_cleanup).setIcon(android.R.drawable.ic_menu_delete);
        mMainMenuRefresh = menu.add(0, MAIN_MENU_REFRESH, 4, R.string.menu_refresh).setIcon(android.R.drawable.ic_menu_rotate);
        mMainMenuStop = menu.add(0, MAIN_MENU_STOP, 5, R.string.menu_stop).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        mMainMenuCreated = true;
        updateMenuStatus(null);
        return true;
    }

    private void updateMenuStatus(Mode mode) {
        if (mode != null) {
            mMode = mode;
        }

        if (!mMainMenuCreated) return;

        mMainMenuRefresh.setVisible(mMode == Mode.Select || mMode == Mode.Empty);
        mMainMenuImport.setVisible(mMode == Mode.Select);
        mMainMenuSelectAll.setVisible(mMode == Mode.Select);
        mMainMenuSelectNone.setVisible(mMode == Mode.Select);
        mMainMenuCleanup.setVisible(mMode == Mode.Select || mMode == Mode.Report);
        mMainMenuStop.setVisible(mMode == Mode.Index || mMode == Mode.Import);

        if (mMode == Mode.Select) {
            final List<ImportRecord> dataChecked = mAdapter.getCheckedData();
            final List<ImportRecord> data = mAdapter.getData();
            mMainMenuImport.setEnabled(dataChecked.size() > 0);
            mMainMenuSelectAll.setEnabled(dataChecked.size() < data.size());
            mMainMenuSelectNone.setEnabled(dataChecked.size() > 0);
        } else {
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
                if (mMode == Mode.Select) {
                    requestCleanupSelectMode();
                }
                if (mMode == Mode.Report) {
                    requestCleanupReportMode();
                }
                return true;
            case MAIN_MENU_STOP:
                if (mMode == Mode.Index) {
                    cancelIndexMode();
                }
                if (mMode == Mode.Import) {
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
        if (mImportTask != null && mImportTask.getStatus() != Status.FINISHED) {
            mImportTask.cancel(false);
        }
        if (mIndexTask != null && mIndexTask.getStatus() != Status.FINISHED) {
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
        builder.setSingleChoiceItems(R.array.import_pmz_cleanup_items, mCleanupMode.ordinal(), new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String itemName = getResources().getStringArray(R.array.import_pmz_cleanup_items)[which];
                mCleanupMode = CleanupMode.valueOf(itemName);
            }
        });
        builder.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cleanupSelectMode();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void requestCleanupReportMode() {
    }

    private void cleanupSelectMode() {
        List<ImportRecord> list = null;
        switch (mCleanupMode) {
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
        if (list != null) {
            for (ImportRecord record : list) {
                FileUtil.delete(new File(record.fileName));
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
        setupSelectMode(mFiles);
    }

    private void setupSelectMode(List<ImportRecord> result) {
        if (result.size() > 0) {
            setContentView(R.layout.import_pmz_main);
            setTitle(R.string.import_title_confirm);
            mAdapter = new ImportListAdapter(ImportPmz.this, 0, result);
            mListView = (ListView) findViewById(R.id.import_pmz_list);
            mListView.setAdapter(mAdapter);
            updateMenuStatus(Mode.Select);
        } else {
            setContentView(R.layout.import_pmz_empty);
            setTitle(R.string.import_title_empty);
            updateMenuStatus(Mode.Empty);
        }
    }

    private void setupImportMode() {
        setContentView(R.layout.import_pmz_progress);
        setTitle(R.string.import_title_importing);
        mProgressBar = (ProgressBar) findViewById(R.id.import_pmz_progress_bar);
        mProgressTitle = (TextView) findViewById(R.id.import_pmz_progress_title);
        mProgressText = (TextView) findViewById(R.id.import_pmz_progress_text);
        mProgressCounter = (TextView) findViewById(R.id.import_pmz_progress_counter);
        updateMenuStatus(Mode.Import);
    }

    private void setupIndexMode() {
        setContentView(R.layout.import_pmz_progress);
        setTitle(R.string.import_title_indexing);
        mProgressBar = (ProgressBar) findViewById(R.id.import_pmz_progress_bar);
        mProgressTitle = (TextView) findViewById(R.id.import_pmz_progress_title);
        mProgressText = (TextView) findViewById(R.id.import_pmz_progress_text);
        mProgressCounter = (TextView) findViewById(R.id.import_pmz_progress_counter);
        updateMenuStatus(Mode.Index);
    }

    private void setupReportMode(List<ImportRecord> result) {
        setContentView(R.layout.import_pmz_main);
        setTitle(R.string.import_title_report);
        mAdapter = new ImportListAdapter(ImportPmz.this, 0, result);
        mListView = (ListView) findViewById(R.id.import_pmz_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ImportPmz.this.setResult(RESULT_OK);
                ImportPmz.this.finish();

            }
        });
        updateMenuStatus(Mode.Report);
    }


}
