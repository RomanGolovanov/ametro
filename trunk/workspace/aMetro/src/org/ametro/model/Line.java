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
import android.graphics.Rect;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class Line implements Serializable {

    public static final int VERSION = 1;

    private static final long serialVersionUID = -957788093146079549L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(name);

        out.writeInt(color);
        out.writeInt(labelColor);
        out.writeInt(labelBgColor);


        final Segment[] localSegments = segments;
        int segmentsCount = localSegments.length;
        out.writeInt(segmentsCount);
        for (int i = 0; i < segmentsCount; i++) {
            out.writeObject(localSegments[i]);
        }

        final Station[] localStations = stations;
        final int stationsCount = localStations.length;
        out.writeInt(stationsCount);
        for (int i = 0; i < stationsCount; i++) {
            out.writeObject(localStations[i]);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        name = (String) in.readObject();

        color = in.readInt();
        labelColor = in.readInt();
        labelBgColor = in.readInt();

        final int segmentsCount = in.readInt();
        final Segment[] localSegments = new Segment[segmentsCount];
        for (int i = 0; i < segmentsCount; i++) {
            final Segment segment = (Segment) in.readObject();
            localSegments[i] = segment;
            segment.getFrom().addSegment(segment, Segment.SEGMENT_BEGIN);
            segment.getTo().addSegment(segment, Segment.SEGMENT_END);
        }
        segments = localSegments;

        final HashMap<String, Station> localStationsMap = new HashMap<String, Station>();
        final int stationsCount = in.readInt();
        final Station[] localStations = new Station[stationsCount];
        for (int i = 0; i < stationsCount; i++) {
            final Station station = (Station) in.readObject();
            localStationsMap.put(station.getName(), station);
            localStations[i] = station;
        }

        stationsMap = localStationsMap;
        stations = localStations;

    }


    public String name;
    public int color;
    public int labelColor;
    public int labelBgColor;

    public HashMap<String, Station> stationsMap = new HashMap<String, Station>();
    public Station[] stations;
    public Segment[] segments;

    public Line(String newName, int newColor, int newLabelColor, int newLabelBgColor) {
        super();
        name = newName;
        color = newColor;
        labelColor = newLabelColor;
        labelBgColor = newLabelBgColor;
    }

    public Station getStation(String name) {
        return stationsMap.get(name);
    }

    private Station addStation(String name, Rect r, Point p) {
        Station st = new Station(name, r, p, this);
        stationsMap.put(name, st);
        return st;
    }

    public Station invalidateStation(String name) {
        Station st = stationsMap.get(name);
        if (st == null) {
            st = addStation(name, null, null);
        }
        return st;
    }

    public Station invalidateStation(String name, Rect r, Point p) {
        Station st = stationsMap.get(name);
        if (st == null) {
            st = addStation(name, r, p);
        } else {
            st.setPoint(p);
            st.setRect(r);
        }
        return st;
    }

    public Segment getSegment(Station from, Station to) {
        final String fromName = from.getName();
        final String toName = to.getName();
        for (Segment seg : segments) {
            if (seg.getFrom().getName().equals(fromName) && seg.getTo().getName().equals(toName)) {
                return seg;
            }
        }
        return null;
    }

}
