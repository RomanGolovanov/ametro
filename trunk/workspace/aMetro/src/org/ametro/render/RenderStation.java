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
import org.ametro.model.Model;
import org.ametro.model.Station;


public class RenderStation extends RenderElement {

    public int X;
    public int Y;
    public float RadiusFirst;
    public float RadiusSecond;
    public Paint PaintFirst;
    public Paint PaintSecond;

    public RenderStation(Model model, Station station) {
        super();
        final boolean hasConnections = station.hasConnections();

        final int x = station.getPoint().x;
        final int y = station.getPoint().y;
        final int radius = model.stationDiameter / 2;
        final float radiusFirst = (float) radius;
        final Paint paintFirst = new Paint();
        final Paint paintSecond = new Paint();

        paintFirst.setColor(station.getLine().color);
        paintFirst.setStyle(Style.FILL);
        paintFirst.setAntiAlias(true);

        paintSecond.setColor(Color.WHITE);
        paintSecond.setAntiAlias(true);

        float radiusSecond;
        if (hasConnections) {
            radiusSecond = radiusFirst;
            paintSecond.setStyle(Style.STROKE);
            paintSecond.setStrokeWidth(0);
        } else {
            radiusSecond = radiusFirst * 0.7f;
            paintSecond.setStyle(Style.FILL);
        }

        this.X = x;
        this.Y = y;
        this.RadiusFirst = radiusFirst;
        this.RadiusSecond = radiusSecond;
        this.PaintFirst = paintFirst;
        this.PaintSecond = paintSecond;

        setProperties(RenderProgram.TYPE_STATION, new Rect(x - radius, y - radius, x + radius, y + radius));
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(X, Y, RadiusFirst, PaintFirst);
        canvas.drawCircle(X, Y, RadiusSecond, PaintSecond);
    }

}
