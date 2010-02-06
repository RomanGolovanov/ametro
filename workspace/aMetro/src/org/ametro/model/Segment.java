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
import org.ametro.libs.Helpers;

import java.io.IOException;
import java.io.Serializable;


public class Segment implements Serializable {

    private static final long serialVersionUID = 4225862817281735747L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(mDelay);
        out.writeObject(mFrom);
        out.writeObject(mTo);
        out.writeInt(mFlags);
        Helpers.serializePointArray(out, mAdditionalNodes);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        mDelay = (Double) in.readObject();
        mFrom = (Station) in.readObject();
        mTo = (Station) in.readObject();
        mFlags = in.readInt();
        mAdditionalNodes = Helpers.deserializePointArray(in);
    }


    public static final int SPLINE = 0x01;
    public static final int INVISIBLE = 0x02;

    public static final int SEGMENT_BEGIN = 0x01;
    public static final int SEGMENT_END = 0x02;

    private Double mDelay;
    private Point[] mAdditionalNodes;
    private Station mFrom;
    private Station mTo;
    private int mFlags;

    public Segment(Station from, Station to, Double delay) {
        super();
        this.mDelay = delay;
        this.mFrom = from;
        this.mTo = to;
        this.mFlags = 0;

        from.addSegment(this, Segment.SEGMENT_BEGIN);
        to.addSegment(this, Segment.SEGMENT_END);
    }

    public Point[] getAdditionalNodes() {
        return mAdditionalNodes;
    }

    public void setAdditionalNodes(Point[] additionalNodes) {
        this.mAdditionalNodes = additionalNodes;
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

    public void setFlags(int flags) {
        this.mFlags = flags;
    }

    public void addFlag(int flag) {
        this.mFlags |= flag;
    }

    @Override
    public String toString() {
        return "[FROM:" + mFrom.getName() + ";TO:" + mTo.getName() + "]";
    }


}
