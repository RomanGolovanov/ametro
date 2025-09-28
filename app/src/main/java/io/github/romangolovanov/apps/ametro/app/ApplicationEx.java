package io.github.romangolovanov.apps.ametro.app;

import android.app.Activity;
import android.app.Application;
import android.graphics.PointF;
import android.util.Pair;

import androidx.core.content.ContextCompat;

import io.github.romangolovanov.apps.ametro.R;
import io.github.romangolovanov.apps.ametro.catalog.MapCatalogProvider;
import io.github.romangolovanov.apps.ametro.catalog.MapInfoLocalizationProvider;
import io.github.romangolovanov.apps.ametro.model.MapContainer;
import io.github.romangolovanov.apps.ametro.providers.IconProvider;
import io.github.romangolovanov.apps.ametro.utils.Lazy;

public class ApplicationEx extends Application {

    private Lazy<ApplicationSettingsProvider> appSettingsProvider;
    private Lazy<IconProvider> countryFlagProvider;
    private Lazy<MapCatalogProvider> mapCatalogProvider;
    private Lazy<MapInfoLocalizationProvider> localizedMapInfoProvider;

    private MapContainer container;
    private String schemeName;
    private String[] enabledTransports;
    private Pair<PointF, Float> centerPositionAndScale;

    private Integer selectedBeginStationUid;

    private Integer selectedEndStationUid;

    public static ApplicationEx getInstance(Activity activity) {
        return (ApplicationEx) activity.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        countryFlagProvider = new Lazy<>(() -> new IconProvider(
                ApplicationEx.this,
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_country),
                "country_icons"));

        localizedMapInfoProvider = new Lazy<>(() -> new MapInfoLocalizationProvider(getApplicationContext(),getApplicationSettingsProvider()));

        appSettingsProvider = new Lazy<>(() -> new ApplicationSettingsProvider(ApplicationEx.this));

        mapCatalogProvider = new Lazy<>(() -> new MapCatalogProvider(getApplicationContext(), getLocalizedMapInfoProvider()));

    }

    public MapInfoLocalizationProvider getLocalizedMapInfoProvider(){
        return localizedMapInfoProvider.getInstance();
    }

    public ApplicationSettingsProvider getApplicationSettingsProvider() {
        return appSettingsProvider.getInstance();
    }

    public IconProvider getCountryFlagProvider() {
        return countryFlagProvider.getInstance();
    }

    public MapCatalogProvider getMapCatalogProvider() {
        return mapCatalogProvider.getInstance();
    }

    public void setCurrentMapViewState(MapContainer container, String schemeName, String[] enabledTransports) {
        this.container = container;
        this.schemeName = schemeName;
        this.enabledTransports = enabledTransports;
    }

    public void clearCurrentMapViewState() {
        this.container = null;
        this.schemeName = null;
        this.enabledTransports = null;
    }

    public MapContainer getContainer() {
        return container;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public String[] getEnabledTransports() {
        return enabledTransports;
    }

    public void setCenterPositionAndScale(Pair<PointF, Float> centerPositionAndScale) {
        this.centerPositionAndScale = centerPositionAndScale;
    }

    public Pair<PointF, Float> getCenterPositionAndScale() {
        return centerPositionAndScale;
    }

    public void setSelectedStations(Integer beginUid, Integer endUid) {
        this.selectedBeginStationUid = beginUid;
        this.selectedEndStationUid = endUid;
    }

    public Integer getSelectedBeginUid() {
        return selectedBeginStationUid;
    }

    public Integer getSelectedEndUid() {
        return selectedEndStationUid;
    }
}
