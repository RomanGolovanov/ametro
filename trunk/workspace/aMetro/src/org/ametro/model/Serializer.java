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
import android.util.Log;
import org.ametro.util.csv.CsvWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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

    public static final String CITY_ENTRY_NAME = "city.csv";
    public static final String MAP_ENTRY_NAME = "subway_map.csv";
    public static final String LINES_ENTRY_NAME = "subway_lines.csv";
    public static final String STATIONS_ENTRY_NAME = "subway_stations.csv";
    public static final String SEGMENTS_ENTRY_NAME = "subway_segments.csv";
    public static final String TRANSFERS_ENTRY_NAME = "subway_transfers.csv";
    public static final String SEGMENTS_POINTS_ENTRY_NAME = "subway_segments_points.csv";

    public static final String ADDONS_DIR_ENTRY_NAME = "addons/";
    
    public static void serialize(OutputStream out, City city, ArrayList<StationAddon> addons) throws IOException {
        long startTime = System.currentTimeMillis();
        ZipOutputStream zipOut = new ZipOutputStream(out);
        CsvWriter csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(zipOut)));

        ZipEntry zipEntry = new ZipEntry(CITY_ENTRY_NAME);
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

        serializeMap(city.subwayMap, zipOut, csvWriter);
        if(addons!=null){
        	serializeAddons(addons, zipOut, csvWriter);
        }
        zipOut.close();

        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, "City saving time is " + (System.currentTimeMillis() - startTime) + "ms");
        }

    }

    private static void serializeAddons(ArrayList<StationAddon> addons, ZipOutputStream zipOut, CsvWriter csvWriter) throws IOException {
		for(StationAddon addon : addons){
	        ZipEntry zipEntry = new ZipEntry(ADDONS_DIR_ENTRY_NAME + addon.stationId + ".csv");
	        zipOut.putNextEntry(zipEntry);

	        csvWriter.newRecord();
	        csvWriter.writeString("version");
	        csvWriter.writeInt(StationAddon.VERSION);

	        csvWriter.newRecord();
	        csvWriter.writeInt(addon.stationId);

	        for(StationAddon.Entry entry : addon.entries){
		        csvWriter.newRecord();
		        csvWriter.writeString("entry");
		        csvWriter.writeInt(entry.id);
		        csvWriter.writeString(entry.caption);
		        csvWriter.writeInt(entry.text.length);
		        
		        for(String textLine: entry.text){
			        csvWriter.newRecord();
			        csvWriter.writeString(textLine);
		        }
	        }

	        csvWriter.flush();
	        zipOut.closeEntry();
		}
	}

	private static void serializeMap(
            SubwayMap subwayMap, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {

        ZipEntry zipEntry = new ZipEntry(MAP_ENTRY_NAME);
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

        serializeLines(subwayMap.lines, zipOut, csvWriter);
        serializeStations(subwayMap.stations, zipOut, csvWriter);
        serializeSegments(subwayMap.segments, zipOut, csvWriter);
        serializeTransfers(subwayMap.transfers, zipOut, csvWriter);
        serializePoints(subwayMap.pointsBySegmentId, zipOut, csvWriter);
    }

    private static void serializeLines(
            SubwayLine[] subwayLines, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (subwayLines != null) {
            ZipEntry zipEntry = new ZipEntry(LINES_ENTRY_NAME);
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

    private static void serializeStations(
            SubwayStation[] stations, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (stations != null) {
            ZipEntry zipEntry = new ZipEntry(STATIONS_ENTRY_NAME);
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwayStation.VERSION);

            for (SubwayStation station : stations) {
                csvWriter.newRecord();
                csvWriter.writeInt(station.id);
                csvWriter.writeString(station.name);
                csvWriter.writeRect(station.rect);
                csvWriter.writePoint(station.point);
                csvWriter.writeInt(station.lineId);
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }

    }

    private static void serializeSegments(
            SubwaySegment[] segments, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (segments != null) {
            ZipEntry zipEntry = new ZipEntry(SEGMENTS_ENTRY_NAME);
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwaySegment.VERSION);

            for (SubwaySegment segment : segments) {
                csvWriter.newRecord();
                csvWriter.writeInt(segment.id);
                csvWriter.writeNullableDouble(segment.delay);
                csvWriter.writeInt(segment.fromStationId);
                csvWriter.writeInt(segment.toStationId);
                csvWriter.writeInt(segment.flags);
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }
    }

    private static void serializeTransfers(
            SubwayTransfer[] transfers, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (transfers != null) {
            ZipEntry zipEntry = new ZipEntry(TRANSFERS_ENTRY_NAME);
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwayTransfer.VERSION);

            for (SubwayTransfer transfer : transfers) {
                csvWriter.newRecord();
                csvWriter.writeInt(transfer.id);
                csvWriter.writeNullableDouble(transfer.delay);
                csvWriter.writeInt(transfer.fromStationId);
                csvWriter.writeInt(transfer.toStationId);
                csvWriter.writeInt(transfer.flags);
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }
    }

    private static void serializePoints(
            HashMap<Integer, Point[]> pointsBySegmentId, ZipOutputStream zipOut, CsvWriter csvWriter
    ) throws IOException {
        if (pointsBySegmentId != null) {
            ZipEntry zipEntry = new ZipEntry(SEGMENTS_POINTS_ENTRY_NAME);
            zipOut.putNextEntry(zipEntry);

            csvWriter.newRecord();
            csvWriter.writeString("version");
            csvWriter.writeInt(SubwaySegment.VERSION);
            
            for (int segmentId : pointsBySegmentId.keySet()) {
                Point[] points = pointsBySegmentId.get(segmentId);
                csvWriter.newRecord();
                csvWriter.writeInt(segmentId);
                csvWriter.writePointArray(points);
            }

            csvWriter.flush();
            zipOut.closeEntry();
        }
    }

}
