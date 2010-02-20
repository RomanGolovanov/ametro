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


public class SubwaySegment {

    public static final int VERSION = 1;

    public static final int SPLINE = 0x01;
    public static final int INVISIBLE = 0x02;

    public int id;

    public Double delay;
    public int fromStationId;
    public int toStationId;
    public int flags;

    public SubwaySegment() {
    }

    public SubwaySegment(int newId, int newFromStationId, int newToStationId, Double newDelay) {
        id = newId;
        delay = newDelay;
        fromStationId = newFromStationId;
        toStationId = newToStationId;
    }

    public SubwaySegment(int newId, int newFromStationId, int newToStationId, Double newDelay, int newFlags) {
        id = newId;
        delay = newDelay;
        fromStationId = newFromStationId;
        toStationId = newToStationId;
        flags = newFlags;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && id == ((SubwaySegment) o).id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override

    public String toString() {
        return "[FROM:" + fromStationId + ";TO:" + toStationId + "]";
    }

}
