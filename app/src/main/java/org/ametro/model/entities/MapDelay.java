package org.ametro.model.entities;

import java.util.Arrays;

public class MapDelay {

    private final MapLocale locale;
    private final Integer name_id;
    private final MapDelayType delayType;
    private final MapDelayWeekdayType weekdays;
    private final MapDelayTimeRange[] ranges;

    public MapDelay(MapLocale locale, Integer name_id, MapDelayType delayType, MapDelayWeekdayType weekdays, MapDelayTimeRange[] ranges) {
        this.locale = locale;
        this.name_id = name_id;
        this.delayType = delayType;
        this.weekdays = weekdays;
        this.ranges = ranges;
    }

    public String getDisplayName() {
        if(name_id == null){
            return null;
        }
        return locale.getText(name_id);
    }

    public MapDelayType getDelayType() {
        return delayType;
    }

    public MapDelayWeekdayType getWeekdays() {
        return weekdays;
    }

    public MapDelayTimeRange[] getRanges() {
        return ranges;
    }

    @Override
    public String toString() {
        return "MapDelay{" +
                "name='" + getDisplayName() + '\'' +
                ", type=" + delayType +
                ", weekdays=" + weekdays +
                ", ranges=" + Arrays.toString(ranges) +
                '}';
    }
}



