package org.ametro.render.elements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.render.RenderConstants;
import org.ametro.render.utils.RenderUtils;
import org.ametro.utils.ModelUtils;

public class StationNameElement extends DrawingElement {

    private boolean vertical;

    private Paint[] textPaints = new Paint[RenderConstants.LAYER_COUNT];
    private Paint[] borderPaints = new Paint[RenderConstants.LAYER_COUNT];

    private String firstLine;
    private MapPoint firstLinePosition;

    private String secondLine;
    private MapPoint secondLinePosition;

    public StationNameElement(MapScheme scheme, MapSchemeLine line, MapSchemeStation station) {
        setUid(station.getUid());

        final String name = scheme.isUpperCase()
                ? station.getDisplayName().toUpperCase()
                : station.getDisplayName();

        final int textLength = name.length();
        final Rect textRect = ModelUtils.toRect(station.getLabelPosition());
        final MapPoint point = station.getPosition();

        vertical= textRect.width() < textRect.height();

        final Align align = vertical
                ? ((point.y > textRect.centerY()) ? Align.LEFT : Align.RIGHT)
                : ((point.x > textRect.centerX() ? Align.RIGHT : Align.LEFT));


        int textColor = line.getLabelColor();
        final Paint paint = createTextPaint(textColor);

        textPaints[RenderConstants.LAYER_VISIBLE] = paint;
        textPaints[RenderConstants.LAYER_VISIBLE].setTextAlign(align);

        textPaints[RenderConstants.LAYER_GRAYED] = createTextPaint(RenderUtils.getGrayedColor(textColor));
        textPaints[RenderConstants.LAYER_GRAYED].setTextAlign(align);

        int borderColor = line.getLabelBackgroundColor();
        int borderGrayedColor = RenderUtils.getGrayedColor(borderColor);
        if(borderColor == -1){
            borderColor = Color.WHITE;
            borderGrayedColor = Color.WHITE;
        }

        borderPaints[RenderConstants.LAYER_VISIBLE] = createBorderPaint(paint, borderColor);
        borderPaints[RenderConstants.LAYER_VISIBLE].setTextAlign(align);

        borderPaints[RenderConstants.LAYER_GRAYED] = createBorderPaint(paint, borderGrayedColor);
        borderPaints[RenderConstants.LAYER_GRAYED].setTextAlign(align);

        splitTextToLines(scheme, name, textLength, textRect, align, paint);

        setBoxAndPriority(textRect, RenderConstants.TYPE_STATION_NAME);
    }

    private void splitTextToLines(MapScheme scheme, String name, int textLength, Rect textRect, Align align, Paint paint) {
        Rect rect;
        if (vertical) {
            if (align == Align.LEFT) {
                rect = new Rect(textRect.left, textRect.bottom, textRect.left + textRect.height(), textRect.bottom + textRect.width());
            } else {
                rect = new Rect(textRect.left - textRect.height(), textRect.top, textRect.left, textRect.top + textRect.width());
            }
        } else {
            rect = new Rect(textRect);
        }

        final Rect bounds = new Rect();
        paint.getTextBounds(name, 0, textLength, bounds);
        boolean isNeedSecondLine = bounds.width() > rect.width() && scheme.isWordWrap();
        int spacePosition = -1;
        if (isNeedSecondLine) {
            spacePosition = name.indexOf(' ');
            isNeedSecondLine = spacePosition != -1;
        }
        if (isNeedSecondLine) {
            final String firstText = name.substring(0, spacePosition);

            final String secondText = name.substring(spacePosition + 1);

            final Rect secondRect = new Rect(rect.left, rect.top
                    + bounds.height() + 2, rect.right, rect.bottom
                    + bounds.height() + 2);

            firstLine = firstText;
            firstLinePosition = initializeLine(firstText, vertical, rect, paint, align);

            secondLine = secondText;
            secondLinePosition = initializeLine(secondText, vertical, secondRect, paint, align);
            secondLinePosition = new MapPoint(secondLinePosition.x-firstLinePosition.x, secondLinePosition.y-firstLinePosition.y);


        } else {
            firstLine = name;
            firstLinePosition = initializeLine(name, vertical, rect, paint, align);
        }
    }

    private Paint createTextPaint(int color) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setFakeBoldText(true);
        paint.setTextSize(10);
        paint.setTextAlign(Align.LEFT);
        paint.setColor(color);
        paint.setStyle(Style.FILL);
        return paint;
    }

    private Paint createBorderPaint(Paint paint, int color) {
        final Paint borderPaint = new Paint(paint);
        borderPaint.setColor(color);
        borderPaint.setStyle(Style.STROKE);
        borderPaint.setStrokeWidth(2);
        return borderPaint;
    }

    private static MapPoint initializeLine(final String text, boolean vertical, final Rect rect, final Paint paint, final Align align) {
        MapPoint position;
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        if (align == Align.RIGHT) { // align to right
            position = new MapPoint(rect.right + (vertical ? bounds.height() : 0), rect.top + (vertical ? 0 : bounds.height()));
        } else { // align to left
            position = new MapPoint(rect.left + (vertical ? bounds.height() : 0), rect.top + (vertical ? 0 : bounds.height()));
        }
        return position;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(firstLinePosition.x, firstLinePosition.y);
        if (vertical) {
            canvas.rotate(-90);
        }
        canvas.drawText(firstLine, 0, 0, borderPaints[layer]);
        canvas.drawText(firstLine, 0, 0, textPaints[layer]);
        if (secondLine != null) {
            canvas.translate(secondLinePosition.x, secondLinePosition.y);
            canvas.drawText(secondLine, 0, 0, borderPaints[layer]);
            canvas.drawText(secondLine, 0, 0, textPaints[layer]);
        }
        canvas.restore();
    }
}
