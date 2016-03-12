package org.ametro.ui.navigation.entities;

import android.graphics.drawable.Drawable;

public class NavigationHeader extends NavigationItem {

    private final Drawable icon;
    private final String city;
    private final String country;
    private final String comment;
    private final Drawable[] transportTypes;


    public NavigationHeader(Drawable icon, String city, String country, String comment, Drawable[] transportTypes) {
        this.icon = icon;
        this.city = city;
        this.country = country;
        this.comment = comment;
        this.transportTypes = transportTypes;
    }

    public NavigationHeader(String emptyText) {
        this(null, emptyText, null, null, new Drawable[0]);
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getComment() {
        return comment;
    }

    public Drawable[] getTransportTypeIcons() {
        return transportTypes;
    }
}
