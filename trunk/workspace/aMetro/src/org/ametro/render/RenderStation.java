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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;


public class RenderStation extends RenderElement {

    public int x;
    public int y;
    public float radiusFirst;
    public float radiusSecond;
    public Paint paintFirst;
    public Paint paintSecond;

    public RenderStation(SubwayMap subwayMap, SubwayStation station) {
        super();
        final boolean hasConnections = station.hasConnections();

        final int localX = station.point.x;
        final int localY = station.point.y;
        final int radius = subwayMap.stationDiameter / 2;
        final float localRadiusFirst = (float) radius;
        final Paint localPaintFirst = new Paint();
        final Paint localPaintSecond = new Paint();

        localPaintFirst.setColor(station.line.color);
        localPaintFirst.setStyle(Style.FILL);
        localPaintFirst.setAntiAlias(true);

        localPaintSecond.setColor(Color.WHITE);
        localPaintSecond.setAntiAlias(true);

        float localRadiusSecond;
        if (hasConnections) {
            localRadiusSecond = localRadiusFirst;
            localPaintSecond.setStyle(Style.STROKE);
            localPaintSecond.setStrokeWidth(0);
        } else {
            localRadiusSecond = localRadiusFirst * 0.7f;
            localPaintSecond.setStyle(Style.FILL);
        }

        x = localX;
        y = localY;
        radiusFirst = localRadiusFirst;
        radiusSecond = localRadiusSecond;
        paintFirst = localPaintFirst;
        paintSecond = localPaintSecond;

        setProperties(RenderProgram.TYPE_STATION, new Rect(localX - radius, localY - radius, localX + radius, localY + radius));
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radiusFirst, paintFirst);
        canvas.drawCircle(x, y, radiusSecond, paintSecond);
    }

}
