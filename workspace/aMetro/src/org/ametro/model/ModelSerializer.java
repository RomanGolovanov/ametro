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

import android.util.Log;
import org.ametro.util.csv.CsvWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.ametro.Constants.LOG_TAG_MAIN;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 10.02.2010
 *         Time: 23:31:44
 */
public class ModelSerializer {

    private static final int HEADER_TYPE = 0;
    private static final int MODEL_TYPE = 1;
    private static final int LINE_TYPE = 2;
    private static final int TRANSFER_TYPE = 3;
    private static final int STATION_TYPE = 4;
    private static final int SEGMENT_TYPE = 5;
    private static final int RECT_TYPE = 6;
    private static final int POINT_TYPE = 7;

    public static void serialize(Model model, OutputStream out) throws IOException {
        long startTime = System.currentTimeMillis();
        ZipOutputStream zipOut = new ZipOutputStream(out);
        ZipEntry zipEntry = new ZipEntry("model.csv");

        zipOut.putNextEntry(zipEntry);

        CsvWriter csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(zipOut)));

        csvWriter.newRecord();
        // file type
        csvWriter.writeString("Ametro map viewer file");
        // date when file was created
        csvWriter.writeDate(new Date());

        csvWriter.newRecord();
        // header flag
        csvWriter.writeInt(HEADER_TYPE);
        // Object type
        csvWriter.writeString("Model");
        // its type code
        csvWriter.writeInt(MODEL_TYPE);
        // and its version for future deprecated (or too new) file recognition
        csvWriter.writeInt(Model.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeString("Line");
        csvWriter.writeInt(LINE_TYPE);
        csvWriter.writeInt(Line.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeString("Transfer");
        csvWriter.writeInt(TRANSFER_TYPE);
        csvWriter.writeInt(Transfer.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeString("Station");
        csvWriter.writeInt(STATION_TYPE);
        csvWriter.writeInt(Station.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeString("Segment");
        csvWriter.writeInt(SEGMENT_TYPE);
        csvWriter.writeInt(Segment.VERSION);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeString("Rect");
        csvWriter.writeInt(RECT_TYPE);

        csvWriter.newRecord();
        csvWriter.writeInt(HEADER_TYPE);
        csvWriter.writeString("Point");
        csvWriter.writeInt(POINT_TYPE);

        serialize(model, csvWriter);

        csvWriter.flush();

        zipOut.closeEntry();
        zipOut.close();

        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, "Model saving time is " + (System.currentTimeMillis() - startTime) + "ms");
        }

    }

    private static void serialize(Model model, CsvWriter csvWriter) throws IOException {
        csvWriter.newRecord();
        csvWriter.writeInt(MODEL_TYPE);
        csvWriter.writeLong(model.timestamp);
        csvWriter.writeLong(model.crc);
        csvWriter.writeString(model.mapName);
        csvWriter.writeString(model.cityName);
        csvWriter.writeString(model.countryName);
        csvWriter.writeInt(model.width);
        csvWriter.writeInt(model.height);
        csvWriter.writeInt(model.stationDiameter);
        csvWriter.writeInt(model.linesWidth);
        csvWriter.writeBoolean(model.wordWrap);
        csvWriter.writeBoolean(model.upperCase);
        csvWriter.writeLong(model.sourceVersion);

        final Line[] lines = model.lines;
        final Transfer[] transfers = model.transfers;
        int linesCount = lines.length;
        int transfersCount = transfers.length;

        csvWriter.writeInt(linesCount);
        csvWriter.writeInt(transfersCount);

        for (int i = 0; i < lines.length; i++) {
            serialize(lines[i], csvWriter);
        }

        for (int i = 0; i < transfers.length; i++) {
            serialize(transfers[i], csvWriter);
        }
    }

    private static void serialize(Line line, CsvWriter csvWriter) throws IOException {
        csvWriter.newRecord();
        csvWriter.writeInt(LINE_TYPE);
        csvWriter.writeString(line.name);
        csvWriter.writeInt(line.color);
        csvWriter.writeInt(line.labelColor);
        csvWriter.writeInt(line.labelBgColor);

        final Station[] stations = line.stations;
        final Segment[] segments = line.segments;
        int stationsCount = stations.length;
        int segmentsCount = segments.length;

        csvWriter.writeInt(stationsCount);
        csvWriter.writeInt(segmentsCount);

        for (int i = 0; i < stations.length; i++) {
            serialize(stations[i], csvWriter);
        }

        for (int i = 0; i < segmentsCount; i++) {
            serialize(segments[i], csvWriter);
        }
    }

    private static void serialize(Station station, CsvWriter csvWriter) throws IOException {
        csvWriter.newRecord();
        csvWriter.writeInt(STATION_TYPE);
        csvWriter.writeString(station.mName);

    }

    private static void serialize(Segment segment, CsvWriter csvWriter) throws IOException {
        csvWriter.newRecord();
        csvWriter.writeInt(SEGMENT_TYPE);
    }

    private static void serialize(Transfer transfer, CsvWriter csvWriter) throws IOException {
        csvWriter.newRecord();
        csvWriter.writeInt(TRANSFER_TYPE);
    }

}
