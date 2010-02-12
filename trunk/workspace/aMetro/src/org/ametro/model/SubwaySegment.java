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

    public Double delay;
    public SubwayStation from;
    public SubwayStation to;
    public int flags;

    public SubwaySegment(SubwayStation newFrom, SubwayStation newTo, Double newDelay) {
        delay = newDelay;
        from = newFrom;
        to = newTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubwaySegment)) return false;
        SubwaySegment segment = (SubwaySegment) o;
        return flags == segment.flags && from.equals(segment.from) && to.equals(segment.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + flags;
        return result;
    }

    @Override
    public String toString() {
        return "[FROM:" + from.name + ";TO:" + to.name + "]";
    }

}
