package io.github.romangolovanov.apps.ametro.model;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import io.github.romangolovanov.apps.ametro.app.Constants;
import io.github.romangolovanov.apps.ametro.catalog.MapCatalogProvider;
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo;
import io.github.romangolovanov.apps.ametro.model.entities.MapLocale;
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata;
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeSegment;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeTransfer;
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportLine;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportSegment;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportTransfer;
import io.github.romangolovanov.apps.ametro.model.serialization.MapProvider;
import io.github.romangolovanov.apps.ametro.model.serialization.MapSerializationException;

public class MapContainer {

    private final MapCatalogProvider catalogManager;
    private final MapInfo mapInfo;
    private MapLocale locale;
    private MapMetadata metadata;
    private MapStationInformation[] stations;
    private HashMap<String, MapTransportScheme> transports;
    private HashMap<String, MapScheme> schemes;

    private final String preferredLanguage;

    public MapContainer(MapCatalogProvider catalogManager, MapInfo mapInfo, String preferredLanguage) {
        this.catalogManager = catalogManager;
        this.mapInfo = mapInfo;
        this.preferredLanguage = preferredLanguage;
    }

    public void loadSchemeWithTransports(String schemeName, String[] enabledTransports) throws MapSerializationException {
        try (var mapProvider = catalogManager.getMapProvider(mapInfo)) {
            if (metadata == null) {
                var texts = mapProvider.getTextsMap(suggestLanguage(mapProvider.getSupportedLocales(), preferredLanguage));
                var allTexts = mapProvider.getAllTextsMap();
                locale = new MapLocale(texts, allTexts);
                metadata = mapProvider.getMetadata(locale);
                stations = mapProvider.getStationInformation();
                schemes = new HashMap<>();
                transports = new HashMap<>();
            }
            MapScheme scheme = loadSchemeFile(mapProvider, schemeName, locale);
            loadTransportFiles(mapProvider, enabledTransports != null ? enabledTransports : scheme.getDefaultTransports());
        } catch (MapSerializationException e) {
            Log.e(Constants.LOG, "Map unpacking failed", e);
            throw e;
        } catch (Exception e) {
            Log.e(Constants.LOG, "Map loading failed", e);
            throw new MapSerializationException(e);
        }
    }

    public MapInfo getMapInfo() {
        return mapInfo;
    }

    public MapMetadata getMetadata() {
        return metadata;
    }

    public MapScheme getScheme(String schemeName) {
        return schemes.get(schemeName);
    }

    public MapTransportScheme[] getTransportSchemes(Collection<String> transportNames) {
        List<MapTransportScheme> result = new ArrayList<>();
        for (String name : transportNames) {
            MapTransportScheme transport = transports.get(name);
            if (transport == null) {
                throw new AssertionError("Transport scheme " + name + " not loaded");
            }
            result.add(transport);
        }
        return result.toArray(new MapTransportScheme[0]);
    }

    public MapStationInformation findStationInformation(String lineName, String stationName) {
        for (final MapStationInformation stationScheme : stations) {
            if (stationScheme.getLine().equals(lineName) && stationScheme.getStation().equals(stationName)) {
                return stationScheme;
            }
        }
        return null;
    }

    public MapSchemeStation findSchemeStation(String schemeName, String lineName, String stationName) {
        for (final MapSchemeLine line : getScheme(schemeName).getLines()) {
            if (!line.getName().equals(lineName)) {
                continue;
            }
            for (final MapSchemeStation stationScheme : line.getStations()) {
                if (stationScheme.getName().equals(stationName)) {
                    return stationScheme;
                }
            }
        }
        return null;
    }

    public String loadStationMap(String mapFilePath) throws IOException {
        try (var mapProvider = catalogManager.getMapProvider(mapInfo)) {
            return mapProvider.getFileContent(mapFilePath);
        } catch (Exception e) {
            Log.e(Constants.LOG, "Map station scheme file loading failed", e);
            throw e;
        }
    }

    public int getMaxStationUid() {
        int max = 0;
        for (MapTransportScheme transport : transports.values()) {
            for (MapTransportLine line : transport.getLines()) {
                for (MapTransportSegment segment : line.getSegments()) {
                    max = Math.max(max, Math.max(segment.getFrom(), segment.getTo()));
                }
            }
            for (MapTransportTransfer transfer : transport.getTransfers()) {
                max = Math.max(max, Math.max(transfer.getFrom(), transfer.getTo()));
            }
        }
        for (MapScheme scheme : schemes.values()) {
            for (MapSchemeLine line : scheme.getLines()) {
                for (MapSchemeSegment segment : line.getSegments()) {
                    max = Math.max(max, Math.max(segment.getFrom(), segment.getTo()));
                }
            }
            for (MapSchemeTransfer transfer : scheme.getTransfers()) {
                max = Math.max(max, Math.max(transfer.getFrom(), transfer.getTo()));
            }
        }
        return max;
    }

    private static String suggestLanguage(String[] supportedLanguages, String preferredLanguage) {
        List<String> locales = Arrays.asList(supportedLanguages);
        if (!locales.contains(preferredLanguage)) {
            return locales.get(0);
        }
        return preferredLanguage;
    }

    private MapScheme loadSchemeFile(MapProvider mapProvider, String schemeName, MapLocale locale)
            throws IOException, MapSerializationException {
        MapScheme scheme = schemes.get(schemeName);
        if (scheme == null) {
            scheme = mapProvider.getScheme(metadata.getScheme(schemeName).getFileName(), locale);
            schemes.put(scheme.getName(), scheme);
        }
        return scheme;
    }

    private void loadTransportFiles(MapProvider mapProvider, String[] enabledTransports)
            throws IOException, MapSerializationException {
        for (String transportName : enabledTransports) {
            loadTransport(mapProvider, transportName);
        }
    }

    private void loadTransport(MapProvider mapProvider, String transportName)
            throws IOException {
        MapTransportScheme transport = transports.get(transportName);
        if (transport == null) {
            transport = mapProvider.getTransportScheme(metadata.getTransport(transportName).getFileName());
            transports.put(transport.getName(), transport);
        }
    }
}
