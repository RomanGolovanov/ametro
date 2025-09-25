package org.ametro.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;            // AndroidX
import androidx.appcompat.app.AppCompatActivity;   // AndroidX
import androidx.appcompat.widget.SearchView;       // AndroidX
import androidx.appcompat.widget.Toolbar;          // AndroidX
import androidx.core.app.NavUtils;                 // AndroidX
import androidx.core.view.MenuItemCompat;          // AndroidX

import org.ametro.R;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.ui.fragments.CityListFragment;
import org.ametro.ui.tasks.MapInstallerAsyncTask;
import org.ametro.ui.tasks.TaskHelpers;
import org.ametro.utils.StringUtils;

public class CityList extends AppCompatActivity
        implements CityListFragment.ICitySelectionListener,
        MapInstallerAsyncTask.IMapInstallerEventListener {

    private CityListFragment cityListFragment;
    private ProgressDialog loadingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list_view);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        cityListFragment = (CityListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.geography_list_fragment);

        cityListFragment.setCitySelectionListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_city_list, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(cityListFragment);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCitySelected(MapInfo[] maps) {
        final MapInstallerAsyncTask downloadTask =
                new MapInstallerAsyncTask(this, this, maps);

        loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setMessage(getString(R.string.msg_downloading));
        loadingProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        loadingProgressDialog.setCancelable(true);
        loadingProgressDialog.setIndeterminate(false);
        loadingProgressDialog.setMax(100);
        loadingProgressDialog.setProgress(0);
        loadingProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
        loadingProgressDialog.show();

        downloadTask.execute();
    }

    @Override
    public void onMapDownloadingComplete(MapInfo[] maps) {
        loadingProgressDialog.dismiss();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onMapDownloadingProgress(long currentSize, long totalSize, MapInfo downloadingMap) {
        loadingProgressDialog.setProgress((int) (currentSize * 100 / totalSize));
        loadingProgressDialog.setMessage(String.format(
                getString(R.string.msg_download_progress),
                downloadingMap.getFileName() + ": " +
                        String.format("%s / %s",
                                StringUtils.humanReadableByteCount(currentSize, false),
                                StringUtils.humanReadableByteCount(totalSize, false))));
    }

    @Override
    public void onMapDownloadingFailed(MapInfo[] maps, Throwable reason) {
        loadingProgressDialog.dismiss();
        TaskHelpers.displayFailReason(this, reason);
    }
}
