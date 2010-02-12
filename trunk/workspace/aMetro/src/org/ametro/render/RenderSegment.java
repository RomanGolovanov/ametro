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
import android.graphics.Paint.Style;
import org.ametro.graphics.ExtendedPath;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwaySegment;
import org.ametro.model.SubwayStation;


public class RenderSegment extends RenderElement {

    public Paint paint;
    public ExtendedPath path;

    public RenderSegment(SubwayMap subwayMap, SubwaySegment segment) {
        super();
        final SubwayStation from = segment.from;
        final SubwayStation to = segment.to;

        final Paint localPaint = new Paint();
        final ExtendedPath localPath = new ExtendedPath();

        final Double delay = segment.delay;
        final boolean lineWorking = (delay != null && delay > 0);
        final int lineWidth = subwayMap.linesWidth;

        localPaint.setStyle(Style.STROKE);
        localPaint.setAntiAlias(true);

        if (lineWorking) {
            localPaint.setStrokeWidth(lineWidth);
            localPaint.setPathEffect(new CornerPathEffect(lineWidth * 0.6f));
        } else {
            localPaint.setStrokeWidth(lineWidth * 0.75f);
            localPaint.setPathEffect(new ComposePathEffect(
                    new DashPathEffect(new float[]{lineWidth * 0.8f, lineWidth * 0.2f}, 0),
                    new CornerPathEffect(lineWidth * 0.6f)
            ));
        }
        localPaint.setColor(segment.from.line.color);

        paint = localPaint;
        drawSegmentPath(subwayMap, segment, from, to, localPath);
        path = localPath;

        final int minx = Math.min(from.point.x, to.point.x) - lineWidth;
        final int maxx = Math.max(from.point.x, to.point.x) + lineWidth;
        final int miny = Math.min(from.point.y, to.point.y) - lineWidth;
        final int maxy = Math.max(from.point.y, to.point.y) + lineWidth;
        final Rect box = new Rect(minx, miny, maxx, maxy);
        final Point[] nodes = subwayMap.getSegmentsNodes(segment.id);
        if (nodes != null) {
            final int length = nodes.length;
            for (int i = 0; i < length; i++) {
                final Point node = nodes[i];
                box.union(node.x, node.y);
            }
        }
        setProperties(RenderProgram.TYPE_LINE, box);
    }

    private void drawSegmentPath(SubwayMap map, SubwaySegment segment, SubwayStation from, SubwayStation to, ExtendedPath path) {
        final Point pointFrom = from.point;
        final Point pointTo = to.point;
        final Point[] additionalPoints = map.getSegmentsNodes(segment.id);
        if (additionalPoints != null) {
            if ((segment.flags & SubwaySegment.SPLINE) != 0) {
                Point[] points = new Point[additionalPoints.length + 2];
                points[0] = pointFrom;
                points[points.length - 1] = pointTo;
                for (int i = 0; i < additionalPoints.length; i++) {
                    Point point = additionalPoints[i];
                    points[i + 1] = point;
                }
                path.drawSpline(points, 0, points.length);
            } else {
                path.moveTo(pointFrom.x, pointFrom.y);
                for (Point additionalPoint : additionalPoints) {
                    path.lineTo(additionalPoint.x, additionalPoint.y);
                }
                path.lineTo(pointTo.x, pointTo.y);
            }
        } else {
            path.moveTo(pointFrom.x, pointFrom.y);
            path.lineTo(pointTo.x, pointTo.y);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

}
