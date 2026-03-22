package io.github.romangolovanov.apps.ametro.model.entities

data class MapDelayTimeRange(
    private val fromHour: Int,
    private val fromMinute: Int,
    private val toHour: Int,
    private val toMinute: Int
) {
    override fun toString(): String =
        "%02d:%02d - %02d:%02d".format(fromHour, fromMinute, toHour, toMinute)
}
