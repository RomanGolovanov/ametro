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
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwaySegment;
import org.ametro.model.SubwayStation;

public class RenderTransferBackground extends RenderElement {

    private int FromX;
    private int FromY;
    private int ToX;
    private int ToY;
    private float RadiusBig;
    private Paint Paint;

    public RenderTransferBackground(SubwayMap subwayMap, SubwaySegment transfer) {
        super();
        final SubwayStation fromStation = subwayMap.stations[transfer.fromStationId];
        final Point from = fromStation.point;
        FromX = from.x;
        FromY = from.y;

        final SubwayStation toStation = subwayMap.stations[transfer.toStationId];
        final Point to = toStation.point;
        ToX = to.x;
        ToY = to.y;

        final int lineWidth = subwayMap.linesWidth;
        final int radius = subwayMap.stationDiameter / 2;

        RadiusBig = (float) radius + 3.5f;

        final Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.FILL);
        paint.setStrokeWidth(lineWidth + 3.5f);
        paint.setAntiAlias(true);
        Paint = paint;

        final int left = Math.min(FromX, ToX) - radius;
        final int right = Math.max(FromX, ToX) + radius;
        final int top = Math.min(FromY, ToY) - radius;
        final int bottom = Math.max(FromY, ToY) + radius;

        setProperties(RenderProgram.TYPE_TRANSFER_BACKGROUND, new Rect(left, top, right, bottom));
    }

    public void setMode(boolean grayed)
    {
    	Paint.setAlpha(grayed ? 80 : 255);
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(FromX, FromY, RadiusBig, Paint);
        canvas.drawCircle(ToX, ToY, RadiusBig, Paint);
        canvas.drawLine(FromX, FromY, ToX, ToY, Paint);
    }

}
