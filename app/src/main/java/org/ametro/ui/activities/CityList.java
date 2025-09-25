package org.ametro.ui.activities;

import android.app.ProgressDialog;
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

public class CityList extends AppCompatActivity implements CityListFragment.ICitySelectionListener {

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
    }
}
