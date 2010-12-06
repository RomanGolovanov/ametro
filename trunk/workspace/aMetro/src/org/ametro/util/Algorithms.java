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

package org.ametro.util;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class Algorithms {

    private final static int LEFT = 1;
    private final static int RIGHT = 2;
    private final static int TOP = 4;
    private final static int BOTTOM = 8;

    public static class Solve2x2 {
        float __determinant = 0;

        public PointF solve(float _a11, float _a12, float _a21, float _a22, float _b1, float _b2, float zeroTolerance, boolean _resolve) {
            if (!_resolve) {
                __determinant = _a11 * _a22 - _a12 * _a21;
            }

            // exercise - dispatch an event if the determinant is near zero
            if (__determinant > zeroTolerance) {
                float x = (_a22 * _b1 - _a12 * _b2) / __determinant;
                float y = (_a11 * _b2 - _a21 * _b1) / __determinant;
                return new PointF(x, y);
            }
            return null;
        }

    }

    public static class QBezierControls {
        public final float x0;
        public final float y0;
        public final float x1;
        public final float y1;

        public QBezierControls(float newX0, float newY0, float newX1, float newY1) {
            super();
            x0 = newX0;
            y0 = newY0;
            x1 = newX1;
            y1 = newY1;
        }

    }

    public static float calculateDistance(Point p0, Point p1) {
        int dx = p0.x - p1.x;
        int dy = p0.y - p1.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float calculateAngle(float x0, float y0, float x, float y) {
        float angle = (float) (Math.atan((y - y0) / (x - x0)) / Math.PI * 180);
        float dx = x - x0;
        float dy = y - y0;
        if (angle > 0) {
            if (dx < 0 && dy < 0) {
                angle += 180;
            }
        } else if (angle < 0) {
            if (dx < 0 && dy > 0) {
                angle += 180;
            } else {
                angle += 360;
            }
        } else {
            if (dx < 0) {
                angle = 180;
            }
        }
        return angle;
    }

    public static PointF interpolateQuadBezier(Point p0, Point p1, Point p2) {
        // compute t-value using chord-length parameterization
        float dx = p1.x - p0.x;
        float dy = p1.y - p0.y;
        float d1 = (float) Math.sqrt(dx * dx + dy * dy);
        float d = d1;

        dx = p2.x - p1.x;
        dy = p2.y - p1.y;
        d += (float) Math.sqrt(dx * dx + dy * dy);

        float t = d1 / d;
        float t1 = 1.0f - t;
        float tSq = t * t;
        float denom = 2.0f * t * t1;

        PointF p = new PointF();
        p.x = (p1.x - t1 * t1 * p0.x - tSq * p2.x) / denom;
        p.y = (p1.y - t1 * t1 * p0.y - tSq * p2.y) / denom;

        return p;
    }

    public static QBezierControls interpolateCubeBezierSmooth(Point p0, Point p1, Point p2, Point p3, float smoothFactor) {
        // Assume we need to calculate the control
        // points between (x1,y1) and (x2,y2).
        // Then x0,y0 - the previous vertex,
        //      x3,y3 - the next one.

        float x0 = p0.x;
        float y0 = p0.y;
        float x1 = p1.x;
        float y1 = p1.y;
        float x2 = p2.x;
        float y2 = p2.y;
        float x3 = p3.x;
        float y3 = p3.y;

        float xc1 = (x0 + x1) / 2.0f;
        float yc1 = (y0 + y1) / 2.0f;
        float xc2 = (x1 + x2) / 2.0f;
        float yc2 = (y1 + y2) / 2.0f;
        float xc3 = (x2 + x3) / 2.0f;
        float yc3 = (y2 + y3) / 2.0f;

        float len1 = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        float len2 = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        float len3 = (float) Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 - y2));

        float k1 = len1 / (len1 + len2);
        float k2 = len2 / (len2 + len3);

        float xm1 = xc1 + (xc2 - xc1) * k1;
        float ym1 = yc1 + (yc2 - yc1) * k1;
        float xm2 = xc2 + (xc3 - xc2) * k2;
        float ym2 = yc2 + (yc3 - yc2) * k2;

        float ctrl1_x = xm1 + (xc2 - xm1) * smoothFactor + x1 - xm1;
        float ctrl1_y = ym1 + (yc2 - ym1) * smoothFactor + y1 - ym1;
        float ctrl2_x = xm2 + (xc2 - xm2) * smoothFactor + x2 - xm2;
        float ctrl2_y = ym2 + (yc2 - ym2) * smoothFactor + y2 - ym2;
        return new QBezierControls(ctrl1_x, ctrl1_y, ctrl2_x, ctrl2_y);
    }

    public static int vcode(Rect r, Point p) {
        return (((p.x < r.left) ? LEFT : 0) +
                ((p.x > r.right) ? RIGHT : 0) +
                ((p.y < r.top) ? TOP : 0) +
                ((p.y > r.bottom) ? BOTTOM : 0));
    }

    public static boolean clipCohenSutherland(Rect r, Point a, Point b) {
        a = new Point(a);
        b = new Point(b);
        int code_a, code_b, code;
        Point c;
        code_a = vcode(r, a);
        code_b = vcode(r, b);
        while (code_a != 0 || code_b != 0) {
            if ((code_a & code_b) != 0)
                return false;
            if (code_a != 0) {
                code = code_a;
                c = a;
            } else {
                code = code_b;
                c = b;
            }
            if ((code & LEFT) != 0) {
                c.y += (a.y - b.y) * (r.left - c.x) / (a.x - b.x);
                c.x = r.left;
            } else if ((code & RIGHT) != 0) {
                c.y += (a.y - b.y) * (r.right - c.x) / (a.x - b.x);
                c.x = r.right;
            }
            if ((code & TOP) != 0) {
                c.x += (a.x - b.x) * (r.top - c.y) / (a.y - b.y);
                c.y = r.top;
            } else if ((code & BOTTOM) != 0) {
                c.x += (a.x - b.x) * (r.bottom - c.y) / (a.y - b.y);
                c.y = r.bottom;
            }
            if (code == code_a)
                code_a = vcode(r, a);
            else
                code_b = vcode(r, b);
        }
        return true;
    }

    public static QBezierControls interpolateCubicBezierControl(Point p0, Point p1, Point p2, Point p3) {
        return interpolateCubeBezierSmooth(p0, p1, p2, p3, 1.0f);
        //		int __p0X        = p0.x;
//		int __p0Y        = p0.y;
//		int __p3X         = p3.x;
//		int __p3Y         = p3.y;
//
//		// currently, this method auto-parameterizes the curve using chord-length parameterization.  
//		// A future version might allow inputting the two t-values, but this is more
//		// user-friendly (what an over-used term :)  As an exercise, try uniform parameterization - t1 = 13/ and 52 = 2/3.
//		int deltaX = p1.x - p0.x;
//		int deltaY = p1.y - p0.y;
//		float d1     = (float)Math.sqrt(deltaX*deltaX + deltaY*deltaY);
//
//		deltaX        = p2.x - p1.x;
//		deltaY        = p2.y - p1.y;
//		float d2     = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY);
//
//		deltaX        = p3.x - p2.x;
//		deltaY        = p3.y - p2.y;
//		float d3     = (float)Math.sqrt(deltaX*deltaX + deltaY*deltaY);
//
//		float d = d1 + d2 + d3;
//		float __t1 = d1/d;
//		float __t2 = (d1+d2)/d;
//
//		// there are four unknowns (x- and y-coords for P1 and P2), which are solved as two separate sets of two equations in two unknowns
//		float t12 = __t1*__t1;
//		float t13 = __t1*t12;
//
//		float t22 = __t2*__t2;
//		float t23 = __t2*t22;
//
//		// x-coordinates of P1 and P2 (t = t1 and t2) - exercise: eliminate redudant 
//		// computations in these equations
//		float a11 = 3*t13 - 6*t12 + 3*__t1;
//		float a12 = -3*t13 + 3*t12;
//		float a21 = 3*t23 - 6*t22 + 3*__t2;
//		float a22 = -3*t23 + 3*t22;
//
//		float b1 = -t13*__p3X + __p0X*(t13 - 3*t12 + 3*__t1 -1) + p1.x;
//		float b2 = -t23*__p3X + __p0X*(t23 - 3*t22 + 3*__t2 -1) + p2.x;
//
//		Solve2x2 s = new Solve2x2();
//		PointF p = s.solve(a11, a12, a21, a22, b1, b2, 0, false);
//
//		float __p1X       = p.x;
//		float __p2X       = p.y;
//
//		// y-coordinates of P1 and P2 (t = t1 and t2)      
//		b1 = -t13*__p3Y + __p0Y*(t13 - 3*t12 + 3*__t1 -1) + p1.y;
//		b2 = -t23*__p3Y + __p0Y*(t23 - 3*t22 + 3*__t2 -1) + p2.y;
//
//		// resolving with same coefficients, but new RHS
//		p     = s.solve(a11, a12, a21, a22, b1, b2, ZERO_TOLERANCE, true);
//		float __p1Y = p.x;
//		float __p2Y = p.y;
//
//		return new QBezierControls(__p1X, __p1Y, __p2X, __p2Y);
    }

}
