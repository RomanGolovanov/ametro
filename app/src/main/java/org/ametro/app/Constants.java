package org.ametro.app;

import java.net.URI;

public class Constants {
    public static final URI MAP_SERVICE_URI = URI.create("https://maps.ametro.org/");

    public static final long MAP_EXPIRATION_PERIOD_MILLISECONDS = 1000 * 60 * 60 * 24;

    public static final String LINE_NAME = "LINE_NAME";

    public static final String MAP_CITY = "MAP_CITY";
    public static final String MAP_COUNTRY = "MAP_COUNTRY";
    public static final String MAP_PATH = "MAP_PATH";

    public static final String STATION_NAME = "STATION_NAME";
    public static final String STATION_UID = "STATION_UID";

    public static final String LOG = "AMETRO";

    public static final long ANIMATION_DURATION = 100;
}
