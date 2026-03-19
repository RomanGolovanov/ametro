package io.github.romangolovanov.apps.ametro.ui.navigation.helpers

import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.model.entities.MapDelayType
import io.github.romangolovanov.apps.ametro.model.entities.MapDelayWeekdayType

object DelayResources {

    private val delayTypeMap: HashMap<MapDelayType, Int> = HashMap<MapDelayType, Int>().apply {
        put(MapDelayType.NotDefined, R.string.delay_type_not_defined)
        put(MapDelayType.Custom, R.string.delay_type_custom)
        put(MapDelayType.Day, R.string.delay_type_day)
        put(MapDelayType.Night, R.string.delay_type_night)
        put(MapDelayType.Evening, R.string.delay_type_evening)
        put(MapDelayType.Mourning, R.string.delay_type_morning)
        put(MapDelayType.Rush, R.string.delay_type_rush)
        put(MapDelayType.Direct, R.string.delay_type_direct)
        put(MapDelayType.WestNorth, R.string.delay_type_west_north)
        put(MapDelayType.WestSouth, R.string.delay_type_west_south)
        put(MapDelayType.WestEast, R.string.delay_type_west_east)
        put(MapDelayType.EastNorth, R.string.delay_type_east_north)
        put(MapDelayType.EastSouth, R.string.delay_type_east_south)
        put(MapDelayType.EastWest, R.string.delay_type_east_west)
        put(MapDelayType.NorthEast, R.string.delay_type_north_east)
        put(MapDelayType.NorthWest, R.string.delay_type_north_west)
        put(MapDelayType.NorthSouth, R.string.delay_type_north_south)
        put(MapDelayType.SouthEast, R.string.delay_type_south_east)
        put(MapDelayType.SouthWest, R.string.delay_type_south_west)
        put(MapDelayType.SouthNorth, R.string.delay_type_south_north)
    }

    private val delayWeekDaysMap: HashMap<MapDelayWeekdayType, Int> = HashMap<MapDelayWeekdayType, Int>().apply {
        put(MapDelayWeekdayType.NotDefined, R.string.delay_weekdays_type_not_defined)
        put(MapDelayWeekdayType.Monday, R.string.delay_weekdays_type_monday)
        put(MapDelayWeekdayType.Tuesday, R.string.delay_weekdays_type_tuesday)
        put(MapDelayWeekdayType.Wednesday, R.string.delay_weekdays_type_wednesday)
        put(MapDelayWeekdayType.Thursday, R.string.delay_weekdays_type_thursday)
        put(MapDelayWeekdayType.Friday, R.string.delay_weekdays_type_friday)
        put(MapDelayWeekdayType.Saturday, R.string.delay_weekdays_type_saturday)
        put(MapDelayWeekdayType.Sunday, R.string.delay_weekdays_type_sunday)
        put(MapDelayWeekdayType.Workdays, R.string.delay_weekdays_type_workdays)
        put(MapDelayWeekdayType.Weekend, R.string.delay_weekdays_type_weekend)
    }

    fun getDelayTypeTextId(type: MapDelayType): Int = delayTypeMap[type]!!

    fun getDelayWeekendTypeTextId(weekdays: MapDelayWeekdayType): Int = delayWeekDaysMap[weekdays]!!
}
