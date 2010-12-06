/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.render;

import org.ametro.model.LineView;
import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransportSegment;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.util.ModelUtil;
import org.ametro.util.ExtendedPath;

import android.graphics.Canvas;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;


public class RenderSegment extends RenderElement {

	private int colorNormal;
	private int colorGrayed;
	
    public Paint paint;
    public ExtendedPath path;

    public RenderSegment(SchemeView map, SegmentView view, TransportSegment segment) {
        super();
        final LineView lineView = map.lines[ view.lineViewId ];
        final StationView from = map.stations[view.stationViewFromId];
        final StationView to = map.stations[view.stationViewToId];

        final Paint localPaint = new Paint();
        final ExtendedPath localPath = new ExtendedPath();

        final Integer delay = segment.delay;
        final boolean lineWorking = (delay != null && delay > 0);
        final int lineWidth = lineView.lineWidth;

        localPaint.setStyle(Style.STROKE);
        localPaint.setAntiAlias(true);

        if (lineWorking) {
            localPaint.setStrokeWidth(lineWidth);
            localPaint.setPathEffect(new CornerPathEffect(lineWidth * 0.2f));
        } else {
            localPaint.setStrokeWidth(lineWidth * 0.75f);
            localPaint.setPathEffect(new ComposePathEffect(
                    new DashPathEffect(new float[]{lineWidth * 0.8f, lineWidth * 0.2f}, 0),
                    new CornerPathEffect(lineWidth * 0.2f)
            ));
        }
        colorNormal = map.lines[view.lineViewId].lineColor;
        colorGrayed = RenderProgram.getGrayedColor(colorNormal);
        localPaint.setColor(colorNormal);

        paint = localPaint;
        drawSegmentPath(map, view, segment, from, to, localPath);
        path = localPath;

        final int minx = Math.min(from.stationPoint.x, to.stationPoint.x) - lineWidth;
        final int maxx = Math.max(from.stationPoint.x, to.stationPoint.x) + lineWidth;
        final int miny = Math.min(from.stationPoint.y, to.stationPoint.y) - lineWidth;
        final int maxy = Math.max(from.stationPoint.y, to.stationPoint.y) + lineWidth;
        final Rect box = new Rect(minx, miny, maxx, maxy);
        final ModelPoint[] points = view.spline!=null ? view.spline.points : null;
        if (points != null) {
            final int length = points.length;
            for (int i = 0; i < length; i++) {
                final ModelPoint node = points[i];
                box.union(node.x, node.y);
            }
        }
        setProperties(lineWorking ? RenderProgram.TYPE_LINE + view.id : RenderProgram.TYPE_LINE_DASHED + view.id, box);
    }
    
    private void drawSegmentPath(SchemeView map, SegmentView view, TransportSegment segment, StationView from, StationView to, ExtendedPath path) {
        final ModelPoint pointFrom = from.stationPoint;
        final ModelPoint pointTo = to.stationPoint;
        final ModelPoint[] modelPoints = view.spline!=null ? view.spline.points : null;
        if (modelPoints != null) {
            if ( view.spline.isSpline ) {
                Point[] points = new Point[modelPoints.length + 2];
                points[0] = ModelUtil.toPoint(pointFrom);
                for (int i = 0; i < modelPoints.length; i++) {
                    points[i + 1] = ModelUtil.toPoint(modelPoints[i]);
                }
                points[points.length - 1] = ModelUtil.toPoint(pointTo);
                path.drawSpline(points, 0, points.length);
            } else {
                path.moveTo(pointFrom.x, pointFrom.y);
                for (ModelPoint p : modelPoints) {
                    path.lineTo(p.x, p.y);
                }
                path.lineTo(pointTo.x, pointTo.y);
            }
        } else {
            path.moveTo(pointFrom.x, pointFrom.y);
            path.lineTo(pointTo.x, pointTo.y);
        }
    }

    public void setAntiAlias(boolean enabled)
    {
    	paint.setAntiAlias(enabled);
    }
    
    protected void setMode(boolean grayed)
    {
    	//paint.setAlpha(grayed ?  80 : 255);
   		paint.setColor(grayed ? colorGrayed : colorNormal);
   		paint.setAlpha(255);
    }

    public void draw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

}
