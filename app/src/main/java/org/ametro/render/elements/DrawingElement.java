package org.ametro.render.elements;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;


public abstract class DrawingElement implements Comparable<DrawingElement> {

    private Integer uid;

    private Rect boundingBox;

    private int priority;

    protected int layer;

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public abstract void draw(Canvas canvas);

    protected void setBoxAndPriority(Rect boundingBox, int priority) {
        this.boundingBox = boundingBox;
        this.priority = priority;
    }

    public int compareTo(@NonNull DrawingElement another) {
        int byLayer = this.layer - another.layer;
        if (byLayer != 0) {
            return byLayer;
        }

        int byPriority = this.priority - another.priority;
        if (byPriority != 0) {
            return byPriority;
        }

        return 0;
    }

}
