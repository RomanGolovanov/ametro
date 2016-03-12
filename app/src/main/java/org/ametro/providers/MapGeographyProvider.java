package org.ametro.providers;

import org.ametro.catalog.entities.MapInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapGeographyProvider {

    private String[] countries;
    private String[][] cities;

    private final HashMap<String,String> countryIsoCodes = new HashMap<>();
    private final HashMap<String,Integer> countryIdentifierMap = new HashMap<>();
    private final HashMap<String,Integer> cityIdentifierMap = new HashMap<>();

    public MapGeographyProvider(MapInfo[] maps)
    {
        setData(maps);
    }

    public void setData(MapInfo[] maps){
        bindStaticData(maps);
        bindData(maps);
    }

    protected void bindData(MapInfo[] maps) {
        Map<String,Set<String>> localCountries = new HashMap<>();
        for(MapInfo m: maps){
            String country = m.getCountry();
            Set<String> localCities = localCountries.get(country);
            if(localCities == null){
                localCities = new HashSet<>();
                localCountries.put(country, localCities);
            }
            String city = m.getCity();
            if(!localCities.contains(city)){
                localCities.add(city);
            }
        }

        countries = localCountries.keySet().toArray(new String[localCountries.keySet().size()]);
        Arrays.sort(countries);

        cities = new String[countries.length][];
        for(int i=0;i< cities.length;i++){
            cities[i] = localCountries.get(countries[i]).toArray(
                    new String[localCountries.get(countries[i]).size()]);
            Arrays.sort(cities[i]);
        }
    }

    private void bindStaticData(MapInfo[] maps){
        int countryId = 1, cityId = 1;
        countryIsoCodes.clear();
        countryIdentifierMap.clear();
        cityIdentifierMap.clear();

        for(MapInfo m: maps){
            String country = m.getCountry();
            if(countryIdentifierMap.get(country) == null){
                countryIsoCodes.put(country, m.getIso());
                countryIdentifierMap.put(country, countryId++);
            }

            String city = m.getCity();
            if(cityIdentifierMap.get(city) == null){
                cityIdentifierMap.put(city, cityId++);
            }
        }
    }

    public String[] getCountries()
    {
        return countries;
    }

    public String[] getCountryCities(int countryIndex)
    {
        return cities[countryIndex];
    }

    public String getCountryIsoCode(String country)
    {
        return countryIsoCodes.get(country);
    }

    public int getCountryId(String countryName)
    {
        return countryIdentifierMap.get(countryName);
    }

    public int getCityId(String cityName)
    {
        return cityIdentifierMap.get(cityName);
    }

}
