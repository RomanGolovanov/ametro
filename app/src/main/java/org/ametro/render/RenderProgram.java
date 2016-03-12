package org.ametro.render;

import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;

import org.ametro.model.MapContainer;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeSegment;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.model.entities.MapSchemeTransfer;
import org.ametro.render.clipping.ClippingTree;
import org.ametro.render.elements.BitmapBackgroundElement;
import org.ametro.render.elements.DrawingElement;
import org.ametro.render.elements.PictureBackgroundElement;
import org.ametro.render.elements.SegmentElement;
import org.ametro.render.elements.StationElement;
import org.ametro.render.elements.StationNameElement;
import org.ametro.render.elements.TransferBackgroundElement;
import org.ametro.render.elements.TransferElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class RenderProgram {

    private List<DrawingElement> elements;
    private ClippingTree clippingTree;

    public RenderProgram(MapContainer container, String schemeName) {
        elements = createElementsTree(container, schemeName);
        highlightsElements(null);
        clippingTree = new ClippingTree(getBoundingBox(elements), elements);
    }

    private static Rect getBoundingBox(Collection<DrawingElement> elements) {
        Rect bounds = null;
        for (DrawingElement element : elements) {
            if (bounds == null) {
                bounds = element.getBoundingBox();
                continue;
            }
            bounds.union(element.getBoundingBox());
        }
        return bounds;
    }

    public void highlightsElements(HashSet<Integer> ids) {
        for (DrawingElement element : elements) {
            if (ids == null) {
                element.setLayer(RenderConstants.LAYER_VISIBLE);
                continue;
            }
            element.setLayer(element.getUid() != null && ids.contains(element.getUid())
                    ? RenderConstants.LAYER_VISIBLE
                    : RenderConstants.LAYER_GRAYED);
        }
        Collections.sort(elements);
    }

    public List<DrawingElement> getAllDrawingElements() {
        return elements;
    }

    public List<DrawingElement> getClippedDrawingElements(RectF viewport) {
        return getClippedDrawingElements(viewport, null);
    }

    public List<DrawingElement> getClippedDrawingElements(RectF viewport1, RectF viewport2) {
        List<DrawingElement> clippedElements = clippingTree.getClippedElements(viewport1, viewport2);
        Collections.sort(clippedElements);
        return clippedElements;
    }

    private List<DrawingElement> createElementsTree(MapContainer container, String schemeName) {
        final List<DrawingElement> elements = new ArrayList<>();
        MapScheme scheme = container.getScheme(schemeName);
        for (MapSchemeLine line : scheme.getLines()) {
            createLine(elements, scheme, line);
        }
        for (MapSchemeTransfer transfer : scheme.getTransfers()) {
            createTransfer(elements, scheme, transfer);
        }
        for (String imageName : scheme.getImageNames()) {
            Object background = scheme.getBackgroundObject(imageName);
            if (background instanceof Picture) {
                elements.add(new PictureBackgroundElement(scheme, (Picture) background));
            } else if (background instanceof Bitmap) {
                elements.add(new BitmapBackgroundElement(scheme, (Bitmap) background));
            }
        }
        return elements;
    }

    private void createLine(final List<DrawingElement> elements, final MapScheme scheme, final MapSchemeLine line) {
        if (line.getSegments() != null) {
            for (MapSchemeSegment segment : line.getSegments()) {
                elements.add(new SegmentElement(line, segment));
            }
        }
        if (line.getStations() != null) {
            for (MapSchemeStation station : line.getStations()) {
                if (station.getPosition() != null) {
                    elements.add(new StationElement(scheme, line, station));
                }
                if (station.getLabelPosition() != null) {
                    elements.add(new StationNameElement(scheme, line, station));
                }
            }
        }
    }

    private void createTransfer(final List<DrawingElement> elements, final MapScheme scheme, final MapSchemeTransfer transfer) {
        if (transfer.getFromStationPosition() == null || transfer.getToStationPosition() == null) {
            return;
        }
        elements.add(new TransferElement(scheme, transfer));
        elements.add(new TransferBackgroundElement(scheme, transfer));
    }

}
