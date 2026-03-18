package io.github.romangolovanov.apps.ametro.model.entities

class MapDelay(
    private val locale: MapLocale?,
    private val nameId: Int?,
    val delayType: MapDelayType,
    val weekdays: MapDelayWeekdayType,
    val ranges: Array<MapDelayTimeRange>
) {
    val displayName: String? get() = if (nameId == null) null else locale?.getText(nameId)

    override fun toString(): String =
        "MapDelay{name='${displayName}', type=$delayType, weekdays=$weekdays, ranges=${ranges.contentToString()}}"
}
