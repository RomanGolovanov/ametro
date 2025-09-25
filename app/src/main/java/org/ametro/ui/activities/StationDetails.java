package org.ametro.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;          // AndroidX
import androidx.appcompat.app.AppCompatActivity; // AndroidX
import androidx.appcompat.widget.Toolbar;        // AndroidX
import androidx.core.content.ContextCompat;      // AndroidX
import androidx.fragment.app.Fragment;           // AndroidX
import androidx.viewpager.widget.ViewPager;      // AndroidX

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.ui.fragments.StationMapFragment;
import org.ametro.ui.toolbar.FragmentPagerArrayAdapter;
import org.ametro.ui.toolbar.FragmentPagerTabInfo;
import org.ametro.ui.views.SlidingTabLayout;

import java.util.ArrayList;

public class StationDetails extends AppCompatActivity implements SlidingTabLayout.TabColorizer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details_view);

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new FragmentPagerArrayAdapter(getSupportFragmentManager(), getTabs()));

        SlidingTabLayout slidingTabLayout = findViewById(R.id.sliding_tabs);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
        slidingTabLayout.setCustomTabColorizer(this);

        ApplicationEx application = ApplicationEx.getInstance(this);
        Intent intent = getIntent();
        MapSchemeStation station = application.getContainer().findSchemeStation(
                application.getSchemeName(),
                intent.getStringExtra(Constants.LINE_NAME),
                intent.getStringExtra(Constants.STATION_NAME));

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(station.getDisplayName());
        }
    }

    private ArrayList<FragmentPagerTabInfo> getTabs() {
        ArrayList<FragmentPagerTabInfo> tabs = new ArrayList<>();

        Fragment mapFragment = new StationMapFragment();
        mapFragment.setArguments(getIntent().getExtras());
        tabs.add(new FragmentPagerTabInfo(getString(R.string.tab_map), mapFragment));

        // If you want to enable "About" tab later, just uncomment:
        // Fragment aboutFragment = new StationAboutFragment();
        // aboutFragment.setArguments(getIntent().getExtras());
        // tabs.add(new FragmentPagerTabInfo(getString(R.string.tab_about), aboutFragment));

        return tabs;
    }

    @Override
    public int getIndicatorColor(int position) {
        return ContextCompat.getColor(this, R.color.accent);
    }
}
