package io.github.romangolovanov.apps.ametro.render.clipping

import android.graphics.Rect
import android.graphics.RectF
import io.github.romangolovanov.apps.ametro.render.elements.DrawingElement

class ClippingTree(bounds: Rect, elements: List<DrawingElement>) {

    private val rootNode: ClippingTreeNode = ClippingTreeNode(bounds)

    init {
        for (element in elements) {
            layoutElementsIntoClippingTree(rootNode, element)
        }
    }

    fun getClippedElements(v1: RectF, v2: RectF?): List<DrawingElement> {
        val elements = mutableListOf<DrawingElement>()
        clipping(rootNode, toRectWithOffset(v1), if (v2 != null) toRectWithOffset(v2) else null, elements)
        return elements
    }

    private fun clipping(
        node: ClippingTreeNode,
        firstVolume: Rect,
        secondVolume: Rect?,
        elements: MutableList<DrawingElement>
    ) {
        if (!(Rect.intersects(node.volume, firstVolume) || (secondVolume != null && Rect.intersects(node.volume, secondVolume)))) {
            return
        }
        for (element in node.drawingElements) {
            val box = element.getBoundingBox()
            if (box != null && Rect.intersects(firstVolume, box)) {
                elements.add(element)
                continue
            }
            if (secondVolume != null && box != null && Rect.intersects(secondVolume, box)) {
                elements.add(element)
            }
        }
        node.leftChild?.let { clipping(it, firstVolume, secondVolume, elements) }
        node.rightChild?.let { clipping(it, firstVolume, secondVolume, elements) }
    }

    private fun layoutElementsIntoClippingTree(node: ClippingTreeNode, element: DrawingElement) {
        if (node.leftChild == null || node.rightChild == null) {
            node.drawingElements.add(element)
            return
        }
        val left = node.leftChild.volume
        val right = node.rightChild.volume
        val rect = element.getBoundingBox()
        when {
            rect != null && left.contains(rect) -> layoutElementsIntoClippingTree(node.leftChild, element)
            rect != null && right.contains(rect) -> layoutElementsIntoClippingTree(node.rightChild, element)
            else -> node.drawingElements.add(element)
        }
    }

    private fun toRectWithOffset(rect: RectF): Rect {
        return Rect(
            (rect.left - CLIPPING_OFFSET).toInt(),
            (rect.top - CLIPPING_OFFSET).toInt(),
            (rect.right + CLIPPING_OFFSET).toInt(),
            (rect.bottom + CLIPPING_OFFSET).toInt()
        )
    }

    companion object {
        private const val CLIPPING_OFFSET = 10
        private const val CLIPPING_TREE_GRANULARITY = 100
    }

    private inner class ClippingTreeNode(clippingVolume: Rect) {

        val drawingElements: MutableList<DrawingElement> = mutableListOf()
        val volume: Rect = clippingVolume
        val leftChild: ClippingTreeNode?
        val rightChild: ClippingTreeNode?

        init {
            val width = volume.width()
            val height = volume.height()

            if (width < CLIPPING_TREE_GRANULARITY && height < CLIPPING_TREE_GRANULARITY) {
                leftChild = null
                rightChild = null
            } else {
                val x = volume.left
                val y = volume.top

                val left = Rect(volume)
                val right = Rect(volume)

                if (width > height) {
                    val half = x + width / 2
                    left.right = half
                    right.left = half
                } else {
                    val half = y + height / 2
                    left.bottom = half
                    right.top = half
                }

                leftChild = ClippingTreeNode(left)
                rightChild = ClippingTreeNode(right)
            }
        }
    }
}
