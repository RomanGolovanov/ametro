package org.ametro.render;

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class RenderElement implements Comparable<RenderElement> {
	
	public int Type;
	public Rect BoundingBox;
	public boolean isVisible;

	public void setProperties(int priority, Rect boundingBox){
		this.Type = priority;
		this.BoundingBox = boundingBox;
	}
	
	public abstract void draw(Canvas canvas);

	@Override
	public int compareTo(RenderElement another) {
		return Type - another.Type;
	}
	
}
