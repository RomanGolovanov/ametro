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

import android.graphics.Point;

import java.io.Serializable;
import java.util.*;

public class SubwayMap {

    public static final int VERSION = 1;

    public long timestamp;
    public long crc;

    public String mapName;

    public String cityName;
    public String countryName;

    public int width;
    public int height;

    public int stationDiameter;
    public int linesWidth;
    public boolean wordWrap;
    public boolean upperCase;

    public long sourceVersion;

    public SubwayLine[] lines;
    public SubwayStationsTransfer[] transfers;
    public SubwaySegment[] segments;
    public SubwayStation[] stations;

    public HashMap<String, SubwayStation> stationsByName = new HashMap<String, SubwayStation>();

    public HashMap<String, SubwayStation[]> stationsByLine = new HashMap<String, SubwayStation[]>();
    public HashMap<String, SubwaySegment[]> segmentsByLine = new HashMap<String, SubwaySegment[]>();

    public HashMap<SubwaySegment, Point[]> segmentNodes = new HashMap<SubwaySegment, Point[]>();

    public SubwayMap(String newMapName) {
        mapName = newMapName;
    }

}
