package org.ametro.render.elements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.render.RenderConstants;
import org.ametro.render.utils.RenderUtils;

public class StationElement extends DrawingElement {

    public MapPoint position;
    public float radiusInternal;
    public float radiusExternal;

    public Paint[] paints = new Paint[RenderConstants.LAYER_COUNT];
    public Paint backgroundPaint;

    public StationElement(final MapScheme scheme, final MapSchemeLine line, final MapSchemeStation station) {
        setUid(station.getUid());

        final int radius = (int) scheme.getStationsDiameter() / 2;
        position = station.getPosition();
        radiusInternal = radius * 0.80f;
        radiusExternal = radius * 1.10f;

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Style.FILL_AND_STROKE);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeWidth(2);

        final int lineColor = line.getLineColor();
        final boolean isWorking = station.isWorking();

        paints[RenderConstants.LAYER_VISIBLE] = createPaint(lineColor, radius, isWorking);
        paints[RenderConstants.LAYER_GRAYED] = createPaint(
                RenderUtils.getGrayedColor(lineColor), radius, isWorking);

        setBoxAndPriority(new Rect(
                        (int) (position.x - radius),
                        (int) (position.y - radius),
                        (int) (position.x + radius),
                        (int) (position.y + radius)
                ),
                RenderConstants.TYPE_STATION);
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(position.x, position.y, radiusExternal, backgroundPaint);
        canvas.drawCircle(position.x, position.y, radiusInternal, paints[layer]);
    }

    private Paint createPaint(int color, float radius, boolean isWorking) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(radius * 0.15f * 2);
        paint.setStyle(isWorking ? Style.FILL_AND_STROKE : Style.STROKE);
        return paint;
    }
}
