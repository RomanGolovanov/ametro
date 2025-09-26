package org.ametro.ng.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PointF;
import android.util.Pair;

import androidx.core.content.ContextCompat;
import androidx.loader.content.AsyncTaskLoader;

import org.ametro.ng.R;
import org.ametro.ng.catalog.MapCatalogProvider;
import org.ametro.ng.catalog.MapInfoLocalizationProvider;
import org.ametro.ng.model.MapContainer;
import org.ametro.ng.providers.IconProvider;
import org.ametro.ng.utils.Lazy;

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

    public static ApplicationEx getInstance(Context applicationContext) {
        return (ApplicationEx) applicationContext;
    }

    public static ApplicationEx getInstance(AsyncTaskLoader<?> loader) {
        return getInstance(loader.getContext().getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        countryFlagProvider = new Lazy<>(new Lazy.IFactory<IconProvider>() {
            @Override
            public IconProvider create() {
                return new IconProvider(
                        ApplicationEx.this,
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_country),
                        "country_icons");
            }
        });

        localizedMapInfoProvider = new Lazy<>(new Lazy.IFactory<MapInfoLocalizationProvider>() {
            @Override
            public MapInfoLocalizationProvider create() {
                return new MapInfoLocalizationProvider(getApplicationContext(),getApplicationSettingsProvider());
            }
        });

        appSettingsProvider = new Lazy<>(new Lazy.IFactory<ApplicationSettingsProvider>() {
            @Override
            public ApplicationSettingsProvider create() {
                return new ApplicationSettingsProvider(ApplicationEx.this);
            }
        });

        mapCatalogProvider = new Lazy<>(new Lazy.IFactory<MapCatalogProvider>() {
            @Override
            public MapCatalogProvider create() {
                return new MapCatalogProvider(getApplicationContext(), getLocalizedMapInfoProvider());
            }
        });

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
