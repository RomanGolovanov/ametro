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

    public String mapName;
    public String countryName;
    public String cityName;

    public int width;
    public int height;

    public long timestamp;
    public long crc;

    public long renderVersion;
    public long sourceVersion;

    public ModelDescription() {
        super();
    }

    public ModelDescription(
            String newMapName, String newCountryName, String newCityName,
            int newWidth, int newHeight, long newCrc, long newTimestamp,
            long newSourceVersion, long newRenderVersion) {

        super();
        mapName = newMapName;
        countryName = newCountryName;
        cityName = newCityName;
        width = newWidth;
        height = newHeight;
        crc = newCrc;
        timestamp = newTimestamp;
        sourceVersion = newSourceVersion;
        renderVersion = newRenderVersion;
    }

    public ModelDescription(SubwayMap subwayMap) {
        super();
        mapName = subwayMap.mapName;
        countryName = subwayMap.countryName;
        cityName = subwayMap.cityName;
        width = subwayMap.width;
        height = subwayMap.height;
        crc = subwayMap.crc;
        timestamp = subwayMap.timestamp;
        sourceVersion = subwayMap.sourceVersion;
    }

    public boolean completeEqual(ModelDescription model) {
        return locationEqual(model)
                && crc == model.crc
                && timestamp == model.timestamp
                && sourceVersion == model.sourceVersion;
    }

    public boolean locationEqual(ModelDescription model) {
        return countryName.equals(model.countryName)
                && cityName.equals(model.cityName);
    }

}
