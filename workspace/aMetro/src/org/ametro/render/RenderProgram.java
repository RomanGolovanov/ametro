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

package org.ametro.render;

import android.graphics.*;
import org.ametro.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


public class RenderProgram {

    public static final int TYPE_LINE = 1;
    public static final int TYPE_TRANSFER_BACKGROUND = 2;
    public static final int TYPE_TRANSFER = 4;
    public static final int TYPE_STATION = 8;
    public static final int TYPE_STATION_NAME = 16;
    public static final int TYPE_BACKGROUND = 32;

    public static final int ONLY_TRANSPORT = TYPE_LINE | TYPE_TRANSFER_BACKGROUND | TYPE_TRANSFER | TYPE_STATION;
    public static final int ALL = ONLY_TRANSPORT | TYPE_STATION_NAME;

    RenderElement[] mElements;
    boolean[] mVisibility;
    Rect[] mBounds;
    int[] mTypes;
    int mRenderFilter;

    public void setRenderFilter(int renderFilter) {
        mRenderFilter = renderFilter;
    }

    public RenderProgram(Model model) {
        ArrayList<RenderElement> renderQueue = new ArrayList<RenderElement>();
        drawLines(model, renderQueue);
        drawTransfers(model, renderQueue);
        drawStations(model, renderQueue);
        Collections.sort(renderQueue);
        mElements = renderQueue.toArray(new RenderElement[renderQueue.size()]);
        final int count = mElements.length;
        mVisibility = new boolean[count];
        mBounds = new Rect[count];
        mTypes = new int[count];
        for (int i = 0; i < count; i++) {
            mVisibility[i] = false;
            mBounds[i] = mElements[i].BoundingBox;
            mTypes[i] = mElements[i].Type;
        }
        mRenderFilter = ALL;
    }

    public void invalidateVisible(RectF viewport) {
        final int offset = 10;
        final Rect v = new Rect(
                (int) (viewport.left - offset),
                (int) (viewport.top - offset),
                (int) (viewport.right + offset),
                (int) (viewport.bottom + offset));
        final Rect[] bounds = mBounds;
        final boolean[] visibility = mVisibility;
        final int[] filters = mTypes;
        final int filter = mRenderFilter;
        final int count = bounds.length;
        for (int i = 0; i < count; i++) {
            final Rect box = new Rect(bounds[i]);

            visibility[i] = ((filters[i] & filter) > 0) && Rect.intersects(v, box);
        }
    }

    public void draw(Canvas canvas) {
        final RenderElement[] elements = mElements;
        final boolean[] visibility = mVisibility;
        final int count = elements.length;
        canvas.drawColor(Color.WHITE);
        for (int i = 0; i < count; i++) {
            if (visibility[i]) {
                elements[i].draw(canvas);
            }
        }
    }

    private void drawStations(Model model, ArrayList<RenderElement> renderQueue) {
        final Line[] lines = model.lines;
        for (int i = 0; i < lines.length; i++) {
            final Line line = lines[i];
            for (Station station : line.stations) {
                if (station.getPoint() != null) {
                    renderQueue.add(new RenderStation(model, station));
                    if (station.getRect() != null && station.getName() != null) {
                        renderQueue.add(new RenderStationName(model, station));
                    }
                }
            }
        }
    }

    private void drawTransfers(Model model, ArrayList<RenderElement> renderQueue) {
        for (Transfer transfer : model.transfers) {
            renderQueue.add(new RenderTransferBackground(model, transfer));
            renderQueue.add(new RenderTransfer(model, transfer));
        }
    }

    private void drawLines(Model model, ArrayList<RenderElement> renderQueue) {
        HashSet<Segment> exclusions = new HashSet<Segment>();
        for (Line line : model.lines) {
            for (Segment segment : line.segments) {
                if (exclusions.contains(segment)) continue;
                if ((segment.getFlags() & Segment.INVISIBLE) == 0) {
                    Station from = segment.getFrom();
                    Station to = segment.getTo();
                    if (from.getPoint() != null || to.getPoint() != null) {
                        Segment opposite = line.getSegment(to, from);
                        Point[] additionalPoints = segment.getAdditionalNodes();
                        Point[] reversePoints = opposite == null ? null : opposite.getAdditionalNodes();
                        boolean additionalForward = additionalPoints != null;
                        boolean additionalBackward = reversePoints != null;
                        if (!additionalForward && additionalBackward) {
                        } else {
                            renderQueue.add(new RenderSegment(model, segment));
                            if (opposite != null) {
                                exclusions.add(opposite);
                            }
                        }
                    }
                }
            }
        }
    }

    public void clearVisibility() {
        final boolean[] visibility = mVisibility;
        final int count = visibility.length;
        for (int i = 0; i < count; i++) {
            visibility[i] = false;
        }
    }

    public void addVisibility(RectF viewport) {
        final int offset = 10;
        final Rect v = new Rect(
                (int) (viewport.left - offset),
                (int) (viewport.top - offset),
                (int) (viewport.right + offset),
                (int) (viewport.bottom + offset));
        final Rect[] bounds = mBounds;
        final boolean[] visibility = mVisibility;
        final int[] filters = mTypes;
        final int filter = mRenderFilter;
        final int count = bounds.length;
        for (int i = 0; i < count; i++) {
            final Rect box = new Rect(bounds[i]);

            visibility[i] |= ((filters[i] & filter) > 0) && Rect.intersects(v, box);
        }
    }
}
