package org.ametro.render.elements;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeTransfer;
import org.ametro.render.RenderConstants;

public class TransferElement extends DrawingElement {

    private MapPoint from;
    private MapPoint to;

    private float radius;
    private Paint paint;

    public TransferElement(MapScheme scheme, MapSchemeTransfer transfer){
        setUid(transfer.getUid());
        from = transfer.getFromStationPosition();
        to = transfer.getToStationPosition();

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth((float)scheme.getLinesWidth() + 1.2f);
        paint.setAntiAlias(true);

        radius = (float)scheme.getStationsDiameter() / 2 + 2.2f;

        setBoxAndPriority(new Rect(
                (int) (Math.min(from.x, to.x) - radius),
                (int) (Math.min(from.y, to.y) - radius),
                (int) (Math.max(from.x, to.x) + radius),
                (int) (Math.max(from.y, to.y) + radius)
        ), RenderConstants.TYPE_TRANSFER);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(from.x, from.y, radius, paint);
        canvas.drawCircle(to.x, to.y, radius, paint);
        canvas.drawLine(from.x, from.y, to.x, to.y, paint);
    }
}
