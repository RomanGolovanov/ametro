package io.github.romangolovanov.apps.ametro.model.serialization

import android.graphics.Color
import android.util.Log

import com.fasterxml.jackson.databind.JsonNode

import io.github.romangolovanov.apps.ametro.app.Constants
import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapRect
import io.github.romangolovanov.apps.ametro.utils.StringUtils

object CommonTypes {
    @JvmField val DEFAULT_COLOR: Int = 0x0000FF
    @JvmField val DEFAULT_LABEL_BG_COLOR: Int = 0x0000FF

    @JvmStatic
    fun asStringArray(node: JsonNode): Array<String> {
        return Array(node.size()) { i -> node.get(i).asText() }
    }

    @JvmStatic
    fun asColor(node: JsonNode?, defaultColor: Int): Int {
        if (node == null || node.isNull) return defaultColor
        val text = node.asText()
        if (StringUtils.isNullOrEmpty(text)) return defaultColor
        if (text == "-1") return Color.parseColor("#FFFFFF")
        return try {
            Color.parseColor('#' + text)
        } catch (ex: Exception) {
            Log.e(Constants.LOG, "Invalid color [$text]")
            defaultColor
        }
    }

    @JvmStatic
    fun asRect(node: JsonNode?): MapRect? {
        if (node == null || node.isNull) return null
        return MapRect(
            node.get(0).asInt(),
            node.get(1).asInt(),
            node.get(2).asInt(),
            node.get(3).asInt()
        )
    }

    @JvmStatic
    fun asPoint(node: JsonNode?): MapPoint? {
        if (node == null || node.isNull) return null
        return MapPoint(
            node.get(0).asDouble().toFloat(),
            node.get(1).asDouble().toFloat()
        )
    }

    @JvmStatic
    fun asPointArray(arrayNode: JsonNode?): Array<MapPoint> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i -> asPoint(arrayNode.get(i))!! }
    }

    @JvmStatic
    fun asIntArray(arrayNode: JsonNode?): IntArray {
        if (arrayNode == null || arrayNode.isNull) return IntArray(0)
        return IntArray(arrayNode.size()) { i -> arrayNode.get(i).asInt() }
    }
}
