package io.github.romangolovanov.apps.ametro.ui.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.ApplicationEx
import io.github.romangolovanov.apps.ametro.app.ApplicationSettingsProvider
import io.github.romangolovanov.apps.ametro.app.Constants
import io.github.romangolovanov.apps.ametro.model.MapContainer
import io.github.romangolovanov.apps.ametro.model.ModelUtil
import io.github.romangolovanov.apps.ametro.model.entities.MapDelay
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation
import io.github.romangolovanov.apps.ametro.model.serialization.MapSerializationException
import io.github.romangolovanov.apps.ametro.providers.TransportIconsProvider
import io.github.romangolovanov.apps.ametro.routes.MapRouteProvider
import io.github.romangolovanov.apps.ametro.routes.RouteUtils
import io.github.romangolovanov.apps.ametro.routes.entities.MapRouteQueryParameters
import io.github.romangolovanov.apps.ametro.ui.adapters.StationSearchAdapter
import io.github.romangolovanov.apps.ametro.ui.navigation.INavigationControllerListener
import io.github.romangolovanov.apps.ametro.ui.navigation.NavigationController
import io.github.romangolovanov.apps.ametro.ui.testing.TestMenuOptionsProcessor
import io.github.romangolovanov.apps.ametro.ui.views.MultiTouchMapView
import io.github.romangolovanov.apps.ametro.ui.widgets.MapBottomPanelWidget
import io.github.romangolovanov.apps.ametro.ui.widgets.MapSelectionIndicatorsWidget
import io.github.romangolovanov.apps.ametro.ui.widgets.MapTopPanelWidget
import io.github.romangolovanov.apps.ametro.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Arrays
import java.util.HashSet

