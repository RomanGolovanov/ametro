package org.ametro.model.entities;

import android.content.res.Resources;

import java.util.Map;

public class MapMetadata {

    private final MapLocale locale;
    private final String id;
    private final int cityId;
    private final int timestamp;
    private final double latitude;
    private final double longitude;
    private final Map<String, Scheme> schemes;
    private final Map<String, TransportScheme> transports;
    private final String[] transportTypes;
    private final MapDelay[] delays;
    private final String[] locales;
    private final Integer commentsTextId;
    private final Integer descriptionTextId;
    private final String fileName;

    public MapMetadata(MapLocale locale,
                       String id,
                       int cityId,
                       int timestamp,
                       double latitude,
                       double longitude,
                       Map<String, Scheme> schemes,
                       Map<String, TransportScheme> transports,
                       String[] transportTypes,
                       MapDelay[] delays,
                       String[] locales, Integer commentsTextId,
                       Integer descriptionTextId,
                       String fileName) {
        this.locale = locale;
        this.id = id;
        this.cityId = cityId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.schemes = schemes;
        this.transports = transports;
        this.transportTypes = transportTypes;
        this.delays = delays;
        this.locales = locales;
        this.commentsTextId = commentsTextId;
        this.descriptionTextId = descriptionTextId;
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public int getCityId() {
        return cityId;
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

    public Map<String, Scheme> getSchemes() {
        return schemes;
    }

    public Map<String, TransportScheme> getTransports() {
        return transports;
    }

    public String[] getTransportTypes() {
        return transportTypes;
    }

    public MapDelay[] getDelays() {
        return delays;
    }

    public String[] getLocales() {
        return locales;
    }

    public String getComments() {
        return locale.getText(commentsTextId);
    }

    public String getDescription() {
        return locale.getText(descriptionTextId);
    }

    public String getFileName() {
        return fileName;
    }

    public Scheme getScheme(String name) {
        Scheme scheme = schemes.get(name);
        if (scheme != null) {
            return scheme;
        }
        throw new Resources.NotFoundException("Not found metadata for scheme " + name);

    }

    public TransportScheme getTransport(String name) {
        TransportScheme scheme = transports.get(name);
        if (scheme != null) {
            return scheme;
        }
        throw new Resources.NotFoundException("Not found metadata for transport " + name);

    }

    public static class Scheme {
        private final MapLocale locale;
        private final String name;
        private final Integer nameTextId;
        private final String typeName;
        private final Integer typeTextId;
        private final String fileName;
        private final String[] transports;
        private final String[] defaultTransports;
        private final boolean isRoot;

        public Scheme(MapLocale locale, String name, Integer nameTextId, String typeName, Integer typeTextId, String fileName, String[] transports, String[] defaultTransports, boolean isRoot) {
            this.locale = locale;
            this.name = name;
            this.nameTextId = nameTextId;
            this.typeName = typeName;
            this.typeTextId = typeTextId;
            this.fileName = fileName;
            this.transports = transports;
            this.defaultTransports = defaultTransports;
            this.isRoot = isRoot;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return locale.getText(nameTextId);
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeDisplayName() {
            return locale.getText(typeTextId);
        }

        public String getFileName() {
            return fileName;
        }

        public String[] getTransports() {
            return transports;
        }

        public String[] getDefaultTransports() {
            return defaultTransports;
        }

        public boolean isRoot() {
            return isRoot;
        }
    }

    public static class TransportScheme {
        private final String name;
        private final String fileName;
        private final String typeName;

        public TransportScheme(String name, String fileName, String typeName) {
            this.name = name;
            this.fileName = fileName;
            this.typeName = typeName;
        }

        public String getName() {
            return name;
        }

        public String getFileName() {
            return fileName;
        }

        public String getTypeName() {
            return typeName;
        }
    }
}
