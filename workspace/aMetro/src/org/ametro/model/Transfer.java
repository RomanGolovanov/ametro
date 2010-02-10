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

public class Transfer implements Serializable {

    public static final int VERSION = 1;

    private static final long serialVersionUID = 5468026097510797847L;

    public static final int INVISIBLE = 1;

    public Double mDelay;
    public Station mFrom;
    public Station mTo;
    public int mFlags;

    public Transfer(Station from, Station to, Double delay, int flags) {
        super();
        this.mDelay = delay;
        this.mFrom = from;
        this.mTo = to;
        this.mFlags = flags;
    }

    public Double getDelay() {
        return mDelay;
    }

    public Station getFrom() {
        return mFrom;
    }

    public Station getTo() {
        return mTo;
    }

    public int getFlags() {
        return mFlags;
    }


}
