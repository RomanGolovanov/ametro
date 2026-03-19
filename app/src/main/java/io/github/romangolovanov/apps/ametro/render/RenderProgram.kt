package io.github.romangolovanov.apps.ametro.render

import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.RectF
import io.github.romangolovanov.apps.ametro.model.MapContainer
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeTransfer
import io.github.romangolovanov.apps.ametro.render.clipping.ClippingTree
import io.github.romangolovanov.apps.ametro.render.elements.BitmapBackgroundElement
import io.github.romangolovanov.apps.ametro.render.elements.DrawingElement
import io.github.romangolovanov.apps.ametro.render.elements.PictureBackgroundElement
import io.github.romangolovanov.apps.ametro.render.elements.SegmentElement
import io.github.romangolovanov.apps.ametro.render.elements.StationElement
import io.github.romangolovanov.apps.ametro.render.elements.StationNameElement
import io.github.romangolovanov.apps.ametro.render.elements.TransferBackgroundElement
import io.github.romangolovanov.apps.ametro.render.elements.TransferElement

class RenderProgram(container: MapContainer, schemeName: String) {

    private var elements: MutableList<DrawingElement>
    private val clippingTree: ClippingTree

    init {
        elements = createElementsTree(container, schemeName)
        highlightsElements(null)
        clippingTree = ClippingTree(getBoundingBox(elements)!!, elements)
    }

    private fun getBoundingBox(elements: Collection<DrawingElement>): Rect? {
        var bounds: Rect? = null
        for (element in elements) {
            val box = element.getBoundingBox() ?: continue
            if (bounds == null) {
                bounds = box
                continue
            }
            bounds.union(box)
        }
        return bounds
    }

    fun highlightsElements(ids: HashSet<Int>?) {
        for (element in elements) {
            if (ids == null) {
                element.setLayer(RenderConstants.LAYER_VISIBLE)
                continue
            }
            element.setLayer(
                if (element.uid != null && ids.contains(element.uid))
                    RenderConstants.LAYER_VISIBLE
                else
                    RenderConstants.LAYER_GRAYED
            )
        }
        elements.sort()
    }

    fun getAllDrawingElements(): List<DrawingElement> = elements

    fun getClippedDrawingElements(viewport: RectF): List<DrawingElement> {
        return getClippedDrawingElements(viewport, null)
    }

    fun getClippedDrawingElements(viewport1: RectF, viewport2: RectF?): List<DrawingElement> {
        val clippedElements = clippingTree.getClippedElements(viewport1, viewport2).toMutableList()
        clippedElements.sort()
        return clippedElements
    }

    private fun createElementsTree(container: MapContainer, schemeName: String): MutableList<DrawingElement> {
        val elements = mutableListOf<DrawingElement>()
        val scheme = container.getScheme(schemeName)!!
        for (line in scheme.lines) {
            createLine(elements, scheme, line)
        }
        for (transfer in scheme.transfers) {
            createTransfer(elements, scheme, transfer)
        }
        for (imageName in scheme.imageNames) {
            val background = scheme.getBackgroundObject(imageName)
            when (background) {
                is Picture -> elements.add(PictureBackgroundElement(scheme, background))
                is Bitmap -> elements.add(BitmapBackgroundElement(scheme, background))
            }
        }
        return elements
    }

    private fun createLine(elements: MutableList<DrawingElement>, scheme: MapScheme, line: MapSchemeLine) {
        for (segment in line.segments) {
            elements.add(SegmentElement(line, segment))
        }
        for (station in line.stations) {
            if (station.position != null) {
                elements.add(StationElement(scheme, line, station))
            }
            if (station.labelPosition != null) {
                elements.add(StationNameElement(scheme, line, station))
            }
        }
    }

    private fun createTransfer(elements: MutableList<DrawingElement>, scheme: MapScheme, transfer: MapSchemeTransfer) {
        if (transfer.fromStationPosition == null || transfer.toStationPosition == null) {
            return
        }
        elements.add(TransferElement(scheme, transfer))
        elements.add(TransferBackgroundElement(scheme, transfer))
    }
}
