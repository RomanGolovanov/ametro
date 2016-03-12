package org.ametro.catalog.entities;

public class MapInfoEntity {
    private final String uid;
    private final int city_id;
    private final TransportType[] types;
    private final String fileName;
    private final int size;
    private final int timestamp;
    private final double latitude;
    private final double longitude;

    public MapInfoEntity(int city_id, String fileName, double latitude, double longitude, int size, int timestamp, TransportType[] types, String uid) {
        this.uid = uid;
        this.city_id = city_id;
        this.types = types;
        this.fileName = fileName;
        this.size = size;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MapInfoEntity(MapInfoEntity src){
        this.uid = src.uid;
        this.city_id = src.city_id;
        this.types = src.types;
        this.fileName = src.fileName;
        this.size = src.size;
        this.timestamp = src.timestamp;
        this.latitude = src.latitude;
        this.longitude = src.longitude;
    }

    public String getUid() {
        return uid;
    }

    public int getCityId() {
        return city_id;
    }

    public TransportType[] getTypes() {
        return types;
    }

    public String getFileName() { return fileName; }

    public int getSize() {
        return size;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this){
            return true;
        }
        if(o instanceof MapInfoEntity){
            MapInfoEntity other = (MapInfoEntity)o;
            return fileName.equals(other.fileName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }
}
