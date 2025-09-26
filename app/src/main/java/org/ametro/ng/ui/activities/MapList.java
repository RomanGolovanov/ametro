package org.ametro.ng.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NavUtils;
import androidx.core.view.MenuItemCompat;

import org.ametro.ng.R;
import org.ametro.ng.app.Constants;
import org.ametro.ng.catalog.entities.MapInfo;
import org.ametro.ng.ui.fragments.MapListFragment;
import org.ametro.ng.ui.loaders.ExtendedMapInfo;

public class MapList extends AppCompatActivity implements MapListFragment.IMapListEventListener{

    private MapListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list_view);

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listFragment = (MapListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        assert listFragment != null;
        listFragment.setMapListEventListener(this);
    }

    @Override
    public void onOpenMap(MapInfo map) {
        Intent viewIntent = new Intent();
        viewIntent.putExtra(Constants.MAP_PATH, map.getFileName());
        setResult(RESULT_OK, viewIntent);
        finish();
    }

    @Override
    public void onLoadedMaps(ExtendedMapInfo[] maps) {

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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
