package org.ametro.ui.navigation;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.ametro.R;
import org.ametro.catalog.entities.TransportTypeHelper;
import org.ametro.catalog.localization.MapInfoLocalizationProvider;
import org.ametro.model.MapContainer;
import org.ametro.model.entities.MapDelay;
import org.ametro.model.entities.MapDelayTimeRange;
import org.ametro.model.entities.MapDelayType;
import org.ametro.model.entities.MapDelayWeekdayType;
import org.ametro.model.entities.MapMetadata;
import org.ametro.providers.IconProvider;
import org.ametro.providers.TransportIconsProvider;
import org.ametro.ui.navigation.adapter.NavigationDrawerAdapter;
import org.ametro.ui.navigation.entities.NavigationCheckBoxItem;
import org.ametro.ui.navigation.entities.NavigationHeader;
import org.ametro.ui.navigation.entities.NavigationItem;
import org.ametro.ui.navigation.entities.NavigationSplitter;
import org.ametro.ui.navigation.entities.NavigationSubHeader;
import org.ametro.ui.navigation.entities.NavigationTextItem;
import org.ametro.ui.navigation.helpers.DelayResources;
import org.ametro.utils.ListUtils;
import org.ametro.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class NavigationController implements AdapterView.OnItemClickListener {

    private static final String SCHEME_TYPE_OTHER = "OTHER";
    private static final String SCHEME_TYPE_ROOT = "ROOT";

    private static final int OPEN_MAPS_ACTION = 1;
    private static final int OPEN_SETTINGS_ACTION = 2;
    private static final int OPEN_ABOUT_ACTION = 3;
    private static final int TOGGLE_TRANSPORT_ACTION = 4;
    private static final int CHANGE_DELAY_ACTION = 5;
    private static final int OPEN_SCHEME_ACTION = 6;


    private final INavigationControllerListener listener;
    private final TransportIconsProvider transportIconProvider;
    private final IconProvider countryIconProvider;
    private final MapInfoLocalizationProvider localizationProvider;
    private final NavigationDrawerAdapter drawerMenuAdapter;
    private final ActionBarDrawerToggle drawerToggle;
    private final DrawerLayout drawerLayout;
    private final Resources resources;
    private final AppCompatActivity context;

    private NavigationItem[] delayItems;

    private Map<String,String> transportNameLocalizations;

    public NavigationController(AppCompatActivity activity, INavigationControllerListener listener,
                                TransportIconsProvider transportIconProvider,
                                IconProvider countryIconProvider,
                                MapInfoLocalizationProvider localizationProvider) {


        this.listener = listener;
        this.transportIconProvider = transportIconProvider;
        this.countryIconProvider = countryIconProvider;
        this.localizationProvider = localizationProvider;

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        context = activity;
        resources = activity.getResources();

        createTransportsLocalizationTable();

        drawerMenuAdapter = new NavigationDrawerAdapter(activity, createNavigationItems(null, null, null, null));

        ListView drawerMenuList = (ListView) activity.findViewById(R.id.drawer);
        drawerMenuList.setAdapter(drawerMenuAdapter);
        drawerMenuList.setOnItemClickListener(this);
        drawerMenuList.setChoiceMode(ListView.CHOICE_MODE_NONE);

        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(true);

        final ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public void setNavigation(MapContainer container, String schemeName, String[] enabledTransports, MapDelay currentDelay) {
        drawerMenuAdapter.setNavigationItems(createNavigationItems(container, schemeName, enabledTransports, currentDelay));
    }

    public void onPostCreate() {
        drawerToggle.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item);
    }

    public boolean isDrawerOpen() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NavigationItem item = drawerMenuAdapter.getItem(position);
        if (!item.isEnabled()) {
            return;
        }
        boolean complete = false;
        switch (item.getAction()) {
            case OPEN_MAPS_ACTION:
                complete = listener.onOpenMaps();
                break;
            case OPEN_SETTINGS_ACTION:
                complete = listener.onOpenSettings();
                break;
            case OPEN_ABOUT_ACTION:
                complete = listener.onOpenAbout();
                break;

            case OPEN_SCHEME_ACTION:
                complete = listener.onChangeScheme((String) item.getSource());
                break;
            case TOGGLE_TRANSPORT_ACTION:
                NavigationCheckBoxItem checkbox = (NavigationCheckBoxItem) drawerMenuAdapter.getItem(position);
                boolean newCheckedState = !checkbox.isChecked();
                if (listener.onToggleTransport((String) item.getSource(), newCheckedState)) {
                    checkbox.setChecked(newCheckedState);
                    drawerMenuAdapter.notifyDataSetChanged();
                }
                break;
            case CHANGE_DELAY_ACTION:
                for(NavigationItem delayItem : delayItems){
                    if(delayItem == item){
                        delayItem.setSelected(true);
                    }else{
                        delayItem.setSelected(false);
                    }
                }
                drawerMenuAdapter.notifyDataSetChanged();
                complete = listener.onDelayChanged((MapDelay) item.getSource());
                break;
        }
        if (complete && isDrawerOpen()) {
            closeDrawer();
        }
    }

    private NavigationItem[] createNavigationItems(MapContainer container, String schemeName, String[] enabledTransports, MapDelay currentDelay) {

        ArrayList<NavigationItem> items = new ArrayList<>();
        items.addAll(Collections.singletonList(createHeaderNavigationItem(container)));

        items.add(new NavigationSubHeader(resources.getString(R.string.nav_options), new NavigationItem[]{
                new NavigationTextItem(OPEN_MAPS_ACTION, ContextCompat.getDrawable(context, R.drawable.ic_public_black_18dp), resources.getString(R.string.nav_select_map)),
                //new NavigationTextItem(OPEN_SETTINGS_ACTION, ContextCompat.getDrawable(context, R.drawable.ic_settings_black_18dp), resources.getString(R.string.nav_settings)),
                new NavigationTextItem(OPEN_ABOUT_ACTION, ContextCompat.getDrawable(context, R.drawable.ic_info_black_24dp), resources.getString(R.string.nav_about)),
                new NavigationSplitter()
        }));

        delayItems = createDelayNavigationItems(container, currentDelay);
        if (delayItems.length > 1) {
            items.add(new NavigationSubHeader(NavigationItem.INVALID_ACTION, resources.getString(R.string.nav_delays), delayItems));
            items.add(new NavigationSplitter());
        }

        NavigationItem[] transportItems = createTransportNavigationItems(container, schemeName, enabledTransports);
        if (transportItems.length > 1) {
            items.add(new NavigationSubHeader(NavigationItem.INVALID_ACTION, resources.getString(R.string.nav_using), transportItems));
            items.add(new NavigationSplitter());
        }

        NavigationItem[] schemeItems = createSchemeNavigationItems(container, schemeName);
        if (schemeItems.length > 1) {
            items.add(new NavigationSubHeader(NavigationItem.INVALID_ACTION, resources.getString(R.string.nav_schemes), schemeItems));
            items.add(new NavigationSplitter());
        }

        return items.toArray(new NavigationItem[items.size()]);
    }

    private NavigationItem createHeaderNavigationItem(MapContainer container) {
        if (container == null) {
            return new NavigationHeader(resources.getString(R.string.nav_no_city));
        }
        MapMetadata meta = container.getMetadata();
        return new NavigationHeader(
                countryIconProvider.getIcon(localizationProvider.getCountryIsoCode(meta.getCityId())),
                localizationProvider.getCityName(meta.getCityId()),
                localizationProvider.getCountryName(meta.getCityId()),
                meta.getComments(),
                transportIconProvider.getTransportIcons(
                        TransportTypeHelper.parseTransportTypes(meta.getTransportTypes()))
        );
    }

    private NavigationItem[] createSchemeNavigationItems(MapContainer container, String schemeName) {
        if (container == null) {
            return new NavigationItem[0];
        }
        MapMetadata meta = container.getMetadata();

        ArrayList<MapMetadata.Scheme> schemeMetadataList =
                new ArrayList<>(ListUtils.filter(meta.getSchemes().values(),
                        new ListUtils.IPredicate<MapMetadata.Scheme>() {
                            @Override
                            public boolean apply(MapMetadata.Scheme type) {
                                return !type.getTypeName().equals(SCHEME_TYPE_OTHER);
                            }
                        }));
        Collections.sort(schemeMetadataList, new SchemeNavigationListComparator());

        List<NavigationItem> schemes = new ArrayList<>();
        for (MapMetadata.Scheme schemeMeta : schemeMetadataList) {

            Drawable icon = null;
            if (schemeMeta.getTypeName().equals(SCHEME_TYPE_ROOT)) {

                String defaultTransport = ListUtils.firstOrDefault(
                        Arrays.asList(schemeMeta.getDefaultTransports()),
                        ListUtils.firstOrDefault(Arrays.asList(schemeMeta.getDefaultTransports()),
                                null));

                if(defaultTransport!=null){

                    icon = transportIconProvider.getTransportIcon(
                            TransportTypeHelper.parseTransportType(
                                    meta.getTransport(defaultTransport).getTypeName()));

                }
            }

            NavigationTextItem item = new NavigationTextItem(
                    OPEN_SCHEME_ACTION,
                    icon,
                    schemeMeta.getDisplayName(),
                    true,
                    schemeMeta.getName());

            if (schemeMeta.getName().equals(schemeName)) {
                item.setSelected(true);
                item.setEnabled(false);
            }

            schemes.add(item);
        }
        return schemes.toArray(new NavigationItem[schemes.size()]);
    }

    private NavigationItem[] createTransportNavigationItems(MapContainer container, String schemeName, String[] enabledTransports) {
        if (container == null) {
            return new NavigationItem[0];
        }
        MapMetadata meta = container.getMetadata();

        HashSet<String> enabledTransportsSet = new HashSet<>(
                Arrays.asList(enabledTransports != null
                        ? enabledTransports
                        : container.getScheme(schemeName).getDefaultTransports()));

        List<NavigationItem> transports = new ArrayList<>();
        for (String name : meta.getScheme(schemeName).getTransports()) {
            MapMetadata.TransportScheme transportSchemeMeta = meta.getTransport(name);
            String displayName = transportNameLocalizations.get(transportSchemeMeta.getTypeName().toLowerCase());
            if(displayName == null){
                displayName = "#" + transports.size();
            }
            transports.add(new NavigationCheckBoxItem(
                    TOGGLE_TRANSPORT_ACTION,
                    displayName,
                    enabledTransportsSet.contains(name),
                    transportSchemeMeta.getName()));
        }
        return transports.toArray(new NavigationItem[transports.size()]);
    }

    private NavigationItem[] createDelayNavigationItems(MapContainer container, MapDelay currentDelay) {
        if (container == null) {
            return new NavigationItem[0];
        }
        MapMetadata meta = container.getMetadata();

        List<NavigationItem> delays = new ArrayList<>();
        boolean defaultDelayWasSet = false;
        for (MapDelay delay : meta.getDelays()) {

            NavigationTextItem item = new NavigationTextItem(
                    CHANGE_DELAY_ACTION,
                    null,
                    createDelayItemName(delay),
                    true,
                    delay);

            if (delay == currentDelay) {
                item.setSelected(true);
                defaultDelayWasSet = true;
            }

            delays.add(item);
        }

        if(delays.size()>0 && !defaultDelayWasSet){
            delays.get(0).setSelected(true);
        }

        return delays.toArray(new NavigationItem[delays.size()]);
    }


    private String createDelayItemName(MapDelay delay){
        if(delay.getDelayType() == MapDelayType.Custom){
            return createDelayName(
                    delay.getDisplayName(),
                    resources.getString(DelayResources.getDelayWeekendTypeTextId(delay.getWeekdays())),
                    delay.getRanges());
        }
        return createDelayName(
                resources.getString(DelayResources.getDelayTypeTextId(delay.getDelayType())),
                resources.getString(DelayResources.getDelayWeekendTypeTextId(delay.getWeekdays())),
                null);
    }

    private String createDelayName(String name, String weekdayName, MapDelayTimeRange[] ranges){
        StringBuilder sb = new StringBuilder();
        if(name!=null){
            sb.append(name);
        }
        if(!StringUtils.isNullOrEmpty(weekdayName)){
            sb.append(" [");
            sb.append(weekdayName);
            sb.append("]");
        }
        if(ranges!=null){
            sb.append(" [");
            for(MapDelayTimeRange range : ranges){
                sb.append(range.toString());
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            sb.append("]");
        }

        return sb.toString().trim();
    }

    @Deprecated
    private void createTransportsLocalizationTable() {
        transportNameLocalizations = new HashMap<>();
        String[] transportTypes = resources.getStringArray(R.array.transport_types);
        String[] transportTypeNames = resources.getStringArray(R.array.transport_type_names);
        for(int i=0;i<transportTypes.length;i++){
            transportNameLocalizations.put(transportTypes[i].toLowerCase(), transportTypeNames[i]);
        }
    }


}

