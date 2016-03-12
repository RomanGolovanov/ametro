package org.ametro.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.ui.fragments.MapListFragment;
import org.ametro.ui.loaders.ExtendedMapInfo;
import org.ametro.ui.loaders.ExtendedMapStatus;
import org.ametro.ui.tasks.MapInstallerAsyncTask;
import org.ametro.ui.tasks.TaskHelpers;
import org.ametro.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MapList extends AppCompatActivity implements
        MapListFragment.IMapListEventListener,
        MapInstallerAsyncTask.IMapInstallerEventListener {

    private final static int ADD_ACTION = 1;

    private MapListFragment listFragment;
    private ProgressDialog progressDialog;

    private MapInfo[] outdatedMaps;
    private View messagePanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list_view);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listFragment = (MapListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        listFragment.setMapListEventListener(this);
        messagePanel = findViewById(R.id.message);
    }

    @Override
    public void onOpenMap(MapInfo map) {
        Intent viewIntent = new Intent();
        viewIntent.putExtra(Constants.MAP_PATH, map.getFileName());
        setResult(RESULT_OK, viewIntent);
        finish();
    }

    @Override
    public void onDeleteMaps(MapInfo[] maps) {
        if (maps.length == 0) {
            Toast.makeText(this, getString(R.string.msg_no_maps_selected), Toast.LENGTH_LONG).show();
            return;
        }
        ApplicationEx.getInstance(this).getLocalMapCatalogManager().deleteMapAll(maps);
        listFragment.forceUpdate();
        Toast.makeText(this, getString(R.string.msg_maps_deleted), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoadedMaps(ExtendedMapInfo[] maps) {
        List<MapInfo> outdated = new ArrayList<>();
        for (ExtendedMapInfo map : maps) {
            if (map.getStatus() == ExtendedMapStatus.Outdated) {
                outdated.add(new MapInfo(map));
            }
        }
        outdatedMaps = outdated.toArray(new MapInfo[outdated.size()]);
        messagePanel.setVisibility(outdatedMaps.length > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAddMap() {
        startActivityForResult(new Intent(this, CityList.class), ADD_ACTION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(listFragment);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_add:
                onAddMap();
                return true;
            case R.id.action_update:
                updateMaps();
                return true;
            case R.id.action_delete:
                listFragment.startContextActionMode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_ACTION:
                listFragment.forceUpdate();
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateMaps() {
        if (outdatedMaps == null || outdatedMaps.length == 0) {
            Toast.makeText(this, getString(R.string.msg_maps_all_updated), Toast.LENGTH_LONG).show();
            return;
        }

        final MapInstallerAsyncTask downloadTask = new MapInstallerAsyncTask(this, this, outdatedMaps);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.msg_maps_updating));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
        progressDialog.show();
        downloadTask.execute();
    }

    @Override
    public void onMapDownloadingProgress(long currentSize, long totalSize, MapInfo downloadingMap) {
        progressDialog.setProgress((int) (currentSize * 100 / totalSize));
        progressDialog.setMessage(String.format(
                getString(R.string.msg_download_progress),
                downloadingMap.getFileName() + ": " +
                        String.format("%s / %s",
                                StringUtils.humanReadableByteCount(currentSize, false),
                                StringUtils.humanReadableByteCount(totalSize, false))));
    }

    @Override
    public void onMapDownloadingComplete(MapInfo[] maps) {
        progressDialog.dismiss();
        Toast.makeText(this, getString(R.string.msg_maps_updated, maps.length), Toast.LENGTH_LONG).show();
        listFragment.forceUpdate();
    }

    @Override
    public void onMapDownloadingFailed(MapInfo[] maps, Throwable reason) {
        progressDialog.dismiss();
        TaskHelpers.displayFailReason(this, reason);
    }

}