class Map : AppCompatActivity(),
    INavigationControllerListener,
    MapBottomPanelWidget.IMapBottomPanelEventListener,
    MapSelectionIndicatorsWidget.IMapSelectionEventListener {

    // ActivityResultLaunchers replace startActivityForResult
    private val openMapsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val localMapCatalogManager = app.mapCatalogProvider
            val mapInfo = localMapCatalogManager.findMapByName(data.getStringExtra(Constants.MAP_PATH)!!)
            val mapContainer = MapContainer(localMapCatalogManager, mapInfo, settingsProvider.preferredMapLanguage)
            loadMap(mapContainer, DEFAULT_SCHEME, null)
        }
    }

    private val openSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* settings don't return data */ }

    private val openStationDetailsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* no result needed */ }

    private lateinit var navigationController: NavigationController
    private var container: MapContainer? = null
    private lateinit var testMenuOptionsProcessor: TestMenuOptionsProcessor
    private var enabledTransportsSet: MutableSet<String> = HashSet()
    private var schemeName: String? = null
    private var currentDelay: MapDelay? = null

    private lateinit var mapContainerView: ViewGroup
    private lateinit var mapPanelView: View

    private var mapView: MultiTouchMapView? = null
    private lateinit var app: ApplicationEx
    private lateinit var settingsProvider: ApplicationSettingsProvider
    private var scheme: MapScheme? = null

    private lateinit var mapTopPanel: MapTopPanelWidget
    private lateinit var mapBottomPanel: MapBottomPanelWidget
    private lateinit var mapSelectionIndicators: MapSelectionIndicatorsWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)
        mapContainerView = findViewById(R.id.map_container)
        mapPanelView = findViewById(R.id.map_panel)
        mapTopPanel = MapTopPanelWidget(findViewById(R.id.map_top_panel))
        mapBottomPanel = MapBottomPanelWidget(findViewById(R.id.map_bottom_panel), this)
        mapSelectionIndicators = MapSelectionIndicatorsWidget(
            this,
            findViewById(R.id.begin_indicator),
            findViewById(R.id.end_indicator)
        )

        app = ApplicationEx.getInstance(this)
        settingsProvider = app.applicationSettingsProvider

        findViewById<View>(R.id.map_empty_panel).setOnClickListener { onOpenMaps() }

        testMenuOptionsProcessor = TestMenuOptionsProcessor(this)
        navigationController = NavigationController(
            this,
            this,
            TransportIconsProvider(this),
            app.countryFlagProvider,
            app.localizedMapInfoProvider
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    navigationController.isDrawerOpen -> navigationController.closeDrawer()
                    mapBottomPanel.isOpened -> mapBottomPanel.hide()
                    mapSelectionIndicators.hasSelection() -> mapSelectionIndicators.clearSelection()
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        val begin = mapSelectionIndicators.beginStation
        val end = mapSelectionIndicators.endStation
        app.setSelectedStations(begin?.uid, end?.uid)
        mapView?.let { app.centerPositionAndScale = it.centerPositionAndScale }
    }

    override fun onResume() {
        super.onResume()
        initMapViewState()
        if (mapView != null && app.centerPositionAndScale != null) {
            val data = app.centerPositionAndScale!!
            mapView!!.setCenterPositionAndScale(data.first, data.second, false)

            val beginUid = app.selectedBeginUid
            val endUid = app.selectedEndUid
            if (beginUid != null) {
                scheme?.let { s -> ModelUtil.findStationByUid(s, beginUid.toLong())?.let {
                    mapSelectionIndicators.setBeginStation(it.second)
                }}
            }
            if (endUid != null) {
                scheme?.let { s -> ModelUtil.findStationByUid(s, endUid.toLong())?.let {
                    mapSelectionIndicators.setEndStation(it.second)
                }}
            }
            if (mapSelectionIndicators.hasSelection()) {
                onRouteSelectionComplete(
                    mapSelectionIndicators.beginStation,
                    mapSelectionIndicators.endStation
                )
            }
        }
        if (mapView == null) {
            val currentMapFileName = settingsProvider.currentMapFileName
            if (currentMapFileName != null) {
                val mapCatalogProvider = app.mapCatalogProvider
                val currentMap = mapCatalogProvider.mapCatalog.findMap(currentMapFileName)
                if (currentMap != null) {
                    loadMap(
                        MapContainer(mapCatalogProvider, currentMap, settingsProvider.preferredMapLanguage),
                        DEFAULT_SCHEME,
                        null
                    )
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navigationController.onPostCreate()
    }

    override fun onConfigurationChanged(@NonNull newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navigationController.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(@NonNull item: MenuItem): Boolean =
        navigationController.onOptionsItemSelected(item)
                || testMenuOptionsProcessor.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)

        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem.actionView as SearchView
        var selectedStation: MapSchemeStation? = null

        searchView.isSubmitButtonEnabled = false
        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))
        searchView.setOnCloseListener {
            selectedStation = null
            true
        }
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int) = true
            override fun onSuggestionClick(position: Int): Boolean {
                selectedStation = (searchView.suggestionsAdapter as StationSearchAdapter).getStation(position)
                searchView.setQuery(selectedStation!!.displayName, true)
                return true
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val currentScheme = scheme ?: return true
                val station = selectedStation ?: return true
                val stationInfo = ModelUtil.findStationByUid(currentScheme, station.uid.toLong())
                if (stationInfo != null) {
                    val stationInformation = container?.findStationInformation(
                        stationInfo.first.name, stationInfo.second.name
                    )
                    val p = PointF(station.position!!.x, station.position!!.y)
                    mapView?.setCenterPositionAndScale(p, mapView!!.scale, true)
                    mapBottomPanel.show(
                        stationInfo.first,
                        stationInfo.second,
                        stationInformation?.mapFilePath != null
                    )
                    searchMenuItem.collapseActionView()
                }
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                val currentScheme = scheme ?: return true
                searchView.suggestionsAdapter = StationSearchAdapter.createFromMapScheme(this@Map, currentScheme, query)
                return true
            }
        })
        return true
    }

    /** Coroutine-based map loading — replaces AsyncTask. */
    private fun loadMap(mapContainer: MapContainer, schemeName: String, enabledTransports: Array<String>?) {
        lifecycleScope.launch {
            val start = System.currentTimeMillis()
            val error = runCatching {
                withContext(Dispatchers.IO) {
                    mapContainer.loadSchemeWithTransports(schemeName, null)
                }
            }.exceptionOrNull()

            if (error == null) {
                val elapsed = System.currentTimeMillis() - start
                onMapLoadComplete(mapContainer, schemeName, enabledTransports, elapsed)
            } else {
                onMapLoadFailed(mapContainer, schemeName, enabledTransports, error)
            }
        }
    }

    private fun onMapLoadComplete(container: MapContainer, schemeName: String, enabledTransports: Array<String>?, time: Long) {
        app.setCurrentMapViewState(container, schemeName, enabledTransports)
        initMapViewState()
        settingsProvider.setCurrentMap(container.mapInfo.fileName)
    }

    private fun initMapViewState() {
        if (app.container == null || app.container?.mapInfo == null) {
            app.clearCurrentMapViewState()
            navigationController.setNavigation(null, null, null, null)
            mapPanelView.visibility = View.GONE
            return
        }

        container = app.container
        schemeName = app.schemeName
        scheme = container!!.getScheme(schemeName!!)

        enabledTransportsSet = HashSet(
            Arrays.asList(*(app.enabledTransports ?: container!!.getScheme(schemeName!!)!!.defaultTransports))
        )

        navigationController.setNavigation(container, schemeName, app.enabledTransports, currentDelay)
        mapPanelView.visibility = View.VISIBLE
        mapSelectionIndicators.clearSelection()

        mapView = MultiTouchMapView(this, container, schemeName, mapSelectionIndicators)
        mapView!!.setOnClickListener {
            val stationInfo = scheme?.let { s -> ModelUtil.findTouchedStation(s, mapView!!.touchPoint) }
            if (stationInfo != null) {
                val stationInformation = container!!.findStationInformation(
                    stationInfo.first.name, stationInfo.second.name
                )
                mapBottomPanel.show(
                    stationInfo.first,
                    stationInfo.second,
                    stationInformation?.mapFilePath != null
                )
            } else {
                mapBottomPanel.hide()
                mapSelectionIndicators.clearSelection()
                onRouteSelectionCleared()
            }
        }

        mapContainerView.removeAllViews()
        mapContainerView.addView(mapView)
        mapView!!.requestFocus()
    }

    private fun onMapLoadFailed(container: MapContainer, schemeName: String, enabledTransports: Array<String>?, reason: Throwable) {
        Toast.makeText(this, getString(R.string.msg_map_loading_failed, reason.message), Toast.LENGTH_LONG).show()
        Log.e(Constants.LOG, "Map load failed due exception: ${reason.message}", reason)
    }

    override fun onOpenMaps(): Boolean {
        mapBottomPanel.hide()
        openMapsLauncher.launch(Intent(this, MapList::class.java))
        return true
    }

    override fun onOpenSettings(): Boolean {
        mapBottomPanel.hide()
        openSettingsLauncher.launch(Intent(this, SettingsList::class.java))
        return true
    }

    override fun onOpenAbout(): Boolean {
        startActivity(Intent(this, About::class.java))
        return false
    }

    override fun onChangeScheme(schemeName: String): Boolean {
        mapBottomPanel.hide()
        loadMap(container!!, schemeName, enabledTransportsSet.toTypedArray())
        return true
    }

    override fun onToggleTransport(transportName: String, checked: Boolean): Boolean {
        return try {
            if (checked) {
                enabledTransportsSet.add(transportName)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        container!!.loadSchemeWithTransports(
                            schemeName!!, enabledTransportsSet.toTypedArray()
                        )
                    }
                }
            } else {
                enabledTransportsSet.remove(transportName)
            }
            true
        } catch (e: MapSerializationException) {
            false
        }
    }

    override fun onDelayChanged(delay: MapDelay): Boolean {
        currentDelay = delay
        return true
    }

    override fun onShowMapDetail(line: MapSchemeLine, station: MapSchemeStation) {
        val intent = Intent(this, StationDetails::class.java).apply {
            putExtra(Constants.LINE_NAME, line.name)
            putExtra(Constants.STATION_NAME, station.name)
            putExtra(Constants.STATION_UID, station.uid)
        }
        openStationDetailsLauncher.launch(intent)
    }

    override fun onSelectBeginStation(line: MapSchemeLine, station: MapSchemeStation) {
        mapBottomPanel.hide()
        mapSelectionIndicators.setBeginStation(station)
    }

    override fun onSelectEndStation(line: MapSchemeLine, station: MapSchemeStation) {
        mapBottomPanel.hide()
        mapSelectionIndicators.setEndStation(station)
    }

    override fun onRouteSelectionComplete(begin: MapSchemeStation?, end: MapSchemeStation?) {
        val routes = MapRouteProvider.findRoutes(
            MapRouteQueryParameters(
                container,
                enabledTransportsSet,
                currentDelayIndex,
                mapSelectionIndicators.beginStation!!.uid,
                mapSelectionIndicators.endStation!!.uid
            )
        )

        if (routes.isEmpty()) {
            mapView?.highlightsElements(null)
            mapTopPanel.hide()
            Toast.makeText(
                this,
                String.format(getString(R.string.msg_no_route_found), begin?.displayName, end?.displayName),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        mapView?.highlightsElements(RouteUtils.convertRouteToSchemeObjectIds(routes[0], scheme))
        mapTopPanel.show(
            String.format(
                getString(R.string.msg_from_to),
                begin?.displayName,
                end?.displayName,
                StringUtils.humanReadableTime(routes[0].delay)
            )
        )
    }

    override fun onRouteSelectionCleared() {
        mapView?.highlightsElements(null)
        mapTopPanel.hide()
    }

    private val currentDelayIndex: Int?
        get() {
            val delays = container?.metadata?.delays ?: return null
            if (delays.isEmpty()) return null
            delays.forEachIndexed { index, delay -> if (delay == currentDelay) return index }
            return 0
        }

    companion object {
        private const val DEFAULT_SCHEME = "metro"
    }
}
