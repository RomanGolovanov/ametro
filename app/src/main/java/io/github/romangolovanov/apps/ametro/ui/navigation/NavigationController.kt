package io.github.romangolovanov.apps.ametro.ui.navigation

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.catalog.MapInfoLocalizationProvider
import io.github.romangolovanov.apps.ametro.catalog.entities.TransportTypeHelper
import io.github.romangolovanov.apps.ametro.model.MapContainer
import io.github.romangolovanov.apps.ametro.model.entities.MapDelay
import io.github.romangolovanov.apps.ametro.model.entities.MapDelayType
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata
import io.github.romangolovanov.apps.ametro.providers.IconProvider
import io.github.romangolovanov.apps.ametro.providers.TransportIconsProvider
import io.github.romangolovanov.apps.ametro.ui.navigation.adapter.NavigationDrawerAdapter
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationCheckBoxItem
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationHeader
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationItem
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationSplitter
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationSubHeader
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationTextItem
import io.github.romangolovanov.apps.ametro.ui.navigation.helpers.DelayResources
import io.github.romangolovanov.apps.ametro.utils.ListUtils
import io.github.romangolovanov.apps.ametro.utils.StringUtils

class NavigationController(
    activity: AppCompatActivity,
    private val listener: INavigationControllerListener,
    private val transportIconProvider: TransportIconsProvider,
    private val countryIconProvider: IconProvider,
    private val localizationProvider: MapInfoLocalizationProvider
) : AdapterView.OnItemClickListener {

    companion object {
        private const val SCHEME_TYPE_OTHER = "OTHER"
        private const val SCHEME_TYPE_ROOT = "ROOT"

        private const val OPEN_MAPS_ACTION = 1
        private const val OPEN_SETTINGS_ACTION = 2
        private const val OPEN_ABOUT_ACTION = 3
        private const val TOGGLE_TRANSPORT_ACTION = 4
        private const val CHANGE_DELAY_ACTION = 5
        private const val OPEN_SCHEME_ACTION = 6
    }

    private val drawerMenuAdapter: NavigationDrawerAdapter
    private val drawerToggle: ActionBarDrawerToggle
    private val drawerLayout: DrawerLayout
    private val resources: Resources
    private val context: AppCompatActivity

    private var delayItems: Array<NavigationItem> = emptyArray()
    private var transportNameLocalizations: Map<String, String> = emptyMap()

    init {
        val toolbar: Toolbar = activity.findViewById(R.id.toolbar)
        activity.setSupportActionBar(toolbar)

        context = activity
        resources = activity.resources

        createTransportsLocalizationTable()

        drawerMenuAdapter = NavigationDrawerAdapter(activity, createNavigationItems(null, null, null, null))

        val drawerMenuList: ListView = activity.findViewById(R.id.drawer)
        drawerMenuList.adapter = drawerMenuAdapter
        drawerMenuList.setOnItemClickListener(this)
        drawerMenuList.choiceMode = ListView.CHOICE_MODE_NONE

        drawerLayout = activity.findViewById(R.id.drawer_layout)
        drawerToggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true

        val actionBar: ActionBar? = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
    }

    fun setNavigation(container: MapContainer?, schemeName: String?, enabledTransports: Array<String>?, currentDelay: MapDelay?) {
        drawerMenuAdapter.setNavigationItems(createNavigationItems(container, schemeName, enabledTransports, currentDelay))
    }

    fun onPostCreate() {
        drawerToggle.syncState()
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        drawerToggle.onConfigurationChanged(newConfig)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerToggle.onOptionsItemSelected(item)
    }

    val isDrawerOpen: Boolean get() = drawerLayout.isDrawerOpen(GravityCompat.START)

    fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = drawerMenuAdapter.getItem(position)
        if (!item.enabled) return
        var complete = false
        when (item.action) {
            OPEN_MAPS_ACTION -> complete = listener.onOpenMaps()
            OPEN_SETTINGS_ACTION -> complete = listener.onOpenSettings()
            OPEN_ABOUT_ACTION -> complete = listener.onOpenAbout()
            OPEN_SCHEME_ACTION -> complete = listener.onChangeScheme(item.source as String)
            TOGGLE_TRANSPORT_ACTION -> {
                val checkbox = drawerMenuAdapter.getItem(position) as NavigationCheckBoxItem
                val newCheckedState = !checkbox.checked
                if (listener.onToggleTransport(item.source as String, newCheckedState)) {
                    checkbox.checked = newCheckedState
                    drawerMenuAdapter.notifyDataSetChanged()
                }
            }
            CHANGE_DELAY_ACTION -> {
                for (delayItem in delayItems) {
                    delayItem.selected = (delayItem == item)
                }
                drawerMenuAdapter.notifyDataSetChanged()
                complete = listener.onDelayChanged(item.source as MapDelay)
            }
        }
        if (complete && isDrawerOpen) {
            closeDrawer()
        }
    }

    private fun createNavigationItems(
        container: MapContainer?,
        schemeName: String?,
        enabledTransports: Array<String>?,
        currentDelay: MapDelay?
    ): Array<NavigationItem> {
        val items = mutableListOf<NavigationItem>(createHeaderNavigationItem(container))

        items.add(NavigationSubHeader(resources.getString(R.string.nav_options), arrayOf(
            NavigationTextItem(OPEN_MAPS_ACTION, ContextCompat.getDrawable(context, R.drawable.ic_public_black_18dp), resources.getString(R.string.nav_select_map)),
            NavigationTextItem(OPEN_ABOUT_ACTION, ContextCompat.getDrawable(context, R.drawable.ic_info_black_24dp), resources.getString(R.string.nav_about)),
            NavigationSplitter()
        )))

        delayItems = createDelayNavigationItems(container, currentDelay)
        if (delayItems.size > 1) {
            items.add(NavigationSubHeader(NavigationItem.INVALID_ACTION, resources.getString(R.string.nav_delays), delayItems))
            items.add(NavigationSplitter())
        }

        val transportItems = createTransportNavigationItems(container, schemeName, enabledTransports)
        if (transportItems.size > 1) {
            items.add(NavigationSubHeader(NavigationItem.INVALID_ACTION, resources.getString(R.string.nav_using), transportItems))
            items.add(NavigationSplitter())
        }

        val schemeItems = createSchemeNavigationItems(container, schemeName)
        if (schemeItems.size > 1) {
            items.add(NavigationSubHeader(NavigationItem.INVALID_ACTION, resources.getString(R.string.nav_schemes), schemeItems))
            items.add(NavigationSplitter())
        }

        return items.toTypedArray()
    }

    private fun createHeaderNavigationItem(container: MapContainer?): NavigationItem {
        if (container == null) {
            return NavigationHeader(resources.getString(R.string.nav_no_city))
        }
        val meta = container.metadata ?: return NavigationHeader(resources.getString(R.string.nav_no_city))
        return NavigationHeader(
            countryIconProvider.getIcon(localizationProvider.getCountryIsoCode(meta.cityId)),
            localizationProvider.getCityName(meta.cityId),
            localizationProvider.getCountryName(meta.cityId),
            meta.comments,
            transportIconProvider.getTransportIcons(
                TransportTypeHelper.parseTransportTypes(meta.transportTypes))
        )
    }

    private fun createSchemeNavigationItems(container: MapContainer?, schemeName: String?): Array<NavigationItem> {
        if (container == null) return emptyArray()
        val meta = container.metadata ?: return emptyArray()

        val schemeMetadataList = ListUtils.filter(meta.schemes.values) { !it.typeName.equals(SCHEME_TYPE_OTHER) }
            .toMutableList()
        schemeMetadataList.sortWith(SchemeNavigationListComparator())

        val schemes = mutableListOf<NavigationItem>()
        for (schemeMeta in schemeMetadataList) {
            var icon: Drawable? = null
            if (schemeMeta.typeName == SCHEME_TYPE_ROOT) {
                val defaultTransport = ListUtils.firstOrDefault(
                    schemeMeta.defaultTransports.toList(),
                    ListUtils.firstOrDefault(schemeMeta.defaultTransports.toList(), null)
                )
                if (defaultTransport != null) {
                    icon = transportIconProvider.getTransportIcon(
                        TransportTypeHelper.parseTransportType(
                            meta.getTransport(defaultTransport).typeName
                        )
                    )
                }
            }

            val item = NavigationTextItem(
                OPEN_SCHEME_ACTION,
                icon,
                schemeMeta.displayName ?: schemeMeta.name,
                true,
                schemeMeta.name
            )

            if (schemeMeta.name == schemeName) {
                item.selected = true
                item.enabled = false
            }

            schemes.add(item)
        }
        return schemes.toTypedArray()
    }

    private fun createTransportNavigationItems(
        container: MapContainer?,
        schemeName: String?,
        enabledTransports: Array<String>?
    ): Array<NavigationItem> {
        if (container == null || schemeName == null) return emptyArray()
        val meta = container.metadata ?: return emptyArray()

        val defaultTransports = container.getScheme(schemeName)?.defaultTransports ?: emptyArray()
        val enabledTransportsSet = HashSet((enabledTransports ?: defaultTransports).toList())

        val transports = mutableListOf<NavigationItem>()
        for (name in meta.getScheme(schemeName).transports) {
            val transportSchemeMeta = meta.getTransport(name)
            var displayName = transportNameLocalizations[transportSchemeMeta.typeName.lowercase()]
            if (displayName == null) {
                displayName = "#" + transports.size
            }
            transports.add(NavigationCheckBoxItem(
                TOGGLE_TRANSPORT_ACTION,
                displayName,
                enabledTransportsSet.contains(name),
                transportSchemeMeta.name
            ))
        }
        return transports.toTypedArray()
    }

    private fun createDelayNavigationItems(container: MapContainer?, currentDelay: MapDelay?): Array<NavigationItem> {
        if (container == null) return emptyArray()
        val meta = container.metadata ?: return emptyArray()

        val delays = mutableListOf<NavigationItem>()
        var defaultDelayWasSet = false
        for (delay in meta.delays) {
            val item = NavigationTextItem(
                CHANGE_DELAY_ACTION,
                null,
                createDelayItemName(delay),
                true,
                delay
            )

            if (delay == currentDelay) {
                item.selected = true
                defaultDelayWasSet = true
            }

            delays.add(item)
        }

        if (delays.isNotEmpty() && !defaultDelayWasSet) {
            delays[0].selected = true
        }

        return delays.toTypedArray()
    }

    private fun createDelayItemName(delay: MapDelay): String {
        if (delay.delayType == MapDelayType.Custom) {
            return createDelayName(
                delay.displayName,
                resources.getString(DelayResources.getDelayWeekendTypeTextId(delay.weekdays)),
                delay.ranges
            )
        }
        return createDelayName(
            resources.getString(DelayResources.getDelayTypeTextId(delay.delayType)),
            resources.getString(DelayResources.getDelayWeekendTypeTextId(delay.weekdays)),
            null
        )
    }

    private fun createDelayName(name: String?, weekdayName: String?, ranges: Array<out Any>?): String {
        val sb = StringBuilder()
        if (name != null) sb.append(name)
        if (!StringUtils.isNullOrEmpty(weekdayName)) {
            sb.append(" [")
            sb.append(weekdayName)
            sb.append("]")
        }
        if (ranges != null) {
            sb.append(" [")
            for (range in ranges) {
                sb.append(range.toString())
                sb.append(',')
            }
            sb.setLength(sb.length - 1)
            sb.append("]")
        }
        return sb.toString().trim()
    }

    private fun createTransportsLocalizationTable() {
        val map = mutableMapOf<String, String>()
        val transportTypes = resources.getStringArray(R.array.transport_types)
        val transportTypeNames = resources.getStringArray(R.array.transport_type_names)
        for (i in transportTypes.indices) {
            map[transportTypes[i].lowercase()] = transportTypeNames[i]
        }
        transportNameLocalizations = map
    }
}
