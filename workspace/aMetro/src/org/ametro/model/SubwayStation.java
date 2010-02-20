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

public class SubwayStation {

    public static final int VERSION = 1;

    public int id;

    public String name;
    public Rect rect;
    public Point point;
    public SubwayLine line;

    public SubwayStation(int newId, String newName, Rect newRect, Point newPoint, SubwayLine newLine) {
        id = newId;
        name = newName;
        rect = newRect;
        point = newPoint;
        line = newLine;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && id == ((SubwayStation) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "[NAME:" + name + "]";
    }

}
