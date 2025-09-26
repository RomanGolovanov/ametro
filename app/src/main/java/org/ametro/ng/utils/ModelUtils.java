package org.ametro.ng.utils;

import android.graphics.Rect;

import org.ametro.ng.model.entities.MapRect;

public class ModelUtils {

    public static Rect toRect(MapRect r) {
        if (r == null) return null;
        return new Rect(r.x, r.y, r.x + r.width, r.y + r.height);
    }

}
