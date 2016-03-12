package org.ametro.render.elements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.ametro.model.entities.MapScheme;
import org.ametro.render.RenderConstants;

public class BitmapBackgroundElement extends DrawingElement {

    private final Paint paint;
    private final Bitmap bitmap;

    public BitmapBackgroundElement(MapScheme scheme, Bitmap bitmap) {
        super();
        this.bitmap = bitmap;
        paint = new Paint();
        setBoxAndPriority(new Rect(0, 0, scheme.getWidth(), scheme.getHeight()), RenderConstants.TYPE_BACKGROUND);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
}
