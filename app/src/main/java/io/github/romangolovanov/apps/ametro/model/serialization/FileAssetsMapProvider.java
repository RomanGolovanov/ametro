package io.github.romangolovanov.apps.ametro.model.serialization;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.github.romangolovanov.apps.ametro.model.entities.MapLocale;
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata;
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme;
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme;
import io.github.romangolovanov.apps.ametro.utils.FileUtils;

/**
 * Provides access to map contents extracted under a relative folder in assets/.
 * For example: fileAssetLocation = "maps/moscow"
 */
public class FileAssetsMapProvider implements MapProvider {

    private final AssetManager assetManager;
    private final String basePath;
    private final ObjectReader reader;
    private final GlobalIdentifierProvider identifierProvider;

    public FileAssetsMapProvider(GlobalIdentifierProvider identifierProvider,
                                 AssetManager assetManager,
                                 String fileAssetLocation) {
        this.identifierProvider = identifierProvider;
        this.assetManager = assetManager;
        this.basePath = fileAssetLocation.endsWith("/") ? fileAssetLocation : fileAssetLocation + "/";
        this.reader = new ObjectMapper().reader();
    }

    public String[] getSupportedLocales() throws IOException {
        return getMetadata(null).getLocales();
    }

    public HashMap<Integer, String> getTextsMap(String languageCode) throws IOException {
        try (InputStream stream = getInputStream("texts/" + languageCode + ".json")) {
            return MetadataTypes.asTextMap(reader.readTree(stream));
        }
    }

    public HashMap<Integer, List<String>> getAllTextsMap() throws IOException {
        var locales = getSupportedLocales();
        var map = new HashMap<Integer, List<String>>();
        for (String locale : locales) {
            var localeTexts = getTextsMap(locale);
            for (int textId : localeTexts.keySet()) {
                if (map.containsKey(textId)) {
                    Objects.requireNonNull(map.get(textId)).add(localeTexts.get(textId));
                } else {
                    var list = new ArrayList<String>();
                    list.add(localeTexts.get(textId));
                    map.put(textId, list);
                }
            }
        }
        return map;
    }

    public MapMetadata getMetadata(MapLocale locale) throws IOException {
        try (InputStream stream = getInputStream("index.json")) {
            return MetadataTypes.asMetadata(reader.readTree(stream), locale);
        }
    }

    public MapTransportScheme getTransportScheme(String name) throws IOException {
        try (InputStream stream = getInputStream(name)) {
            return TransportSchemeTypes.asMapTransportScheme(reader.readTree(stream));
        }
    }

    public MapScheme getScheme(String name, MapLocale locale) throws IOException {
        MapScheme scheme;
        try (InputStream stream = getInputStream(name)) {
            scheme = SchemeTypes.asMapScheme(identifierProvider, reader.readTree(stream), locale);
        }
        for (String imageName : scheme.getImageNames()) {
            scheme.setBackgroundObject(imageName, getBackgroundObject(imageName));
        }
        return scheme;
    }

    public Object getBackgroundObject(String name) throws IOException {
        try (InputStream stream = getInputStream(name)) {
            if (name.endsWith(".svg")) {
                SVG svg = SVG.getFromInputStream(stream);
                return svg.renderToPicture();
            } else if (name.endsWith(".png")) {
                return BitmapFactory.decodeStream(stream);
            } else {
                throw new IOException("Unsupported type of image file " + name);
            }
        } catch (SVGParseException e) {
            throw new RuntimeException(e);
        }
    }

    public MapStationInformation[] getStationInformation() throws IOException {
        try (InputStream stream = getInputStream("images.json")) {
            return MetadataTypes.asStationInformation(reader.readTree(stream));
        }
    }

    public String getFileContent(String name) throws IOException {
        try (InputStream stream = getInputStream(name)) {
            return FileUtils.readAllText(stream);
        }
    }

    private InputStream getInputStream(String relativePath) throws IOException {
        String fullPath = basePath + relativePath;
        try {
            return assetManager.open(fullPath);
        } catch (IOException e) {
            throw new IOException("Asset not found: " + fullPath, e);
        }
    }

    @Override
    public void close() {
        // Nothing to close for AssetManager
    }
}
