package io.github.romangolovanov.apps.ametro.model.entities;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class MapDelayTimeRange
{
    private final int fromHour;
    private final int fromMinute;
    private final int toHour;
    private final int toMinute;

    public MapDelayTimeRange(int fromHour, int fromMinute, int toHour, int toMinute) {
        this.fromHour = fromHour;
        this.fromMinute = fromMinute;
        this.toHour = toHour;
        this.toMinute = toMinute;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("%1$02d:%2$02d - %3$02d:%4$02d", fromHour, fromMinute, toHour, toMinute);
    }
}


