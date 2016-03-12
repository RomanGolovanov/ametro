package org.ametro.ui.navigation.helpers;

import org.ametro.R;
import org.ametro.model.entities.MapDelayType;
import org.ametro.model.entities.MapDelayWeekdayType;

import java.util.HashMap;

public class DelayResources {

    private final static HashMap<MapDelayType, Integer> delayTypeMap;
    private final static HashMap<MapDelayWeekdayType, Integer> delayWeekDaysMap;

    public static int getDelayTypeTextId(final MapDelayType type){
        return delayTypeMap.get(type);
    }

    public static int getDelayWeekendTypeTextId(MapDelayWeekdayType weekdays) {
        return delayWeekDaysMap.get(weekdays);
    }

    static {
        delayTypeMap = new HashMap<>();
        delayTypeMap.put(MapDelayType.NotDefined, R.string.delay_type_not_defined);

        delayTypeMap.put(MapDelayType.Custom, R.string.delay_type_custom);

        delayTypeMap.put(MapDelayType.Day, R.string.delay_type_day);
        delayTypeMap.put(MapDelayType.Night, R.string.delay_type_night);
        delayTypeMap.put(MapDelayType.Evening, R.string.delay_type_evening);
        delayTypeMap.put(MapDelayType.Mourning, R.string.delay_type_morning);
        delayTypeMap.put(MapDelayType.Rush, R.string.delay_type_rush);

        delayTypeMap.put(MapDelayType.Direct, R.string.delay_type_direct);

        delayTypeMap.put(MapDelayType.WestNorth, R.string.delay_type_west_north);
        delayTypeMap.put(MapDelayType.WestSouth, R.string.delay_type_west_south);
        delayTypeMap.put(MapDelayType.WestEast, R.string.delay_type_west_east);

        delayTypeMap.put(MapDelayType.EastNorth, R.string.delay_type_east_north);
        delayTypeMap.put(MapDelayType.EastSouth, R.string.delay_type_east_south);
        delayTypeMap.put(MapDelayType.EastWest, R.string.delay_type_east_west);

        delayTypeMap.put(MapDelayType.NorthEast, R.string.delay_type_north_east);
        delayTypeMap.put(MapDelayType.NorthWest, R.string.delay_type_north_west);
        delayTypeMap.put(MapDelayType.NorthSouth, R.string.delay_type_north_south);

        delayTypeMap.put(MapDelayType.SouthEast, R.string.delay_type_south_east);
        delayTypeMap.put(MapDelayType.SouthWest, R.string.delay_type_south_west);
        delayTypeMap.put(MapDelayType.SouthNorth, R.string.delay_type_south_north);

        delayWeekDaysMap = new HashMap<>();
        delayWeekDaysMap.put(MapDelayWeekdayType.NotDefined, R.string.delay_weekdays_type_not_defined);

        delayWeekDaysMap.put(MapDelayWeekdayType.Monday, R.string.delay_weekdays_type_monday);
        delayWeekDaysMap.put(MapDelayWeekdayType.Tuesday, R.string.delay_weekdays_type_tuesday);
        delayWeekDaysMap.put(MapDelayWeekdayType.Wednesday, R.string.delay_weekdays_type_wednesday);
        delayWeekDaysMap.put(MapDelayWeekdayType.Thursday, R.string.delay_weekdays_type_thursday);
        delayWeekDaysMap.put(MapDelayWeekdayType.Friday, R.string.delay_weekdays_type_friday);
        delayWeekDaysMap.put(MapDelayWeekdayType.Saturday, R.string.delay_weekdays_type_saturday);
        delayWeekDaysMap.put(MapDelayWeekdayType.Sunday, R.string.delay_weekdays_type_sunday);

        delayWeekDaysMap.put(MapDelayWeekdayType.Workdays, R.string.delay_weekdays_type_workdays);
        delayWeekDaysMap.put(MapDelayWeekdayType.Weekend, R.string.delay_weekdays_type_weekend);


    }
}
