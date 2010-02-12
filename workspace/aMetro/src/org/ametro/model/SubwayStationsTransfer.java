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

public class SubwayStationsTransfer {

    public static final int VERSION = 1;

    public static final int INVISIBLE = 1;

    public Double delay;
    public SubwayStation from;
    public SubwayStation to;
    public int flags;

    public SubwayStationsTransfer(SubwayStation newFrom, SubwayStation newTo, Double newDelay, int newFlags) {
        delay = newDelay;
        from = newFrom;
        to = newTo;
        flags = newFlags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubwayStationsTransfer)) return false;

        SubwayStationsTransfer that = (SubwayStationsTransfer) o;
        return flags == that.flags && from.equals(that.from) && to.equals(that.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + flags;
        return result;
    }
}
