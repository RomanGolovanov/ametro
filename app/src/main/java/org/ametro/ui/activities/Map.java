package org.ametro.ui.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.ApplicationSettingsProvider;
import org.ametro.app.Constants;
import org.ametro.catalog.MapCatalogManager;
import org.ametro.model.MapContainer;
import org.ametro.model.ModelUtil;
import org.ametro.model.entities.MapDelay;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.model.entities.MapStationInformation;
import org.ametro.model.serialization.MapSerializationException;
import org.ametro.providers.TransportIconsProvider;
import org.ametro.routes.MapRouteProvider;
import org.ametro.routes.RouteUtils;
import org.ametro.routes.entities.MapRoute;
import org.ametro.routes.entities.MapRouteQueryParameters;
import org.ametro.ui.adapters.StationSearchAdapter;
import org.ametro.ui.navigation.INavigationControllerListener;
import org.ametro.ui.navigation.NavigationController;
import org.ametro.ui.tasks.MapLoadAsyncTask;
import org.ametro.ui.testing.DebugToast;
import org.ametro.ui.testing.TestMenuOptionsProcessor;
import org.ametro.ui.views.MultiTouchMapView;
import org.ametro.ui.widgets.MapBottomPanelWidget;
import org.ametro.ui.widgets.MapSelectionIndicatorsWidget;
import org.ametro.ui.widgets.MapTopPanelWidget;
import org.ametro.utils.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Map extends AppCompatActivity implements
        MapLoadAsyncTask.IMapLoadingEventListener,
        INavigationControllerListener,
        MapBottomPanelWidget.IMapBottomPanelEventListener,
        MapSelectionIndicatorsWidget.IMapSelectionEventListener {

    private static final int OPEN_MAPS_ACTION = 1;
    private static final int OPEN_SETTINGS_ACTION = 2;
    private static final int OPEN_STATION_DETAILS = 3;

    private NavigationController navigationController;
    private ProgressDialog loadingProgressDialog;
    private MapContainer container;
    private TestMenuOptionsProcessor testMenuOptionsProcessor;
    private Set<String> enabledTransportsSet;
    private String schemeName;
    private MapDelay currentDelay;

    private ViewGroup mapContainerView;
    private View mapPanelView;

    private MultiTouchMapView mapView;
    private ApplicationEx app;
    private ApplicationSettingsProvider settingsProvider;
    private MapScheme scheme;

    private MapTopPanelWidget mapTopPanel;
    private MapBottomPanelWidget mapBottomPanel;
    private MapSelectionIndicatorsWidget mapSelectionIndicators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        mapContainerView = (ViewGroup) findViewById(R.id.map_container);
        mapPanelView = findViewById(R.id.map_panel);
        mapTopPanel = new MapTopPanelWidget((ViewGroup) findViewById(R.id.map_top_panel));
        mapBottomPanel = new MapBottomPanelWidget((ViewGroup) findViewById(R.id.map_bottom_panel), this);
        mapSelectionIndicators = new MapSelectionIndicatorsWidget(
                this,
                findViewById(R.id.begin_indicator),
                findViewById(R.id.end_indicator)
        );

        app = ApplicationEx.getInstance(this);
        settingsProvider = app.getApplicationSettingsProvider();

        findViewById(R.id.map_empty_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOpenMaps();
            }
        });

        testMenuOptionsProcessor = new TestMenuOptionsProcessor(this);
        navigationController = new NavigationController(
                this,
                this,
                new TransportIconsProvider(this),
                ApplicationEx.getInstance(this).getCountryFlagProvider(),
                ApplicationEx.getInstance(this).getLocalizedMapInfoProvider()
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mapView!=null){
            app.setCenterPositionAndScale(mapView.getCenterPositionAndScale());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMapViewState();
        if(mapView!=null && app.getCenterPositionAndScale()!=null){
            android.util.Pair<PointF, Float> data = app.getCenterPositionAndScale();
            mapView.setCenterPositionAndScale(data.first, data.second, false);
        }
        if(mapView==null){
            File currentMapFile = settingsProvider.getCurrentMap();
            if (currentMapFile != null) {
                new MapLoadAsyncTask(this, this, new MapContainer(
                        currentMapFile,
                        settingsProvider.getPreferredMapLanguage())
                ).execute();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationController.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navigationController.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return navigationController.onOptionsItemSelected(item)
                || testMenuOptionsProcessor.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView)searchMenuItem.getActionView();
        final MapSchemeStation[] selectedStation = new MapSchemeStation[1];

        searchView.setSubmitButtonEnabled(false);
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                selectedStation[0] = null;
                return true;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }
            @Override
            public boolean onSuggestionClick(int position) {
                selectedStation[0] = ((StationSearchAdapter)searchView.getSuggestionsAdapter()).getStation(position);
                searchView.setQuery(selectedStation[0].getDisplayName(), true);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (scheme == null) {
                    return true;
                }
                if (selectedStation[0] != null) {
                    Pair<MapSchemeLine, MapSchemeStation> stationInfo =
                            ModelUtil.findStationByUid(scheme, selectedStation[0].getUid());
                    if (stationInfo != null) {
                        MapStationInformation stationInformation = container
                                .findStationInformation(stationInfo.first.getName(), stationInfo.second.getName());
                        PointF p = new PointF(selectedStation[0].getPosition().x, selectedStation[0].getPosition().y);
                        mapView.setCenterPositionAndScale(p, mapView.getScale(), true);
                        mapBottomPanel.show(
                                stationInfo.first,
                                stationInfo.second,
                                stationInformation != null && stationInformation.getMapFilePath() != null);
                        searchMenuItem.collapseActionView();
                    }

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (scheme == null) {
                    return true;
                }
                searchView.setSuggestionsAdapter(
                        StationSearchAdapter.createFromMapScheme(Map.this, scheme, query));
                return true;

            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (navigationController.isDrawerOpen()) {
            navigationController.closeDrawer();
        } else if (mapBottomPanel.isOpened()) {
            mapBottomPanel.hide();
        } else if (mapSelectionIndicators.hasSelection()) {
            mapSelectionIndicators.clearSelection();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OPEN_MAPS_ACTION:
                if (resultCode == RESULT_OK) {
                    MapCatalogManager localMapCatalogManager = app.getLocalMapCatalogManager();
                    new MapLoadAsyncTask(this, this, new MapContainer(
                        localMapCatalogManager.getMapFile(
                            localMapCatalogManager.findMapByName(
                                data.getStringExtra(Constants.MAP_PATH))),
                        settingsProvider.getPreferredMapLanguage())
                    ).execute();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBeforeMapLoading(MapContainer container, String schemeName, String[] enabledTransports) {
        if (!container.isLoaded(schemeName, enabledTransports)) {

            if (loadingProgressDialog != null) {
                loadingProgressDialog.dismiss();
                loadingProgressDialog = null;
            }

            loadingProgressDialog = new ProgressDialog(this);
            loadingProgressDialog.setIndeterminate(true);
            loadingProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingProgressDialog.setCancelable(false);
            loadingProgressDialog.setMessage(getResources().getString(R.string.msg_map_loading_progress, schemeName));
            loadingProgressDialog.show();
        }
    }

    @Override
    public void onMapLoadComplete(final MapContainer container, final String schemeName, String[] enabledTransports, long time) {
        if (loadingProgressDialog != null) {
            loadingProgressDialog.dismiss();
            loadingProgressDialog = null;
        }
        DebugToast.show(this, getString(R.string.msg_map_loaded, time), Toast.LENGTH_LONG);
        this.app.setCurrentMapViewState(container, schemeName, enabledTransports);
        initMapViewState();
        settingsProvider.setCurrentMap(container.getMapFile());
    }



    private void initMapViewState(){
        if(app.getContainer()==null || !app.getContainer().getMapFile().exists()){
            app.clearCurrentMapViewState();
            navigationController.setNavigation(null, null, null, null);
            mapPanelView.setVisibility(View.GONE);
            return;
        }

        container = app.getContainer();
        schemeName = app.getSchemeName();
        scheme = container.getScheme(schemeName);

        enabledTransportsSet = new HashSet<>(Arrays.asList(
                app.getEnabledTransports() != null
                        ? app.getEnabledTransports()
                        : container.getScheme(schemeName).getDefaultTransports()));

        navigationController.setNavigation(container, schemeName, app.getEnabledTransports(), currentDelay);

        mapPanelView.setVisibility(View.VISIBLE);

        mapSelectionIndicators.clearSelection();
        mapView = new MultiTouchMapView(this, container, schemeName, mapSelectionIndicators);

        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<MapSchemeLine, MapSchemeStation> stationInfo =
                        ModelUtil.findTouchedStation(scheme, mapView.getTouchPoint());
                if (stationInfo != null) {

                    MapStationInformation stationInformation = container
                            .findStationInformation(stationInfo.first.getName(), stationInfo.second.getName());

                    mapBottomPanel.show(
                            stationInfo.first,
                            stationInfo.second,
                            stationInformation != null && stationInformation.getMapFilePath() != null);
                } else {
                    mapBottomPanel.hide();
                }
            }
        });

        mapContainerView.removeAllViews();
        mapContainerView.addView(mapView);
        mapView.requestFocus();
    }


    @Override
    public void onMapLoadFailed(MapContainer container, String schemeName, String[] enabledTransports, Throwable reason) {
        if (loadingProgressDialog != null) {
            loadingProgressDialog.dismiss();
            loadingProgressDialog = null;
        }
        Toast.makeText(this, getString(R.string.msg_map_loading_failed, reason.getMessage()), Toast.LENGTH_LONG).show();
        Log.e(Constants.LOG, "Map load failed due exception: " + reason.getMessage(), reason);
    }

    @Override
    public boolean onOpenMaps() {
        mapBottomPanel.hide();
        startActivityForResult(new Intent(this, MapList.class), OPEN_MAPS_ACTION);
        return true;
    }

    @Override
    public boolean onOpenSettings() {
        mapBottomPanel.hide();
        startActivityForResult(new Intent(this, SettingsList.class), OPEN_SETTINGS_ACTION);
        return true;
    }

    @Override
    public boolean onOpenAbout() {
        startActivity(new Intent(this, About.class));
        return false;
    }


    @Override
    public boolean onChangeScheme(String schemeName) {
        mapBottomPanel.hide();
        new MapLoadAsyncTask(this, this, container, schemeName, enabledTransportsSet.toArray(new String[enabledTransportsSet.size()])).execute();
        return true;
    }

    @Override
    public boolean onToggleTransport(String transportName, boolean checked) {
        try {
            if (checked) {
                enabledTransportsSet.add(transportName);
                container.loadSchemeWithTransports(
                        schemeName,
                        enabledTransportsSet.toArray(new String[enabledTransportsSet.size()]));
            } else {
                enabledTransportsSet.remove(transportName);
            }
            return true;
        } catch (MapSerializationException e) {
            return false;
        }
    }

    @Override
    public boolean onDelayChanged(MapDelay delay) {
        currentDelay = delay;
        return true;
    }

    @Override
    public void onShowMapDetail(MapSchemeLine line, MapSchemeStation station) {
        Intent intent = new Intent(this, StationDetails.class);
        intent.putExtra(Constants.LINE_NAME, line.getName());
        intent.putExtra(Constants.STATION_NAME, station.getName());
        intent.putExtra(Constants.STATION_UID, station.getUid());
        startActivityForResult(intent, OPEN_STATION_DETAILS);
    }

    @Override
    public void onSelectBeginStation(MapSchemeLine line, MapSchemeStation station) {
        mapBottomPanel.hide();
        mapSelectionIndicators.setBeginStation(station);
    }

    @Override
    public void onSelectEndStation(MapSchemeLine line, MapSchemeStation station) {
        mapBottomPanel.hide();
        mapSelectionIndicators.setEndStation(station);
    }

    @Override
    public void onRouteSelectionComplete(MapSchemeStation begin, MapSchemeStation end) {

        MapRoute[] routes = MapRouteProvider.findRoutes(
            new MapRouteQueryParameters(
                container,
                enabledTransportsSet,
                getCurrentDelayIndex(),
                mapSelectionIndicators.getBeginStation().getUid(),
                mapSelectionIndicators.getEndStation().getUid()));

        if (routes.length == 0) {
            mapView.highlightsElements(null);
            mapTopPanel.hide();
            Toast.makeText(this,
                String.format(getString(R.string.msg_no_route_found), begin.getDisplayName(), end.getDisplayName()),
                Toast.LENGTH_LONG).show();
            return;
        }
        mapView.highlightsElements(RouteUtils.convertRouteToSchemeObjectIds(routes[0], scheme));

        mapTopPanel.show(String.format(
                getString(R.string.msg_from_to),
                begin.getDisplayName(),
                end.getDisplayName(),
                StringUtils.humanReadableTime(routes[0].getDelay())));

    }

    public void onRouteSelectionCleared() {
        if (mapView != null) {
            mapView.highlightsElements(null);
            mapTopPanel.hide();
        }
    }

    public Integer getCurrentDelayIndex() {
        final MapDelay[] delays = container.getMetadata().getDelays();
        if(delays.length == 0){
            return null;
        }
        int index = 0;
        for(MapDelay delay : delays){
            if(delay == currentDelay){
                return index;
            }
            index++;
        }
        return 0;
    }
}
