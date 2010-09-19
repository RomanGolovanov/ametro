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

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class RenderElement implements Comparable<RenderElement> {

    public int type;
    public Rect boundingBox;
    public int selected;

    public void setProperties(int newPriority, Rect newBoundingBox) {
        type = newPriority;
        boundingBox = newBoundingBox;
        selected = 0;
    }

    public void setSelection(boolean selected){
    	this.selected = selected ? 1 : 0;
    	setMode(!selected);
    }
    
    protected abstract void setMode(boolean grayed);
    public abstract void setAntiAlias(boolean enabled);
    
    public abstract void draw(Canvas canvas);

    public int compareTo(RenderElement another) {
    	int base = this.selected - another.selected;
        return base != 0 ? base : type - another.type;
    }

}
