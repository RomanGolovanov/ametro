package org.ametro.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PointF;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.util.Pair;

import org.ametro.R;
import org.ametro.catalog.MapCatalogManager;
import org.ametro.catalog.RemoteMapCatalogProvider;
import org.ametro.catalog.localization.MapInfoLocalizationProvider;
import org.ametro.catalog.service.IMapServiceCache;
import org.ametro.catalog.service.MapServiceCache;
import org.ametro.catalog.service.ServiceTransport;
import org.ametro.model.MapContainer;
import org.ametro.providers.IconProvider;
import org.ametro.utils.Lazy;

public class ApplicationEx extends Application {

    private Lazy<ApplicationSettingsProvider> appSettingsProvider;
    private Lazy<IconProvider> countryFlagProvider;
    private Lazy<RemoteMapCatalogProvider> remoteMapCatalogProvider;
    private Lazy<IMapServiceCache> mapServiceCache;
    private Lazy<MapCatalogManager> localMapCatalogManager;
    private Lazy<MapInfoLocalizationProvider> localizedMapInfoProvider;

    private MapContainer container;
    private String schemeName;
    private String[] enabledTransports;
    private Pair<PointF, Float> centerPositionAndScale;

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

        appSettingsProvider = new Lazy<>(new Lazy.IFactory<ApplicationSettingsProvider>() {
            @Override
            public ApplicationSettingsProvider create() {
                return new ApplicationSettingsProvider(ApplicationEx.this);
            }
        });

        countryFlagProvider = new Lazy<>(new Lazy.IFactory<IconProvider>() {
            @Override
            public IconProvider create() {
                return new IconProvider(
                        ApplicationEx.this,
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_country),
                        "country_icons");
            }
        });

        mapServiceCache = new Lazy<>(new Lazy.IFactory<IMapServiceCache>() {
            @Override
            public IMapServiceCache create() {
                return new MapServiceCache(
                        new ServiceTransport(),
                        Constants.MAP_SERVICE_URI,
                        getFilesDir(),
                        getApplicationSettingsProvider().getDefaultLanguage(),
                        Constants.MAP_EXPIRATION_PERIOD_MILLISECONDS);
            }
        });

        localizedMapInfoProvider = new Lazy<>(new Lazy.IFactory<MapInfoLocalizationProvider>() {
            @Override
            public MapInfoLocalizationProvider create() {
                return new MapInfoLocalizationProvider(mapServiceCache.getInstance());
            }
        });

        remoteMapCatalogProvider = new Lazy<>(new Lazy.IFactory<RemoteMapCatalogProvider>() {
            @Override
            public RemoteMapCatalogProvider create() {
                return new RemoteMapCatalogProvider(
                        Constants.MAP_SERVICE_URI,
                        mapServiceCache.getInstance(),
                        localizedMapInfoProvider.getInstance());
            }
        });

        localMapCatalogManager = new Lazy<>(new Lazy.IFactory<MapCatalogManager>() {
            @Override
            public MapCatalogManager create() {
                return new MapCatalogManager(getFilesDir(), getLocalizedMapInfoProvider());
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

    public RemoteMapCatalogProvider getRemoteMapCatalogProvider() {
        return remoteMapCatalogProvider.getInstance();
    }

    public MapCatalogManager getLocalMapCatalogManager() {
        return localMapCatalogManager.getInstance();
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
}
