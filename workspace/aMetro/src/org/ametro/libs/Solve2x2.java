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

import android.graphics.PointF;

class Solve2x2 {
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
