package org.ametro.model.serialization;

import android.graphics.BitmapFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import org.ametro.model.entities.MapLocale;
import org.ametro.model.entities.MapMetadata;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapStationInformation;
import org.ametro.model.entities.MapTransportScheme;
import org.ametro.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipFile;

public class ZipArchiveMapProvider extends ZipFile {

    private final ObjectReader reader;
    private final GlobalIdentifierProvider identifierProvider;

    public ZipArchiveMapProvider(GlobalIdentifierProvider identifierProvider, File mapFile) throws IOException {
        super(mapFile);
        this.identifierProvider = identifierProvider;
        reader = new ObjectMapper().reader();
    }

    public String[] getSupportedLocales() throws IOException {
        return getMetadata(null).getLocales();
    }

    public HashMap<Integer, String> getTextsMap(String languageCode) throws IOException {
        InputStream stream = getInputStream(String.format("texts/%s.json", languageCode));
        try {
            return MetadataTypes.asTextMap(reader.readTree(stream));
        } finally {
            stream.close();
        }
    }

    public MapMetadata getMetadata(MapLocale locale) throws IOException {
        InputStream stream = getInputStream("index.json");
        try {
            return MetadataTypes.asMetadata(reader.readTree(stream), locale);
        } finally {
            stream.close();
        }
    }

    public MapTransportScheme getTransportScheme(String name) throws IOException {
        InputStream stream = getInputStream(name);
        try {
            return TransportSchemeTypes.asMapTransportScheme(reader.readTree(stream));
        } finally {
            stream.close();
        }
    }

    public MapScheme getScheme(String name, MapLocale locale) throws IOException {
        MapScheme scheme = null;
        InputStream stream = getInputStream(name);
        try {
            scheme = SchemeTypes.asMapScheme(identifierProvider, reader.readTree(stream), locale);
        } finally {
            stream.close();
        }

        for (String imageName : scheme.getImageNames()) {
            scheme.setBackgroundObject(imageName, getBackgroundObject(imageName));
        }
        return scheme;
    }

    public Object getBackgroundObject(String name) throws IOException {
        InputStream stream = getInputStream(name);
        try {
            if (name.endsWith(".svg")) {
                SVG svg = new SVGBuilder().readFromString(FileUtils.readAllText(stream)).build();
                return svg.getPicture();
            } else if (name.endsWith(".png")) {
                return BitmapFactory.decodeStream(stream);
            }else {
                throw new IOException("Unsupported type of image file " + name);
            }
        } finally {
            stream.close();
        }
    }

    public MapStationInformation[] getStationInformation() throws IOException {
        InputStream stream = getInputStream("images.json");
        try {
            return MetadataTypes.asStationInformation(reader.readTree(stream));
        } finally {
            stream.close();
        }
    }

    public String getFileContent(String name) throws IOException{
        InputStream stream = getInputStream(name);
        try {
            return FileUtils.readAllText(stream);
        } finally {
            stream.close();
        }
    }

    private InputStream getInputStream(String name) throws IOException {
        return getInputStream(getEntry(name));
    }

}
