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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.ametro.MapSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.adapter.MapListAdapter;
import org.ametro.model.City;
import org.ametro.other.FileGroupsDictionary;
import org.ametro.other.ProgressInfo;
import org.ametro.util.ModelUtil;

import java.io.File;
import java.io.FilenameFilter;


public class BrowseLibrary extends Activity implements ExpandableListView.OnChildClickListener {

    private class IndexTask extends AsyncTask<Void, ProgressInfo, FileGroupsDictionary> {
        private boolean mIsCanceled = false;
        private boolean mIsProgressVisible = false;

        private void scanModelFileContent(FileGroupsDictionary map, String fileName, String fullFileName) {
            try {
                City city = ModelUtil.loadModelDescription(fullFileName);
                if (city.sourceVersion == MapSettings.getSourceVersion()) {
                    map.putFile(city.countryName, city.cityName, fileName);
                }
            } catch (Exception e) {
                Log.d("aMetro", "Map indexing failed for " + fileName, e);
            }
        }

        private FileGroupsDictionary scanMapDirectory(File dir) {
            FileGroupsDictionary map;
            ProgressInfo pi = new ProgressInfo(0, 0, null, "Search maps...");
            publishProgress(pi);
            map = new FileGroupsDictionary();
            map.timestamp = dir.lastModified();
            String[] files = dir.list(new FilenameFilter() {
                public boolean accept(File f, String filename) {
                    return filename.endsWith(MapSettings.MAP_FILE_TYPE);
                }
            });

            if (files != null) {
                final int count = files.length;
                pi.title = "Read maps...";
                pi.maximum = count;

                for (int i = 0; i < count && !mIsCanceled; i++) {
                    String fileName = files[i];
                    pi.progress = i;
                    pi.message = fileName;
                    publishProgress(pi);
                    String fullFileName = dir.getAbsolutePath() + '/' + files[i];
                    scanModelFileContent(map, fileName, fullFileName);
                }
            }
            return map;
        }

        @Override
        protected FileGroupsDictionary doInBackground(Void... params) {
            final String cacheFileName = MapSettings.ROOT_PATH + MapSettings.MAPS_LIST;
            final File dir = new File(MapSettings.MAPS_PATH);
            FileGroupsDictionary map = FileGroupsDictionary.read(cacheFileName);
            if (map == null || map.timestamp < dir.lastModified()) {
                map = scanMapDirectory(dir);
                FileGroupsDictionary.write(map, cacheFileName);
            }
            return map;

        }


        @Override
        protected void onProgressUpdate(ProgressInfo... values) {
            if (!mIsProgressVisible) {
                mIsProgressVisible = true;
                setContentView(R.layout.import_pmz_progress);
                mProgressBar = (ProgressBar) findViewById(R.id.import_pmz_progress_bar);
                mProgressTitle = (TextView) findViewById(R.id.import_pmz_progress_title);
                mProgressText = (TextView) findViewById(R.id.import_pmz_progress_text);
                mProgressCounter = (TextView) findViewById(R.id.import_pmz_progress_counter);
            }
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
        protected void onCancelled() {
            mIsCanceled = true;
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            mDefaultPackageFileName = null;
            Intent intent = getIntent();
            Uri uri = intent != null ? intent.getData() : null;
            if (uri != null) {
                mDefaultPackageFileName = MapUri.getMapName(uri) + MapSettings.MAP_FILE_TYPE;
            }
            setContentView(R.layout.global_wait);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(FileGroupsDictionary result) {
            if (result.getGroupCount() > 0) {
                setContentView(R.layout.browse_library_main);
                mAdapter = new MapListAdapter(BrowseLibrary.this, result);
                mListView = (ExpandableListView) findViewById(R.id.library_map_list);
                mListView.setOnChildClickListener(BrowseLibrary.this);
                mListView.setAdapter(mAdapter);
                if (mDefaultPackageFileName != null) {
                    mAdapter.setSelectedFile(mDefaultPackageFileName);
                    int groupPosition = mAdapter.getSelectedGroupPosition();
                    int childPosition = mAdapter.getSelectChildPosition();
                    if (groupPosition != -1) {
                        mListView.expandGroup(groupPosition);
                        if (childPosition != -1) {
                            mListView.setSelectedChild(groupPosition, childPosition, true);
                        }
                    }
                }
            } else {
                setContentView(R.layout.browse_library_empty);
            }
            super.onPostExecute(result);
        }
    }


    private MapListAdapter mAdapter;
    private ExpandableListView mListView;
    private String mDefaultPackageFileName;

    private ProgressBar mProgressBar;
    private TextView mProgressTitle;
    private TextView mProgressText;
    private TextView mProgressCounter;

    private final int MAIN_MENU_REFRESH = 1;
    private final int MAIN_MENU_ALL_MAPS = 2;
    private final int MAIN_MENU_MY_MAPS = 3;
    private final int MAIN_MENU_LOCATION = 4;
    private final int MAIN_MENU_IMPORT = 5;

    private MenuItem mMainMenuAllMaps;
    private MenuItem mMainMenuMyMaps;

    private IndexTask mIndexTask;

    private final static int REQUEST_IMPORT = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MAIN_MENU_REFRESH, 0, R.string.menu_refresh).setIcon(android.R.drawable.ic_menu_rotate);
        menu.add(0, MAIN_MENU_LOCATION, 3, R.string.menu_location).setIcon(android.R.drawable.ic_menu_mylocation);
        menu.add(0, MAIN_MENU_IMPORT, 4, R.string.menu_import).setIcon(android.R.drawable.ic_menu_add);

        mMainMenuAllMaps = menu.add(0, MAIN_MENU_ALL_MAPS, 1, R.string.menu_all_maps).setIcon(android.R.drawable.ic_menu_mapmode).setVisible(false);
        mMainMenuMyMaps = menu.add(0, MAIN_MENU_MY_MAPS, 2, R.string.menu_my_maps).setIcon(android.R.drawable.ic_menu_myplaces);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MAIN_MENU_REFRESH:
                MapSettings.refreshMapList();
                beginIndexing();
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
                startActivityForResult(new Intent(this, ImportPmz.class), REQUEST_IMPORT);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMPORT:
                beginIndexing();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapSettings.checkPrerequisite(this);
        beginIndexing();
    }

    @Override
    protected void onStop() {
        if (mIndexTask != null && mIndexTask.getStatus() != Status.FINISHED) {
            mIndexTask.cancel(false);
        }
        super.onStop();
    }

    private void beginIndexing() {
        mIndexTask = new IndexTask();
        mIndexTask.execute();
    }

    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        String fileName = mAdapter.getFileName(groupPosition, childPosition);
        String mapName = fileName.replace(".ametro", "");
        Intent i = new Intent();
        i.setData(MapUri.create(mapName));
        setResult(RESULT_OK, i);
        finish();
        return true;
    }

}
