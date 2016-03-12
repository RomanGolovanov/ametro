package org.ametro.render.utils;

import android.graphics.Color;

public class RenderUtils {

    public static int getGrayedColor(int color) {
        if(color == Color.BLACK){
            return 0xFFd0d0d0;
        }
        float r = (float)Color.red(color) / 255;
        float g = (float)Color.green(color) / 255;
        float b = (float)Color.blue(color) / 255;
        float t = 0.8f;
        r = r*(1-t) + 1.0f * t;
        g = g*(1-t) + 1.0f * t;
        b = b*(1-t) + 1.0f * t;
        return Color.argb(0xFF, (int)Math.min(r * 255,255), (int)Math.min(g * 255,255), (int)Math.min(b * 255,255));
    }


}
