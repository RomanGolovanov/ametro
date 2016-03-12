package org.ametro.catalog.localization;

import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.catalog.entities.MapInfoEntity;
import org.ametro.catalog.entities.MapInfoEntityName;
import org.ametro.catalog.serialization.MapCatalogSerializer;
import org.ametro.catalog.serialization.SerializationException;
import org.ametro.catalog.service.IMapServiceCache;
import org.ametro.utils.FileUtils;

import java.io.IOException;
import java.util.HashMap;

public class MapInfoLocalizationProvider {

    private final IMapServiceCache cache;
    private HashMap<Integer, MapInfoEntityName> localizationMap;

    public MapInfoLocalizationProvider(IMapServiceCache cache){
        this.cache = cache;
    }

    public String getCityName(int cityId){
        return getLocalizationMap().get(cityId).getCityName();
    }

    public String getCountryName(int cityId){
        return getLocalizationMap().get(cityId).getCountryName();
    }

    public String getCountryIsoCode(int cityId){
        return getLocalizationMap().get(cityId).getCountryIsoCode();
    }

    public MapCatalog createCatalog(MapInfoEntity[] maps) {
        HashMap<Integer, MapInfoEntityName> localizations = getLocalizationMap();
        MapInfo[] localizedMaps = new MapInfo[maps.length];
        for(int i=0; i<maps.length; i++){
            MapInfoEntityName localization = localizations.get(maps[i].getCityId());
            localizedMaps[i] = new MapInfo(
                    maps[i],
                    localization.getCityName(),
                    localization.getCountryName(),
                    localization.getCountryIsoCode());
        }
        return new MapCatalog(localizedMaps);
    }

    private HashMap<Integer, MapInfoEntityName> getLocalizationMap() {
        if(localizationMap !=null)
        {
            return localizationMap;
        }

        try {
            HashMap<Integer, MapInfoEntityName> newLocalizationMap = new HashMap<>();
            MapInfoEntityName[] entities = MapCatalogSerializer.deserializeLocalization(
                    FileUtils.readAllText(cache.getLocalizationFile()));

            for (MapInfoEntityName entity : entities) {
                newLocalizationMap.put(entity.getCityId(), entity);
            }
            localizationMap = newLocalizationMap;
            return localizationMap;
        }catch (SerializationException ex){
            throw new RuntimeException("Localization data has an invalid format", ex);
        }catch(IOException ex){
            throw new RuntimeException("Localization cannot be read", ex);
        }
    }
}
