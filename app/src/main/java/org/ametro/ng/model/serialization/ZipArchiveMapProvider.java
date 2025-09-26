package org.ametro.ng.model.serialization;

import android.graphics.BitmapFactory;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.ametro.ng.model.entities.MapLocale;
import org.ametro.ng.model.entities.MapMetadata;
import org.ametro.ng.model.entities.MapScheme;
import org.ametro.ng.model.entities.MapStationInformation;
import org.ametro.ng.model.entities.MapTransportScheme;
import org.ametro.ng.utils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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

    /** Construct from a File (old path). */
    public ZipArchiveMapProvider(GlobalIdentifierProvider identifierProvider, java.io.File mapFile) throws IOException {
        this.identifierProvider = identifierProvider;
        this.reader = new ObjectMapper().reader();
        this.zipFile = new ZipFile(mapFile);
        this.entryCache = null;
    }

    /** Construct from an InputStream (assets path). */
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
