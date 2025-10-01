package io.github.romangolovanov.apps.ametro.model.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.github.romangolovanov.apps.ametro.model.entities.MapLocale;
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata;
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme;
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme; /**
 * Abstraction for different sources of map content (zip archive, assets folder, etc.)
 */
public interface MapProvider extends AutoCloseable {

    String[] getSupportedLocales() throws IOException;

    HashMap<Integer, String> getTextsMap(String languageCode) throws IOException;

    HashMap<Integer, List<String>> getAllTextsMap() throws IOException;

    MapMetadata getMetadata(MapLocale locale) throws IOException;

    MapTransportScheme getTransportScheme(String name) throws IOException;

    MapScheme getScheme(String name, MapLocale locale) throws IOException;

    Object getBackgroundObject(String name) throws IOException;

    MapStationInformation[] getStationInformation() throws IOException;

    String getFileContent(String name) throws IOException;

    @Override
    void close() throws IOException;
}
