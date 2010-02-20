/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.model;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import org.ametro.util.csv.CsvWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.ametro.Constants.LOG_TAG_MAIN;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 10.02.2010
 *         Time: 23:31:44
 */
public class Serializer {

    private static final int HEADER_TYPE = 0;
    private static final int CITY_TYPE = 1;
    private static final int SUBWAY_MAP_TYPE = 2;
    private static final int SUBWAY_LINE_TYPE = 3;
    private static final int SUBWAY_STATION_TYPE = 4;
    private static final int SUBWAY_TRANSFER_TYPE = 5;
    private static final int SUBWAY_SEGMENT_TYPE = 6;
    private static final int RECT_TYPE = 7;
    private static final int POINT_TYPE = 8;

    public static void serialize(City[] cities, OutputStream out) throws IOException {
        long startTime = System.currentTimeMillis();
        ZipOutputStream zipOut = new ZipOutputStream(out);
        CsvWriter csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(zipOut)));

        for (City city : cities) {
            serialize(city, zipOut, csvWriter);
        }

        zipOut.close();

        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, "SubwayMap saving time is " + (System.currentTimeMillis() - startTime) + "ms");
        }

    }

    private static void serialize(City city, ZipOutputStream zipOut, CsvWriter csvWriter) throws IOException {
        ZipEntry zipEntry = new ZipEntry(city.cityName + ".csv");
        zipOut.putNextEntry(zipEntry);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(CITY_TYPE);
        csvWriter.writeInt(City.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(SUBWAY_MAP_TYPE);
        csvWriter.writeInt(SubwayMap.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(SUBWAY_LINE_TYPE);
        csvWriter.writeInt(SubwayLine.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(SUBWAY_STATION_TYPE);
        csvWriter.writeInt(SubwayStation.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(SUBWAY_TRANSFER_TYPE);
        csvWriter.writeInt(SubwayTransfer.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(SUBWAY_SEGMENT_TYPE);
        csvWriter.writeInt(SubwaySegment.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(RECT_TYPE);
        csvWriter.writeInt(1);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeInt(POINT_TYPE);
        csvWriter.writeInt(1);

        serialize(city, csvWriter);

        csvWriter.flush();
        zipOut.closeEntry();
    }

    private static void serialize(City city, CsvWriter csvWriter) throws IOException {
        csvWriter.newRecord();
        csvWriter.writeInt(CITY_TYPE);
        csvWriter.writeInt(city.id);
        csvWriter.writeString(city.countryName);
        csvWriter.writeString(city.cityName);
        csvWriter.writeInt(city.width);
        csvWriter.writeInt(city.height);
        csvWriter.writeLong(city.timestamp);
        csvWriter.writeLong(city.crc);
        csvWriter.writeLong(city.renderVersion);
        csvWriter.writeLong(city.sourceVersion);
        serialize(city.subwayMap, csvWriter);
    }

    private static void serialize(SubwayMap subwayMap, CsvWriter csvWriter) throws IOException {
        int id = subwayMap.id;
        csvWriter.newRecord();
        csvWriter.writeInt(SUBWAY_MAP_TYPE);
        csvWriter.writeInt(id);
        csvWriter.writeLong(subwayMap.timestamp);
        csvWriter.writeLong(subwayMap.crc);
        csvWriter.writeString(subwayMap.mapName);
        csvWriter.writeString(subwayMap.cityName);
        csvWriter.writeString(subwayMap.countryName);
        csvWriter.writeInt(subwayMap.width);
        csvWriter.writeInt(subwayMap.height);
        csvWriter.writeInt(subwayMap.stationDiameter);
        csvWriter.writeInt(subwayMap.linesWidth);
        csvWriter.writeBoolean(subwayMap.wordWrap);
        csvWriter.writeBoolean(subwayMap.upperCase);
        csvWriter.writeLong(subwayMap.sourceVersion);

        serialize(id, subwayMap.lines, csvWriter);
        serialize(id, subwayMap.stations, csvWriter);
    }

    private static void serialize(int subwayMapId, SubwayLine[] subwayLines, CsvWriter csvWriter) throws IOException {
        if (subwayLines != null) {
            for (SubwayLine subwayLine : subwayLines) {
                csvWriter.newRecord();
                csvWriter.writeInt(SUBWAY_LINE_TYPE);
                csvWriter.writeInt(subwayMapId);
                csvWriter.writeInt(subwayLine.id);
                csvWriter.writeString(subwayLine.name);
                csvWriter.writeInt(subwayLine.color);
                csvWriter.writeInt(subwayLine.labelColor);
                csvWriter.writeInt(subwayLine.labelBgColor);
            }
        }
    }

    private static void serialize(int subwayMapId, SubwayStation[] stations, CsvWriter csvWriter) throws IOException {
        if (stations!= null) {
            for (SubwayStation station : stations) {
                csvWriter.newRecord();
                csvWriter.writeInt(SUBWAY_STATION_TYPE);
                csvWriter.writeInt(subwayMapId);
                csvWriter.writeInt(station.id);
                csvWriter.writeString(station.name);
                Rect rect = station.rect;
                csvWriter.writeInt(rect.left);
                csvWriter.writeInt(rect.top);
                csvWriter.writeInt(rect.right);
                csvWriter.writeInt(rect.bottom);
                Point point = station.point;
                csvWriter.writeInt(point.x);
                csvWriter.writeInt(point.y);
                csvWriter.writeInt(station.line.id);
            }
        }

    }

    private static void serialize(SubwaySegment segment, CsvWriter csvWriter) throws IOException {
        csvWriter.newRecord();
        csvWriter.writeInt(SUBWAY_SEGMENT_TYPE);
    }

}
