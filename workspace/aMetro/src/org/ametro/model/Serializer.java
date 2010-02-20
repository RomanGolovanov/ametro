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
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.ametro.Constants.LOG_TAG_MAIN;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 10.02.2010
 *         Time: 23:31:44
 */
public class Serializer {

    public static void serialize(City city, OutputStream out) throws IOException {
        long startTime = System.currentTimeMillis();
        ZipOutputStream zipOut = new ZipOutputStream(out);
        CsvWriter csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(zipOut)));

        serializeCity(city, zipOut, csvWriter);

        zipOut.close();

        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, "City saving time is " + (System.currentTimeMillis() - startTime) + "ms");
        }

    }

    private static void serializeCity(City city, ZipOutputStream zipOut, CsvWriter csvWriter) throws IOException {
        ZipEntry zipEntry = new ZipEntry("city.csv");
        zipOut.putNextEntry(zipEntry);

        csvWriter.newRecord();
        csvWriter.writeString("version");
        csvWriter.writeInt(City.VERSION);

        csvWriter.newRecord();
        csvWriter.writeString(city.countryName);
        csvWriter.writeString(city.cityName);
        csvWriter.writeInt(city.width);
        csvWriter.writeInt(city.height);
        csvWriter.writeLong(city.timestamp);
        csvWriter.writeLong(city.crc);
        csvWriter.writeLong(city.renderVersion);
        csvWriter.writeLong(city.sourceVersion);

        csvWriter.flush();
        zipOut.closeEntry();

        serializeSubwayMap(city.subwayMap, zipOut, csvWriter);
    }

    private static void serializeSubwayMap(
            SubwayMap subwayMap, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {

        ZipEntry zipEntry = new ZipEntry("subway_map.csv");
        zipOut.putNextEntry(zipEntry);

        csvWriter.newRecord();
        csvWriter.writeString("version");
        csvWriter.writeInt(SubwayMap.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(subwayMap.id);
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

        csvWriter.flush();
        zipOut.closeEntry();

        writeLines(subwayMap.lines, zipOut, csvWriter);
        writeStations(subwayMap.stations, zipOut, csvWriter);
        writeSegments(subwayMap.segments, zipOut, csvWriter);
        writeTransfers(subwayMap.transfers, zipOut, csvWriter);
        writePoints(subwayMap.pointsBySegmentId, zipOut, csvWriter);
    }

    private static void writeLines(
            SubwayLine[] subwayLines, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (subwayLines != null) {
            ZipEntry zipEntry = new ZipEntry("subway_lines.csv");
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwayLine.VERSION);

            for (SubwayLine subwayLine : subwayLines) {
                csvWriter.newRecord();
                csvWriter.writeInt(subwayLine.id);
                csvWriter.writeString(subwayLine.name);
                csvWriter.writeInt(subwayLine.color);
                csvWriter.writeInt(subwayLine.labelColor);
                csvWriter.writeInt(subwayLine.labelBgColor);
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }
    }

    private static void writeStations(
            SubwayStation[] stations, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (stations != null) {
            ZipEntry zipEntry = new ZipEntry("subway_stations.csv");
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwayStation.VERSION);

            for (SubwayStation station : stations) {
                csvWriter.newRecord();
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

            csvWriter.flush();
            zipOut.closeEntry();
        }

    }

    private static void writeSegments(
            SubwaySegment[] segments, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (segments != null) {
            ZipEntry zipEntry = new ZipEntry("subway_segments.csv");
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwaySegment.VERSION);

            for (SubwaySegment segment : segments) {
                csvWriter.newRecord();
                csvWriter.writeInt(segment.id);
                csvWriter.writeDouble(segment.delay);
                csvWriter.writeInt(segment.from.id);
                csvWriter.writeInt(segment.to.id);
                csvWriter.writeInt(segment.flags);
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }
    }

    private static void writeTransfers(
            SubwayTransfer[] transfers, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (transfers != null) {
            ZipEntry zipEntry = new ZipEntry("subway_transfers.csv");
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwayTransfer.VERSION);

            for (SubwayTransfer transfer : transfers) {
                csvWriter.newRecord();
                csvWriter.writeInt(transfer.id);
                csvWriter.writeDouble(transfer.delay);
                csvWriter.writeInt(transfer.from.id);
                csvWriter.writeInt(transfer.to.id);
                csvWriter.writeInt(transfer.flags);
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }
    }

    private static void writePoints(
            HashMap<Integer, Point[]> pointsBySegmentId, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (pointsBySegmentId != null) {
            ZipEntry zipEntry = new ZipEntry("subway_segments_points.csv");
            zipOut.putNextEntry(zipEntry);

            for (int segmentId : pointsBySegmentId.keySet()) {
                Point[] points = pointsBySegmentId.get(segmentId);
                for (Point point : points) {
                    csvWriter.newRecord();
                    csvWriter.writeInt(segmentId);
                    csvWriter.writeInt(point.x);
                    csvWriter.writeInt(point.y);
                }
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }
    }

}
