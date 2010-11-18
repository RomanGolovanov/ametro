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
import org.ametro.model.TransportStation;

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
    
    private int colorNormal;
    private int colorGrayed;
    
    public RenderStation(SchemeView map, StationView view, TransportStation station) {
        super();
        final boolean hasConnections = map.hasConnections(view);

        final int localX = view.stationPoint.x;
        final int localY = view.stationPoint.y;
        final int radius = map.stationDiameter / 2;

        colorNormal = map.lines[view.lineViewId].lineColor;
        colorGrayed = RenderProgram.getGrayedColor(colorNormal);
        
        final Paint localPaintColor = new Paint();

        paintBackGround = new Paint();
        paintBackGround.setColor(Color.WHITE);
        paintBackGround.setStyle(Style.FILL_AND_STROKE);
        paintBackGround.setAntiAlias(true);
        paintBackGround.setStrokeWidth(1);
        
        localPaintColor.setColor(colorNormal);
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

        setProperties(RenderProgram.TYPE_STATION + view.id, new Rect(localX - radius, localY - radius, localX + radius, localY + radius));
    }

    public void setAntiAlias(boolean enabled)
    {
    	paintColor.setAntiAlias(enabled);
    	paintBackGround.setAntiAlias(enabled);
    }

    protected void setMode(boolean grayed)
    {
    	paintColor.setColor(grayed ? colorGrayed : colorNormal);
    	paintColor.setAlpha(255);
    }
    
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radiusExternal, paintBackGround);
        canvas.drawCircle(x, y, radiusInternal, paintColor);
    }

}
