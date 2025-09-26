package org.ametro.ng.render.elements;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;

import org.ametro.ng.model.entities.MapScheme;
import org.ametro.ng.render.RenderConstants;

public class PictureBackgroundElement extends DrawingElement {

    private final Picture picture;

    public PictureBackgroundElement(MapScheme scheme, Picture picture) {
        super();
        this.picture = picture;
        setBoxAndPriority(new Rect(0, 0, scheme.getWidth(), scheme.getHeight()), RenderConstants.TYPE_BACKGROUND);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPicture(picture);
    }
}
