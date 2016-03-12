package org.ametro.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

import org.ametro.model.MapContainer;
import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapScheme;
import org.ametro.render.CanvasRenderer;
import org.ametro.render.RenderProgram;
import org.ametro.render.utils.Algorithms;
import org.ametro.ui.controllers.MultiTouchController;

import java.util.HashSet;

public class MultiTouchMapView extends ScrollView implements MultiTouchController.IMultiTouchListener {

    private static final long SCROLLBAR_TIMEOUT = 1000;

    private final MultiTouchController multiTouchController;
    private final IViewportChangedListener viewportChangedListener;

    private final CanvasRenderer renderer;

    private final Paint renderFailedTextPaint;
    private final String renderFailedErrorText;

    private final MapScheme mapScheme;

    private final Handler dispatcher = new Handler();
    private final RenderProgram rendererProgram;
    private final Runnable hideScrollbarsRunnable = new Runnable() {
        public void run() {
            fadeScrollBars();
        }
    };
    private PointF lastClickPosition;
    private final Runnable performClickRunnable = new Runnable() {
        public void run() {
            lastClickPosition = null;
            performClick();
        }
    };

    private float doubleClickSlop;
    private int verticalScrollOffset;
    private int horizontalScrollOffset;
    private int verticalScrollRange;
    private int horizontalScrollRange;
    private PointF changeCenterPoint;
    private Float changeScale;

    public MultiTouchMapView(Context context) {
        this(context, null, null, null);
    }

    public MultiTouchMapView(Context context, MapContainer container, String schemeName, IViewportChangedListener viewportChangedListener) {
        super(context);
        this.viewportChangedListener = viewportChangedListener;
        setScrollbarFadingEnabled(false);

        renderFailedErrorText = "Render failed!";

        setFocusable(true);
        setFocusableInTouchMode(true);

        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);

        awakeScrollBars();

        mapScheme = container.getScheme(schemeName);

        multiTouchController = new MultiTouchController(getContext(), this);

        doubleClickSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();

        rendererProgram = new RenderProgram(container, schemeName);
        renderer = new CanvasRenderer(this, mapScheme, rendererProgram);

        renderFailedTextPaint = new Paint();
        renderFailedTextPaint.setColor(Color.RED);
        renderFailedTextPaint.setTextAlign(Align.CENTER);

        initializeViewport();
    }

    protected int computeVerticalScrollOffset() {
        return verticalScrollOffset;
    }

    protected int computeVerticalScrollRange() {
        return verticalScrollRange;
    }

    protected int computeHorizontalScrollOffset() {
        return horizontalScrollOffset;
    }

    protected int computeHorizontalScrollRange() {
        return horizontalScrollRange;
    }

    protected void onAttachedToWindow() {
        renderer.onAttachedToWindow();
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        renderer.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (!renderer.draw(canvas)) {
            canvas.drawText(renderFailedErrorText, getWidth() / 2, getHeight() / 2, renderFailedTextPaint);
        }
        canvas.restore();
        super.onDraw(canvas);
    }

    public Matrix getPositionAndScaleMatrix() {
        return multiTouchController.getPositionAndScale();
    }

    public void setPositionAndScaleMatrix(Matrix matrix) {
        updateScrollBars(matrix);
        renderer.setMatrix(matrix);
        viewportChangedListener.onViewportChanged(matrix);
    }

    public void onTouchModeChanged(int mode) {
        renderer.setUpdatesEnabled(mode != MultiTouchController.MODE_ZOOM && mode != MultiTouchController.MODE_ANIMATION);
    }

    public void onPerformClick(PointF position) {
        if (lastClickPosition == null) {
            lastClickPosition = multiTouchController.getScreenTouchPoint();
            dispatcher.removeCallbacks(performClickRunnable);
            dispatcher.postDelayed(performClickRunnable, ViewConfiguration.getDoubleTapTimeout());
            return;
        }

        float distance = Algorithms.calculateDistance(lastClickPosition, multiTouchController.getScreenTouchPoint());

        dispatcher.removeCallbacks(performClickRunnable);
        lastClickPosition = null;

        if (distance <= doubleClickSlop) {
            multiTouchController.doZoomAnimation(MultiTouchController.ZOOM_IN, multiTouchController.getTouchPoint());
        } else {
            performClick();
        }
    }

    public void onPerformLongClick(PointF position) {
        performLongClick();
    }

    public MapPoint getTouchPoint() {
        PointF p = multiTouchController.getTouchPoint();
        return new MapPoint(p.x, p.y);
    }


    public void setCenterPositionAndScale(PointF position, Float zoom, boolean animated) {
        if (!animated) {
            changeCenterPoint = position;
            changeScale = zoom;
            invalidate();
        } else {
            multiTouchController.doScrollAndZoomAnimation(position, zoom);
        }
    }

    public Pair<PointF, Float> getCenterPositionAndScale() {
        PointF position = new PointF();
        float scale = multiTouchController.getPositionAndScale(position);
        float width = getWidth() / scale;
        float height = getHeight() / scale;
        position.offset(width/2,height/2);
        return new Pair<>(position, scale);
    }

    public float getScale() {
        return multiTouchController.getScale();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateViewRect();
        }
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return multiTouchController.onMultiTouchEvent(event);
    }

    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        updateViewRect();
        super.onSizeChanged(w, h, oldWidth, oldHeight);
    }

    public void highlightsElements(HashSet<Integer> ids) {
        rendererProgram.highlightsElements(ids);
        renderer.recycleCache();
        invalidate();
    }

    private void initializeViewport() {
        final RectF area = new RectF(0, 0, mapScheme.getWidth(), mapScheme.getHeight());
        final float scaleX = getWidth() / area.width();
        final float scaleY = getHeight() / area.height();
        final float targetScale = Math.min(scaleX, scaleY);
        final float currentScale = getScale();
        final float scale = targetScale > currentScale ? currentScale : targetScale;
        setCenterPositionAndScale(new PointF(area.centerX(), area.centerY()), scale, false);
    }

    private void updateViewRect() {
        multiTouchController.setViewRect(mapScheme.getWidth(), mapScheme.getHeight(), new RectF(0, 0, getWidth(), getHeight()));
        if (changeCenterPoint != null && changeScale != null) {
            float width = getWidth() / changeScale;
            float height = getHeight() / changeScale;
            changeCenterPoint.offset(-width / 2, -height / 2);
            multiTouchController.setPositionAndScale(changeCenterPoint, changeScale);
            changeCenterPoint = null;
            changeScale = null;
        }
    }

    private void updateScrollBars(Matrix matrix) {
        final float[] values = new float[9];
        matrix.getValues(values);
        float scale = values[Matrix.MSCALE_X];
        horizontalScrollRange = (int) (mapScheme.getWidth() * scale);
        verticalScrollRange = (int) (mapScheme.getHeight() * scale);
        horizontalScrollOffset = (int) -values[Matrix.MTRANS_X];
        verticalScrollOffset = (int) -values[Matrix.MTRANS_Y];
        awakeScrollBars();
    }

    private void awakeScrollBars() {
        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(true);
        dispatcher.removeCallbacks(hideScrollbarsRunnable);
        dispatcher.postDelayed(hideScrollbarsRunnable, SCROLLBAR_TIMEOUT);
        invalidate();
    }

    private void fadeScrollBars() {
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        invalidate();
    }

    public interface IViewportChangedListener {
        void onViewportChanged(Matrix matrix);
    }

}