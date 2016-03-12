package org.ametro.render.elements;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeTransfer;
import org.ametro.render.RenderConstants;
import org.ametro.render.utils.RenderUtils;

public class TransferBackgroundElement extends DrawingElement {

    private MapPoint from;
    private MapPoint to;

    private float radius;
    private Paint[] paints = new Paint[RenderConstants.LAYER_COUNT];

    public TransferBackgroundElement(MapScheme scheme, MapSchemeTransfer transfer) {
        setUid(transfer.getUid());

        from = transfer.getFromStationPosition();
        to = transfer.getToStationPosition();

        radius = (float) scheme.getStationsDiameter() / 2 + 3.5f;

        final float linesWidth = (float) scheme.getLinesWidth();

        paints[RenderConstants.LAYER_VISIBLE] = createPaint(Color.BLACK, linesWidth);
        paints[RenderConstants.LAYER_GRAYED] = createPaint(
                RenderUtils.getGrayedColor(Color.BLACK), linesWidth);

        setBoxAndPriority(new Rect(
                (int) (Math.min(from.x, to.x) - radius),
                (int) (Math.min(from.y, to.y) - radius),
                (int) (Math.max(from.x, to.x) + radius),
                (int) (Math.max(from.y, to.y) + radius)
        ), RenderConstants.TYPE_TRANSFER_BACKGROUND);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(from.x, from.y, radius, paints[layer]);
        canvas.drawCircle(to.x, to.y, radius, paints[layer]);
        canvas.drawLine(from.x, from.y, to.x, to.y, paints[layer]);
    }

    private Paint createPaint(int color, float linesWidth) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(linesWidth + 4.5f);
        paint.setAntiAlias(true);
        return paint;
    }

}
