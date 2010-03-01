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

import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;


public class RenderStation extends RenderElement {

    public int x;
    public int y;
    public float radiusInternal;
    public float radiusExternal;
    public Paint paintColor;
    public Paint paintBackGround;

    public RenderStation(SubwayMap subwayMap, SubwayStation station) {
        super();
        final boolean hasConnections = subwayMap.hasConnections(station);

        final int localX = station.point.x;
        final int localY = station.point.y;
        final int radius = subwayMap.stationDiameter / 2;

        final Paint localPaintColor = new Paint();

        paintBackGround = new Paint();
        paintBackGround.setColor(Color.WHITE);
        paintBackGround.setStyle(Style.FILL_AND_STROKE);
        paintBackGround.setAntiAlias(true);
        paintBackGround.setStrokeWidth(1);
        
        localPaintColor.setColor(subwayMap.lines[station.lineId].color);
        localPaintColor.setAntiAlias(true);
        localPaintColor.setStrokeWidth(radius * 0.15f * 2);

        if (hasConnections) {
            localPaintColor.setStyle(Style.FILL_AND_STROKE);
        } else {
        	localPaintColor.setStyle(Style.STROKE);
        }

        x = localX;
        y = localY;
        radiusInternal = radius * 0.80f;
        radiusExternal = radius * 1.10f;
        paintColor = localPaintColor;

        setProperties(RenderProgram.TYPE_STATION, new Rect(localX - radius, localY - radius, localX + radius, localY + radius));
    }

    public void setMode(boolean grayed)
    {
    	paintColor.setAlpha(grayed ?  80 : 255);
    }
    
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radiusExternal, paintBackGround);
        canvas.drawCircle(x, y, radiusInternal, paintColor);
    }

}
