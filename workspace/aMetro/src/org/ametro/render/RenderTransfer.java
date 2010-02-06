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
import org.ametro.model.Model;
import org.ametro.model.Station;
import org.ametro.model.Transfer;

public class RenderTransfer extends RenderElement {

    private int FromX;
    private int FromY;

    private int ToX;
    private int ToY;

    private float Radius;
    private Paint Paint;


    public RenderTransfer(Model model, Transfer transfer) {
        super();
        final Station fromStation = transfer.getFrom();
        final Point from = fromStation.getPoint();
        FromX = from.x;
        FromY = from.y;

        final Station toStation = transfer.getTo();
        final Point to = toStation.getPoint();
        ToX = to.x;
        ToY = to.y;

        final int radius = model.getStationDiameter() / 2;

        final int lineWidth = model.getLinesWidth();
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

        setProperties(RenderProgram.TYPE_TRANSFER, new Rect(left, top, right, bottom));
    }


    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(FromX, FromY, Radius, Paint);
        canvas.drawCircle(ToX, ToY, Radius, Paint);
        canvas.drawLine(FromX, FromY, ToX, ToY, Paint);
    }

}
