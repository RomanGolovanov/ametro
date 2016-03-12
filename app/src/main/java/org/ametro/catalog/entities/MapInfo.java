package org.ametro.catalog.entities;

public class MapInfo extends MapInfoEntity {

    private final String city;
    private final String country;
    private final String iso;

    public MapInfo(MapInfoEntity src, String city, String country, String iso) {
        super(src);
        this.city = city;
        this.country = country;
        this.iso = iso;
    }

    public MapInfo(MapInfo src) {
        super(src);
        this.city = src.getCity();
        this.country = src.getCountry();
        this.iso = src.getIso();
    }

    public MapInfo(int city_id, String fileName, double latitude, double longitude, int size,
                   int timestamp, TransportType[] types, String uid, String city, String country,
                   String iso) {
        super(city_id, fileName, latitude, longitude, size, timestamp, types, uid);
        this.city = city;
        this.country = country;
        this.iso = iso;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getIso() {
        return iso;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", city, country);
    }
}

