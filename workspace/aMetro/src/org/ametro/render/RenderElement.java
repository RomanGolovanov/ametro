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
import android.graphics.Rect;

public abstract class RenderElement implements Comparable<RenderElement> {

    public int Type;
    public Rect BoundingBox;
    public boolean isVisible;

    public void setProperties(int priority, Rect boundingBox) {
        this.Type = priority;
        this.BoundingBox = boundingBox;
    }

    public abstract void draw(Canvas canvas);

    public int compareTo(RenderElement another) {
        return Type - another.Type;
    }

}
