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

import org.ametro.model.SchemeView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.TransportTransfer;
import org.ametro.model.ext.ModelPoint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;

public class RenderTransfer extends RenderElement {

    private int FromX;
    private int FromY;

    private int ToX;
    private int ToY;

    private float Radius;
    private Paint Paint;


    public RenderTransfer(SchemeView map, TransferView view, TransportTransfer transfer) {
        super();
        final StationView fromStation = map.stations[view.stationViewFromId];
        final ModelPoint from = fromStation.stationPoint;
        FromX = from.x;
        FromY = from.y;

        final StationView toStation = map.stations[view.stationViewToId];
        final ModelPoint to = toStation.stationPoint;
        ToX = to.x;
        ToY = to.y;

        final int radius = map.stationDiameter / 2;

        final int lineWidth = map.lineWidth;
        final Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.FILL);
        paint.setStrokeWidth(lineWidth + 1.2f);
        paint.setAntiAlias(true);
        Paint = paint;

        Radius = radius + 2.2f;

        final int left = Math.min(FromX, ToX) - radius;
        final int right = Math.max(FromX, ToX) + radius;
        final int top = Math.min(FromY, ToY) - radius;
        final int bottom = Math.max(FromY, ToY) + radius;

        setProperties(RenderProgram.TYPE_TRANSFER + view.id, new Rect(left, top, right, bottom));
    }

    public void setAntiAlias(boolean enabled)
    {
    	Paint.setAntiAlias(enabled);
    }
    
    protected void setMode(boolean grayed)
    {
    	//Paint.setAlpha(grayed ? 80 : 255);
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(FromX, FromY, Radius, Paint);
        canvas.drawCircle(ToX, ToY, Radius, Paint);
        canvas.drawLine(FromX, FromY, ToX, ToY, Paint);
    }

}
