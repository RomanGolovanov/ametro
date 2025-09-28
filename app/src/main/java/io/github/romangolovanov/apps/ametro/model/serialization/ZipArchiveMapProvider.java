package io.github.romangolovanov.apps.ametro.model.serialization;

import android.graphics.BitmapFactory;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.github.romangolovanov.apps.ametro.model.entities.MapLocale;
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata;
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme;
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme;
import io.github.romangolovanov.apps.ametro.utils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Provides access to map archive (.zip) contents.
 * Now supports creation from either a File or an InputStream (assets).
 */
public class ZipArchiveMapProvider implements AutoCloseable {

    private final ObjectReader reader;
    private final GlobalIdentifierProvider identifierProvider;

    private final ZipFile zipFile;         // used if constructed with File
    private final HashMap<String, byte[]> entryCache; // used if constructed with InputStream

    public ZipArchiveMapProvider(GlobalIdentifierProvider identifierProvider, InputStream inputStream) throws IOException {
        this.identifierProvider = identifierProvider;
        this.reader = new ObjectMapper().reader();
        this.zipFile = null;
        this.entryCache = new HashMap<>();

        // fully buffer InputStream into memory for random access
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int read;
                while ((read = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                entryCache.put(entry.getName(), baos.toByteArray());
                zis.closeEntry();
            }
        }
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
            for(int textId : localeTexts.keySet()){
                if(map.containsKey(textId)){
                    Objects.requireNonNull(map.get(textId)).add(localeTexts.get(textId));
                }else{
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

    private InputStream getInputStream(String name) throws IOException {
        if (zipFile != null) {
            ZipEntry entry = zipFile.getEntry(name);
            if (entry == null) throw new IOException("Entry not found: " + name);
            return zipFile.getInputStream(entry);
        } else if (entryCache != null) {
            byte[] data = entryCache.get(name);
            if (data == null) throw new IOException("Entry not found: " + name);
            return new ByteArrayInputStream(data);
        } else {
            throw new IOException("Provider not initialized");
        }
    }

    @Override
    public void close() throws IOException {
        if (zipFile != null) {
            zipFile.close();
        }
        // entryCache doesn't need explicit close
    }
}
