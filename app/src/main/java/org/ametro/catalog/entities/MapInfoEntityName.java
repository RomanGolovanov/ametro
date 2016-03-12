package org.ametro.catalog.entities;

public class MapInfoEntityName {

    private final int cityId;
    private final String cityName;
    private final String countryName;
    private final String countryIsoCode;

    public MapInfoEntityName(int cityId, String cityName, String countryName, String countryIsoCode) {

        this.cityId = cityId;
        this.cityName = cityName;
        this.countryName = countryName;
        this.countryIsoCode = countryIsoCode;
    }

    public int getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }
}
