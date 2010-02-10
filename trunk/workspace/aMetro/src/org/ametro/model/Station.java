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
import org.ametro.libs.Helpers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Station implements Serializable {

    private static final long serialVersionUID = 8903734927284011978L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(mName);
        Helpers.serializeRect(out, mRect);
        Helpers.serializePoint(out, mPoint);
        out.writeObject(mLine);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        mName = (String) in.readObject();
        mRect = Helpers.deserializeRect(in);
        mPoint = Helpers.deserializePoint(in);
        mLine = (Line) in.readObject();
        mSegments = new ArrayList<Segment>();
    }

    private String mName;
    private Rect mRect;
    private Point mPoint;
    private Line mLine;

    private ArrayList<Segment> mSegments = new ArrayList<Segment>();

    public Station(String mName, Rect mRect, Point mPoint, Line mLine) {
        super();
        this.mName = mName;
        this.mRect = mRect;
        this.mPoint = mPoint;
        this.mLine = mLine;
    }

    public Rect getRect() {
        return mRect;
    }

    public void setRect(Rect mRect) {
        this.mRect = mRect;
    }

    public Point getPoint() {
        return mPoint;
    }

    public void setPoint(Point mPoint) {
        this.mPoint = mPoint;
    }

    public String getName() {
        return mName;
    }

    public Line getLine() {
        return mLine;
    }

    public boolean hasConnections() {
        for (Segment segment : mSegments) {
            Double delay = segment.getDelay();
            if (delay != null && delay != 0) {
                return true;
            }
        }
        return false;
    }

    public Segment addSegment(Segment segment, int segmentMode) {
        mSegments.add(segment);
        return segment;
    }

    @Override
    public String toString() {
        return "[NAME:" + mName + "]";
    }
}
