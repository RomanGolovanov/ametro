package org.ametro.render.elements;

import android.graphics.Canvas;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeSegment;
import org.ametro.render.RenderConstants;
import org.ametro.render.utils.RenderUtils;


public class SegmentElement extends DrawingElement {

    public final Paint[] paints = new Paint[RenderConstants.LAYER_COUNT];

    public final Path path;

    public SegmentElement(final MapSchemeLine line, final MapSchemeSegment segment) {
        setUid(segment.getUid());

        final int lineColor = line.getLineColor();
        final boolean isWorking = segment.isWorking();
        final float lineWidth = (float) line.getLineWidth();

        paints[RenderConstants.LAYER_VISIBLE] = createPaint(lineColor, lineWidth, isWorking);

        paints[RenderConstants.LAYER_GRAYED] = createPaint(
                RenderUtils.getGrayedColor(lineColor), lineWidth, isWorking);

        final MapPoint[] points = segment.getPoints();

        final int minX = (int)(Math.min(points[0].x, points[points.length-1].x) - lineWidth);
        final int maxX = (int)(Math.max(points[0].x, points[points.length-1].x) + lineWidth);
        final int minY = (int)(Math.min(points[0].y, points[points.length-1].y) - lineWidth);
        final int maxY = (int)(Math.max(points[0].y, points[points.length-1].y) + lineWidth);
        final Rect box = new Rect(minX, minY, maxX, maxY);

        path = new Path();
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i++) {
            final MapPoint p = points[i];
            path.lineTo(p.x, p.y);
            box.union((int)p.x, (int)p.y);
        }
        setBoxAndPriority(box, segment.isWorking() ? RenderConstants.TYPE_LINE : RenderConstants.TYPE_LINE_DASHED);
    }

    public void draw(Canvas canvas) {
        canvas.drawPath(path, paints[layer]);
    }

    private Paint createPaint(int color, float lineWidth, boolean isWorking){
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(color);

        if (isWorking) {
            paint.setStrokeWidth(lineWidth);
            paint.setPathEffect(
                    new CornerPathEffect(lineWidth * 0.2f));
        } else {
            paint.setStrokeWidth(lineWidth * 0.75f);
            paint.setPathEffect(new ComposePathEffect(
                    new DashPathEffect(new float[]{lineWidth * 0.8f, lineWidth * 0.2f}, 0),
                    new CornerPathEffect(lineWidth * 0.2f)
            ));
        }
        return paint;
    }

}
