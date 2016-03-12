package org.ametro.catalog;

import android.content.res.Resources;
import android.util.Log;

import org.ametro.app.Constants;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.catalog.localization.MapInfoLocalizationProvider;
import org.ametro.catalog.serialization.MapCatalogSerializer;
import org.ametro.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapCatalogManager {

    private static final String LOCAL_CATALOG_FILE = "local-maps-catalog.json";

    private final File file;
    private final File workingDirectory;
    private final MapInfoLocalizationProvider localizationProvider;
    private MapCatalog catalog;

    public MapCatalogManager(File workingDirectory, MapInfoLocalizationProvider localizationProvider) {
        this.workingDirectory = workingDirectory;
        this.localizationProvider = localizationProvider;
        this.file = new File(workingDirectory, LOCAL_CATALOG_FILE);
    }

    public MapCatalog getMapCatalog() {
        if (catalog != null) {
            return catalog;
        }
        if (!file.exists()) {
            return catalog = new MapCatalog(new MapInfo[0]);
        }
        return catalog = loadCatalog();
    }

    public void addOrReplaceMapAll(MapInfo[] newMaps) {
        List<MapInfo> maps = new ArrayList<>(Arrays.asList(getMapCatalog().getMaps()));
        List<MapInfo> mapsList = Arrays.asList(newMaps);
        maps.removeAll(mapsList);
        maps.addAll(0, mapsList);
        catalog = new MapCatalog(maps.toArray(new MapInfo[maps.size()]));
        storeCatalog();
    }

    public void deleteMapAll(MapInfo[] deletingMaps) {
        List<MapInfo> maps = new ArrayList<>(Arrays.asList(getMapCatalog().getMaps()));
        maps.removeAll(Arrays.asList(deletingMaps));
        catalog = new MapCatalog(maps.toArray(new MapInfo[maps.size()]));
        storeCatalog();
        for (MapInfo m : deletingMaps) {
            try {
                FileUtils.delete(getMapFile(m));
            } catch (IOException e) {
                Log.e(Constants.LOG, "Cannot delete map " + m.getFileName(), e);
            }
        }
    }

    public MapInfo findMapByName(String mapFileName) {
        for (MapInfo map : getMapCatalog().getMaps()) {
            if (map.getFileName().equals(mapFileName)) {
                return map;
            }
        }
        throw new Resources.NotFoundException("Not found map file " + mapFileName);
    }

    private MapCatalog loadCatalog() {
        try {
            return localizationProvider.createCatalog(
                    MapCatalogSerializer.deserializeMapInfoArray(FileUtils.readAllText(file)));
        } catch (Exception ex) {
            Log.e(Constants.LOG, String.format("Cannot read map catalog due exception: %s", ex.toString()));
            FileUtils.safeDelete(file);
            return new MapCatalog(new MapInfo[0]);
        }
    }

    private void storeCatalog() {
        try {
            FileUtils.writeAllText(file, MapCatalogSerializer.serializeMapInfoArray(catalog.getMaps()));
        } catch (Exception ex) {
            Log.e(Constants.LOG, String.format("Cannot store map catalog due exception: %s", ex.toString()));
        }
    }

    public File getMapFile(MapInfo map) {
        return new File(this.workingDirectory, map.getFileName());
    }

    public File getTempMapFile(MapInfo map) {
        return new File(this.workingDirectory, map.getFileName() + ".temporary");
    }

}
