package org.ametro.model;


import android.util.Log;

import org.ametro.app.Constants;
import org.ametro.model.entities.MapLocale;
import org.ametro.model.entities.MapMetadata;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeSegment;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.model.entities.MapSchemeTransfer;
import org.ametro.model.entities.MapStationInformation;
import org.ametro.model.entities.MapTransportLine;
import org.ametro.model.entities.MapTransportScheme;
import org.ametro.model.entities.MapTransportSegment;
import org.ametro.model.entities.MapTransportTransfer;
import org.ametro.model.serialization.GlobalIdentifierProvider;
import org.ametro.model.serialization.MapSerializationException;
import org.ametro.model.serialization.ZipArchiveMapProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MapContainer {

    private final File mapFile;
    private final GlobalIdentifierProvider identifierProvider = new GlobalIdentifierProvider();
    private MapLocale locale;
    private MapMetadata metadata;
    private MapStationInformation[] stations;
    private HashMap<String, MapTransportScheme> transports;
    private HashMap<String, MapScheme> schemes;

    private final String preferredLanguage;

    public MapContainer(File mapFile, String preferredLanguage) {
        this.mapFile = mapFile;
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isLoaded(String schemeName, String[] enabledTransports) {
        if (metadata == null || stations == null) {
            return false;
        }
        MapScheme scheme = schemes.get(schemeName);
        if (scheme == null) {
            return false;
        }
        if (enabledTransports == null) {
            enabledTransports = scheme.getDefaultTransports();
        }
        for (String transportName : enabledTransports) {
            if (transports.get(transportName) == null) {
                return false;
            }
        }
        return true;
    }

    public void loadSchemeWithTransports(String schemeName, String[] enabledTransports) throws MapSerializationException {
        try {
            ZipArchiveMapProvider mapProvider = new ZipArchiveMapProvider(identifierProvider, mapFile);
            try {
                if (metadata == null) {

                    locale = new MapLocale(mapProvider.getTextsMap(
                            suggestLanguage(
                                    mapProvider.getSupportedLocales(),
                                    preferredLanguage)));

                    metadata = mapProvider.getMetadata(locale);
                    stations = mapProvider.getStationInformation();
                    schemes = new HashMap<>();
                    transports = new HashMap<>();
                }
                MapScheme scheme = loadSchemeFile(mapProvider, schemeName, locale);
                loadTransportFiles(mapProvider, enabledTransports != null ? enabledTransports : scheme.getDefaultTransports());
            } finally {
                mapProvider.close();
            }
        } catch (MapSerializationException e) {
            Log.e(Constants.LOG, "Map unpacking failed", e);
            throw e;
        } catch (Exception e) {
            Log.e(Constants.LOG, "Map loading failed", e);
            throw new MapSerializationException(e);
        }
    }

    public File getMapFile(){
        return mapFile;
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
        return result.toArray(new MapTransportScheme[result.size()]);
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
            if(!line.getName().equals(lineName)){
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
        try {
            ZipArchiveMapProvider mapProvider = new ZipArchiveMapProvider(identifierProvider, mapFile);
            try {
                return mapProvider.getFileContent(mapFilePath);
            } finally {
                mapProvider.close();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG, "Map station scheme file loading failed", e);
            throw e;
        }
    }

    public int getMaxStationUid() {
        int max = 0;
        for(MapTransportScheme transport: transports.values()){
            for(MapTransportLine line: transport.getLines()){
                for(MapTransportSegment segment: line.getSegments()){
                    max = Math.max(max, Math.max(segment.getFrom(), segment.getTo()));
                }
            }
            for(MapTransportTransfer transfer: transport.getTransfers()){
                max = Math.max(max, Math.max(transfer.getFrom(), transfer.getTo()));
            }
        }
        for(MapScheme scheme : schemes.values()){
            for(MapSchemeLine line: scheme.getLines()){
                for(MapSchemeSegment segment: line.getSegments()){
                    max = Math.max(max, Math.max(segment.getFrom(), segment.getTo()));
                }

            }
            for(MapSchemeTransfer transfer: scheme.getTransfers()){
                max = Math.max(max, Math.max(transfer.getFrom(), transfer.getTo()));
            }
        }
        return max;
    }

    private static String suggestLanguage(String[] supportedLanguages, String preferredLanguage) throws IOException {
        List<String> locales = Arrays.asList(supportedLanguages);
        if(!locales.contains(preferredLanguage)){
            return locales.get(0);
        }
        return preferredLanguage;
    }

    private MapScheme loadSchemeFile(ZipArchiveMapProvider mapProvider, String schemeName, MapLocale locale) throws IOException, MapSerializationException {
        MapScheme scheme = schemes.get(schemeName);
        if (scheme == null) {
            scheme = mapProvider.getScheme(metadata.getScheme(schemeName).getFileName(), locale);
            schemes.put(scheme.getName(), scheme);
        }
        return scheme;
    }

    private void loadTransportFiles(ZipArchiveMapProvider mapProvider, String[] enabledTransports) throws IOException, MapSerializationException {
        for (String transportName : enabledTransports) {
            loadTransport(mapProvider, transportName);
        }
    }

    private MapTransportScheme loadTransport(ZipArchiveMapProvider mapProvider, String transportName) throws IOException, MapSerializationException {
        MapTransportScheme transport = transports.get(transportName);
        if (transport == null) {
            transport = mapProvider.getTransportScheme(metadata.getTransport(transportName).getFileName());
            transports.put(transport.getName(), transport);
        }
        return transport;
    }
}
