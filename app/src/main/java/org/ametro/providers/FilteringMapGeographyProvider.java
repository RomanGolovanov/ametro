package org.ametro.providers;

import org.ametro.catalog.entities.MapInfo;
import org.ametro.utils.StringUtils;

import java.util.ArrayList;

public class FilteringMapGeographyProvider extends MapGeographyProvider {

    private final MapInfo[] originalValues;

    public FilteringMapGeographyProvider(MapInfo[] maps) {
        super(maps);
        originalValues = maps;
    }

    public void setFilter(String criteria){
        bindData(filterMapsByCountryOrCityName(criteria));
    }

    private MapInfo[] filterMapsByCountryOrCityName(String criteria) {
        if(criteria == null)
            return originalValues;

        ArrayList<MapInfo> filteredMaps = new ArrayList<>(originalValues.length);
        for(MapInfo map: originalValues){
            if (StringUtils.startsWithoutDiacritics(map.getCity(), criteria)
                    || StringUtils.startsWithoutDiacritics(map.getCountry(),criteria)) {
                filteredMaps.add(map);
            }
        }
        return filteredMaps.toArray(new MapInfo[filteredMaps.size()]);
    }

}
