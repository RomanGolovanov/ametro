package org.ametro.render.clipping;

import android.graphics.Rect;
import android.graphics.RectF;

import org.ametro.render.elements.DrawingElement;

import java.util.ArrayList;
import java.util.List;

public class ClippingTree {

    private static final int CLIPPING_OFFSET = 10;
    private static final int CLIPPING_TREE_GRANULARITY = 100;

    private final ClippingTreeNode rootNode;

    public ClippingTree(Rect bounds, List<DrawingElement> elements) {
        rootNode = new ClippingTreeNode(bounds);
        for (final DrawingElement element : elements) {
            layoutElementsIntoClippingTree(rootNode, element);
        }
    }

    public List<DrawingElement> getClippedElements(RectF v1, RectF v2) {
        final List<DrawingElement> elements = new ArrayList<>();
        clipping(rootNode, toRectWithOffset(v1), v2!=null ? toRectWithOffset(v2) : null, elements);
        return elements;
    }

    private void clipping(final ClippingTreeNode node, final Rect firstVolume, final Rect secondVolume, final List<DrawingElement> elements) {
        if (!(Rect.intersects(node.volume, firstVolume) || (secondVolume!=null && Rect.intersects(node.volume, secondVolume)))) {
            return;
        }
        for (final DrawingElement element : node.drawingElements) {
            final Rect box = element.getBoundingBox();
            if (Rect.intersects(firstVolume, box)) {
                elements.add(element);
                continue;
            }
            if (secondVolume != null && Rect.intersects(secondVolume, box)) {
                elements.add(element);
            }
        }
        if (node.leftChild!=null) {
            clipping(node.leftChild, firstVolume, secondVolume, elements);
        }
        if (node.rightChild!=null) {
            clipping(node.rightChild, firstVolume, secondVolume, elements);
        }
    }

    private void layoutElementsIntoClippingTree(final ClippingTreeNode node, final DrawingElement element) {
        if (node.leftChild == null || node.rightChild == null) {
            node.drawingElements.add(element);
            return;
        }
        final Rect left = node.leftChild.volume;
        final Rect right = node.rightChild.volume;
        final Rect rect = element.getBoundingBox();
        if (left.contains(rect)) {
            layoutElementsIntoClippingTree(node.leftChild, element);
        } else if (right.contains(rect)) {
            layoutElementsIntoClippingTree(node.rightChild, element);
        } else {
            node.drawingElements.add(element);
        }
    }

    private Rect toRectWithOffset(final RectF rect) {
        return new Rect(
                (int) (rect.left - CLIPPING_OFFSET),
                (int) (rect.top - CLIPPING_OFFSET),
                (int) (rect.right + CLIPPING_OFFSET),
                (int) (rect.bottom + CLIPPING_OFFSET));
    }

    private static class ClippingTreeNode {

        public final List<DrawingElement> drawingElements;

        public final Rect volume;
        public final ClippingTreeNode leftChild;
        public final ClippingTreeNode rightChild;

        public ClippingTreeNode(Rect clippingVolume) {
            this.drawingElements = new ArrayList<>();

            this.volume = clippingVolume;

            final int width = volume.width();
            final int height = volume.height();

            if (width < CLIPPING_TREE_GRANULARITY && height < CLIPPING_TREE_GRANULARITY) {
                leftChild = null;
                rightChild = null;
                return;
            }

            final int x = volume.left;
            final int y = volume.top;

            final Rect left = new Rect(volume);
            final Rect right = new Rect(volume);

            if (width > height) {
                int half = x + width / 2;
                left.right = half;
                right.left = half;
            } else {
                int half = y + height / 2;
                left.bottom = half;
                right.top = half;
            }

            leftChild = new ClippingTreeNode(left);
            rightChild = new ClippingTreeNode(right);
        }
    }
}

