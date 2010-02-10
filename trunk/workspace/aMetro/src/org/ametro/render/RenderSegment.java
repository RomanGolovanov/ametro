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
import org.ametro.model.Line;
import org.ametro.model.Model;
import org.ametro.model.Segment;
import org.ametro.model.Station;


public class RenderSegment extends RenderElement {

    public Paint Paint;
    public ExtendedPath Path;

    public RenderSegment(Model model, Segment segment) {
        super();
        final Station from = segment.getFrom();
        final Station to = segment.getTo();
        final Line line = from.getLine();

        final Paint paint = new Paint();
        final ExtendedPath path = new ExtendedPath();

        final Double delay = segment.getDelay();
        final boolean lineWorking = (delay != null && delay > 0);
        final int lineWidth = model.getLinesWidth();

        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);

        if (lineWorking) {
            paint.setStrokeWidth(lineWidth);
            paint.setPathEffect(new CornerPathEffect(lineWidth * 0.6f));
        } else {
            paint.setStrokeWidth(lineWidth * 0.75f);
            paint.setPathEffect(new ComposePathEffect(
                    new DashPathEffect(new float[]{lineWidth * 0.8f, lineWidth * 0.2f}, 0),
                    new CornerPathEffect(lineWidth * 0.6f)
            ));
        }
        paint.setColor(segment.getFrom().getLine().getColor());

        this.Paint = paint;
        drawSegmentPath(line, segment, from, to, path);
        this.Path = path;

        final int minx = Math.min(from.getPoint().x, to.getPoint().x) - lineWidth;
        final int maxx = Math.max(from.getPoint().x, to.getPoint().x) + lineWidth;
        final int miny = Math.min(from.getPoint().y, to.getPoint().y) - lineWidth;
        final int maxy = Math.max(from.getPoint().y, to.getPoint().y) + lineWidth;
        final Rect box = new Rect(minx, miny, maxx, maxy);
        final Point[] nodes = segment.getAdditionalNodes();
        if (nodes != null) {
            final int length = nodes.length;
            for (int i = 0; i < length; i++) {
                final Point node = nodes[i];
                box.union(node.x, node.y);
            }
        }
        setProperties(RenderProgram.TYPE_LINE, box);
    }

    private void drawSegmentPath(Line line, Segment segment, Station from, Station to, ExtendedPath path) {
        final Point pointFrom = from.getPoint();
        final Point pointTo = to.getPoint();
        final Point[] additionalPoints = segment.getAdditionalNodes();
        if (additionalPoints != null) {
            if ((segment.getFlags() & Segment.SPLINE) != 0) {
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
                for (int i = 0; i < additionalPoints.length; i++) {
                    path.lineTo(additionalPoints[i].x, additionalPoints[i].y);
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
        canvas.drawPath(Path, Paint);
    }

}
