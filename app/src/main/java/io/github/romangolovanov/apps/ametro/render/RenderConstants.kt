package io.github.romangolovanov.apps.ametro.render

object RenderConstants {

    const val TYPE_BACKGROUND: Int = 0x0000F000
    const val TYPE_LINE_DASHED: Int = 0x00010000
    const val TYPE_LINE: Int = 0x00020000
    const val TYPE_TRANSFER_BACKGROUND: Int = 0x00040000
    const val TYPE_TRANSFER: Int = 0x00080000
    const val TYPE_STATION: Int = 0x000100000
    const val TYPE_STATION_NAME: Int = 0x000200000

    const val LAYER_GRAYED: Int = 0
    const val LAYER_VISIBLE: Int = 1

    const val LAYER_COUNT: Int = 2
}
