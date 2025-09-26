package org.ametro.ng.ui.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import org.ametro.ng.R;
import org.ametro.ng.app.ApplicationEx;
import org.ametro.ng.app.ApplicationSettingsProvider;
import org.ametro.ng.app.Constants;
import org.ametro.ng.model.MapContainer;
import org.ametro.ng.model.ModelUtil;
import org.ametro.ng.model.entities.MapDelay;
import org.ametro.ng.model.entities.MapScheme;
import org.ametro.ng.model.entities.MapSchemeLine;
import org.ametro.ng.model.entities.MapSchemeStation;
import org.ametro.ng.model.serialization.MapSerializationException;
import org.ametro.ng.providers.TransportIconsProvider;
import org.ametro.ng.routes.MapRouteProvider;
import org.ametro.ng.routes.RouteUtils;
import org.ametro.ng.routes.entities.MapRouteQueryParameters;
import org.ametro.ng.ui.adapters.StationSearchAdapter;
import org.ametro.ng.ui.navigation.INavigationControllerListener;
import org.ametro.ng.ui.navigation.NavigationController;
import org.ametro.ng.ui.tasks.MapLoadAsyncTask;
import org.ametro.ng.ui.testing.TestMenuOptionsProcessor;
import org.ametro.ng.ui.views.MultiTouchMapView;
import org.ametro.ng.ui.widgets.MapBottomPanelWidget;
import org.ametro.ng.ui.widgets.MapSelectionIndicatorsWidget;
import org.ametro.ng.ui.widgets.MapTopPanelWidget;
import org.ametro.ng.utils.StringUtils;

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
        mapContainerView = findViewById(R.id.map_container);
        mapPanelView = findViewById(R.id.map_panel);
        mapTopPanel = new MapTopPanelWidget(findViewById(R.id.map_top_panel));
        mapBottomPanel = new MapBottomPanelWidget(findViewById(R.id.map_bottom_panel), this);
        mapSelectionIndicators = new MapSelectionIndicatorsWidget(
                this,
                findViewById(R.id.begin_indicator),
                findViewById(R.id.end_indicator)
        );

        app = ApplicationEx.getInstance(this);
        settingsProvider = app.getApplicationSettingsProvider();

        findViewById(R.id.map_empty_panel).setOnClickListener(v -> onOpenMaps());

        testMenuOptionsProcessor = new TestMenuOptionsProcessor(this);
        navigationController = new NavigationController(
                this,
                this,
                new TransportIconsProvider(this),
                ApplicationEx.getInstance(this).getCountryFlagProvider(),
                ApplicationEx.getInstance(this).getLocalizedMapInfoProvider()
        );


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (navigationController.isDrawerOpen()) {
                    navigationController.closeDrawer();
                } else if (mapBottomPanel.isOpened()) {
                    mapBottomPanel.hide();
                } else if (mapSelectionIndicators.hasSelection()) {
                    mapSelectionIndicators.clearSelection();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapSelectionIndicators != null) {
            MapSchemeStation begin = mapSelectionIndicators.getBeginStation();
            MapSchemeStation end = mapSelectionIndicators.getEndStation();
            app.setSelectedStations(begin != null ? begin.getUid() : null, end != null ? end.getUid() : null);
        }
        if (mapView != null) {
            app.setCenterPositionAndScale(mapView.getCenterPositionAndScale());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMapViewState();
        if (mapView != null && app.getCenterPositionAndScale() != null) {
            var data = app.getCenterPositionAndScale();
            mapView.setCenterPositionAndScale(data.first, data.second, false);

            var beginUid = app.getSelectedBeginUid();
            var endUid = app.getSelectedEndUid();
            if (beginUid != null) {
                var beginInfo = ModelUtil.findStationByUid(scheme, beginUid);
                if (beginInfo != null) {
                    mapSelectionIndicators.setBeginStation(beginInfo.second);
                }
            }
            if (endUid != null) {
                var endInfo = ModelUtil.findStationByUid(scheme, endUid);
                if (endInfo != null) {
                    mapSelectionIndicators.setEndStation(endInfo.second);
                }
            }

            if (mapSelectionIndicators.hasSelection()) {
                onRouteSelectionComplete(
                        mapSelectionIndicators.getBeginStation(),
                        mapSelectionIndicators.getEndStation());
            }


        }
        if (mapView == null) {
            var currentMapFileName = settingsProvider.getCurrentMapFileName();
            if (currentMapFileName != null) {
                var mapCatalogProvider = app.getMapCatalogProvider();
                var currentMap = mapCatalogProvider.getMapCatalog().findMap(currentMapFileName);
                if(currentMap!=null){
                    new MapLoadAsyncTask(this, new MapContainer(
                            mapCatalogProvider,
                            currentMap,
                            settingsProvider.getPreferredMapLanguage())
                    ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationController.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navigationController.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return navigationController.onOptionsItemSelected(item)
                || testMenuOptionsProcessor.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);

        var manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        final var searchMenuItem = menu.findItem(R.id.action_search);
        final var searchView = (SearchView) searchMenuItem.getActionView();
        final var selectedStation = new MapSchemeStation[1];

        assert searchView != null;

        searchView.setSubmitButtonEnabled(false);
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(() -> {
            selectedStation[0] = null;
            return true;
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                selectedStation[0] = ((StationSearchAdapter) searchView.getSuggestionsAdapter()).getStation(position);
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
                    var stationInfo =
                            ModelUtil.findStationByUid(scheme, selectedStation[0].getUid());
                    if (stationInfo != null) {
                        var stationInformation = container
                                .findStationInformation(stationInfo.first.getName(), stationInfo.second.getName());
                        var p = new PointF(selectedStation[0].getPosition().x, selectedStation[0].getPosition().y);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_MAPS_ACTION) {
            if (resultCode == RESULT_OK) {
                var localMapCatalogManager = app.getMapCatalogProvider();
                var mapInfo = localMapCatalogManager.findMapByName(data.getStringExtra(Constants.MAP_PATH));
                var mapContainer = new MapContainer(localMapCatalogManager, mapInfo, settingsProvider.getPreferredMapLanguage());
                new MapLoadAsyncTask(this,  mapContainer).execute();
            }
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
        this.app.setCurrentMapViewState(container, schemeName, enabledTransports);
        initMapViewState();
        settingsProvider.setCurrentMap(container.getMapInfo().getFileName());
    }

    private void initMapViewState() {
        if (app.getContainer() == null || app.getContainer().getMapInfo() == null) {
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

        mapView.setOnClickListener(v -> {
            var stationInfo = ModelUtil.findTouchedStation(scheme, mapView.getTouchPoint());
            if (stationInfo != null) {
                var stationInformation = container.findStationInformation(stationInfo.first.getName(), stationInfo.second.getName());
                mapBottomPanel.show(
                        stationInfo.first,
                        stationInfo.second,
                        stationInformation != null && stationInformation.getMapFilePath() != null);
            } else {
                mapBottomPanel.hide();
                mapSelectionIndicators.clearSelection();
                onRouteSelectionCleared();
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
        new MapLoadAsyncTask(this, container, schemeName,
                enabledTransportsSet.toArray(new String[0])).execute();
        return true;
    }

    @Override
    public boolean onToggleTransport(String transportName, boolean checked) {
        try {
            if (checked) {
                enabledTransportsSet.add(transportName);
                container.loadSchemeWithTransports(
                        schemeName,
                        enabledTransportsSet.toArray(new String[0]));
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
        var intent = new Intent(this, StationDetails.class);
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
        var routes = MapRouteProvider.findRoutes(
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
        final var delays = container.getMetadata().getDelays();
        if (delays.length == 0) {
            return null;
        }
        var index = 0;
        for (var delay : delays) {
            if (delay == currentDelay) {
                return index;
            }
            index++;
        }
        return 0;
    }
}
