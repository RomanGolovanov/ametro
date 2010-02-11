/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.model;

import java.io.Serializable;

public class ModelDescription implements Serializable {

    public static final int VERSION = 1;

    private static final long serialVersionUID = 7999055006455680808L;

    private String mMapName;
    private String mCountryName;
    private String mCityName;

    private int mWidth;
    private int mHeight;

    private long mTimestamp;
    private long mCrc;

    private long mRenderVersion;
    private long mSourceVersion;

    public ModelDescription() {
        super();
    }

    public ModelDescription(String mapName, String countryName, String cityName, int width, int height, long crc, long timestamp, long sourceVersion, long renderVersion) {
        super();
        this.mMapName = mapName;
        this.mCountryName = countryName;
        this.mCityName = cityName;
        this.mWidth = width;
        this.mHeight = height;
        this.mCrc = crc;
        this.mTimestamp = timestamp;
        this.mSourceVersion = sourceVersion;
        this.mRenderVersion = renderVersion;
    }

    public ModelDescription(Model model) {
        super();
        mMapName = model.mapName;
        mCountryName = model.countryName;
        mCityName = model.cityName;
        mWidth = model.width;
        mHeight = model.height;
        mCrc = model.crc;
        mTimestamp = model.timestamp;
        mSourceVersion = model.sourceVersion;
    }

    public String getMapName() {
        return mMapName;
    }

    public void setMapName(String mapName) {
        this.mMapName = mapName;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public void setCountryName(String countryName) {
        this.mCountryName = countryName;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        this.mCityName = cityName;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public long getCrc() {
        return mCrc;
    }

    public void setCrc(long mCrc) {
        this.mCrc = mCrc;
    }

    public long getRenderVersion() {
        return mRenderVersion;
    }

    public void setRenderVersion(long mRenderVersion) {
        this.mRenderVersion = mRenderVersion;
    }

    public long getSourceVersion() {
        return mSourceVersion;
    }

    public void setSourceVersion(long mSourceVersion) {
        this.mSourceVersion = mSourceVersion;
    }

    public boolean completeEqual(ModelDescription model) {
        return locationEqual(model)
                && mCrc == model.getCrc()
                && mTimestamp == model.getTimestamp()
                && mSourceVersion == model.getSourceVersion()
                ;
    }

    public boolean locationEqual(ModelDescription model) {
        return mCountryName.equals(model.getCountryName())
                && mCityName.equals(model.getCityName())
                ;
    }


}
