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

package org.ametro.libs;

import android.graphics.*;

public class ExtendedGraphic2D {

    public static void drawCircleArc(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3, Paint linePaint) {
        Path path = new Path();
        drawCircleArc(path, x1, y1, x2, y2, x3, y3);
        canvas.drawPath(path, linePaint);
    }

    public static void drawCircleArc(Path path, float x1, float y1, float x2, float y2, float x3, float y3) {
        float x12 = (x1 + x2) / 2;
        float y12 = (y1 + y2) / 2;
        float x23 = (x2 + x3) / 2;
        float y23 = (y2 + y3) / 2;
        float k12 = -(x1 - x2) / (y1 - y2);
        float b12 = y12 - k12 * x12;
        float k23 = -(x2 - x3) / (y2 - y3);
        float b23 = y23 - k23 * x23;

        float y0 = (k12 * b23 - k23 * b12) / (k12 - k23);
        float x0 = (y0 - b12) / k12;

        float R = (float) Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));

        float angle1 = Algorithms.calculateAngle(x0, y0, x1, y1); //(float)(Math.atan( (y1-y0)/(x1-x0) ) / Math.PI * 180);
        float angle3 = Algorithms.calculateAngle(x0, y0, x3, y3); //(float)(Math.atan( (y3-y0)/(x3-x0) ) / Math.PI * 180);

        float startAngle = Math.min(angle1, angle3);
        float endAngle = Math.max(angle1, angle3);
        float sweepAngle = endAngle - startAngle;
        if (startAngle < 90 && endAngle > 270) {
            sweepAngle = 360 - endAngle + startAngle;
            startAngle = endAngle;
        }
        RectF oval = new RectF(x0 - R, y0 - R, x0 + R, y0 + R);
        path.arcTo(oval, startAngle, sweepAngle, true);
    }

    public static void drawLineClipped(Canvas canvas, Rect r, Point p0, Point p1, Paint paint, boolean clipInner) {
        ExtendedPath path = new ExtendedPath();
        path.moveTo(p0.x, p0.y);
        path.lineToClipped(r, p0, p1, clipInner);
    }


}
