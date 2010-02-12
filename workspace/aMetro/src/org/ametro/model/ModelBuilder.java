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

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import org.ametro.MapSettings;
import org.ametro.pmz.FilePackage;
import org.ametro.pmz.GenericResource;
import org.ametro.pmz.MapResource;
import org.ametro.pmz.MapResource.MapAddiditionalLine;
import org.ametro.pmz.MapResource.MapLine;
import org.ametro.pmz.TransportResource;
import org.ametro.pmz.TransportResource.TransportLine;
import org.ametro.pmz.TransportResource.TransportTransfer;
import org.ametro.util.SerializeUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.ametro.Constants.LOG_TAG_MAIN;


public class ModelBuilder {

    private static final int BUFFER_SIZE = 8192;

    public static ModelDescription loadModelDescription(String fileName) throws IOException, ClassNotFoundException {
        long startTime = System.currentTimeMillis();
        ObjectInputStream strm = null;
        ZipFile zip = null;
        try {
            zip = new ZipFile(fileName);
            ZipEntry entry = zip.getEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
            strm = new ObjectInputStream(new BufferedInputStream(zip.getInputStream(entry), BUFFER_SIZE));
            ModelDescription modelDescription = (ModelDescription) strm.readObject();
            if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
                Log.i(LOG_TAG_MAIN, "SubwayMap description '" + fileName
                        + "' loading time is " + (System.currentTimeMillis() - startTime) + "ms");
            }
            return modelDescription;
        } finally {
            if (strm != null) {
                try {
                    strm.close();
                } catch (Exception ignored) {
                }
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static SubwayMap loadModel(String fileName) throws IOException, ClassNotFoundException {
        Date startTimestamp = new Date();
        ObjectInputStream strm = null;
        ZipFile zip = null;
        try {
            zip = new ZipFile(fileName);
            ZipEntry entry = zip.getEntry(MapSettings.MAP_ENTRY_NAME);
            strm = new ObjectInputStream(new BufferedInputStream(zip.getInputStream(entry), BUFFER_SIZE));
            SubwayMap subwayMap = (SubwayMap) strm.readObject();
            if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
                Log.i(LOG_TAG_MAIN, String.format("SubwayMap data '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
            }
            return subwayMap;
        } finally {
            if (strm != null) {
                try {
                    strm.close();
                } catch (Exception ignored) {
                }
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void saveModel(SubwayMap subwayMap) throws IOException {
        Date startTimestamp = new Date();
        String fileName = MapSettings.getMapFileName(subwayMap.mapName);
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName), BUFFER_SIZE));
        saveModelDescriptionEntry(subwayMap, zip);
        saveModelEntry(subwayMap, zip);
        zip.flush();
        zip.close();
        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, String.format("SubwayMap file '%s' saving time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        }
    }

    private static void saveModelDescriptionEntry(SubwayMap subwayMap, ZipOutputStream zip) throws IOException {
        ZipEntry entry = new ZipEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
        zip.putNextEntry(entry);
        ObjectOutputStream strm = new ObjectOutputStream(zip);
        strm.writeObject(new ModelDescription(subwayMap));
        strm.flush();
        zip.closeEntry();
    }

    private static void saveModelEntry(SubwayMap subwayMap, ZipOutputStream zip) throws IOException {
        ZipEntry entry = new ZipEntry(MapSettings.MAP_ENTRY_NAME);
        zip.putNextEntry(entry);
        ObjectOutputStream strm = new ObjectOutputStream(zip);
        strm.writeObject(subwayMap);
        strm.flush();
        zip.closeEntry();
    }

    public static ModelDescription indexPmz(String fileName) throws IOException {
        Date startTimestamp = new Date();
        ModelDescription model = new ModelDescription();
        FilePackage pkg = new FilePackage(fileName);
        GenericResource info = pkg.getCityGenericResource();
        String countryName = info.getValue("Options", "Country");
        String cityName = info.getValue("Options", "RusName");
        if (cityName == null) {
            cityName = info.getValue("Options", "CityName");
        }
        model.countryName = countryName;
        model.cityName = cityName;
        model.sourceVersion = MapSettings.getSourceVersion();
        File pmzFile = new File(fileName);
        model.timestamp = pmzFile.lastModified();
        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, String.format("PMZ description '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        }
        return model;
    }


    public static SubwayMap importPmz(String fileName) throws IOException {
        Date startTimestamp = new Date();
        File file = new File(fileName);
        FilePackage pkg = new FilePackage(fileName);

        GenericResource info = pkg.getCityGenericResource();
        MapResource map = pkg.getMapResource("metro.map");
        TransportResource trp = pkg.getTransportResource(map.getTransportName() != null ? map.getTransportName() : "metro.trp");

        SubwayMap subwayMap = new SubwayMap(file.getName().replace(".pmz", ""));

        String countryName = info.getValue("Options", "Country");
        String cityName = info.getValue("Options", "RusName");
        if (cityName == null) {
            cityName = info.getValue("Options", "CityName");
        }
        subwayMap.countryName = countryName;
        subwayMap.cityName = cityName;
        subwayMap.linesWidth = map.getLinesWidth();
        subwayMap.stationDiameter = map.getStationDiameter();
        subwayMap.wordWrap = map.isWordWrap();
        subwayMap.upperCase = map.isUpperCase();

        subwayMap.timestamp = file.lastModified();
        subwayMap.sourceVersion = MapSettings.getSourceVersion();

        HashMap<String, MapLine> mapLines = map.getMapLines();
        HashMap<String, TransportLine> transportLines = trp.getLines();

        // lines construction
        ArrayList<SubwayLine> lines = new ArrayList<SubwayLine>();
        for (TransportLine tl : transportLines.values()) {
            MapLine ml = mapLines.get(tl.name);
            if (ml == null && tl.name != null) {
                continue;
            }
            String lineName = tl.name;
            int lineColor = ml.linesColor | 0xFF000000;
            int labelColor = ml.labelColor > 0 ? ml.labelColor | 0xFF000000 : lineColor;
            int labelBgColor = ml.backgroundColor;
            if (labelBgColor != -1) {
                labelBgColor = labelBgColor == 0 ? Color.WHITE : labelBgColor | 0xFF000000;
            } else {
                labelBgColor = 0;
            }

            SubwayLine line = new SubwayLine(lineName, lineColor, labelColor, labelBgColor);
            lines.add(line);
            if (ml.coordinates != null) {

                DelaysString tDelays = new DelaysString(tl.drivingDelaysText);
                StationsString tStations = new StationsString(tl.stationText);
                Point[] points = ml.coordinates;
                Rect[] rects = ml.rectangles;

                int stationIndex = 0;

                SubwayStation toStation;
                Double toDelay;

                SubwayStation fromStation = null;
                Double fromDelay = null;

                SubwayStation thisStation = line.invalidateStation(
                        tStations.next(),
                        getRect(rects, stationIndex),
                        getPoint(points, stationIndex));

                ArrayList<SubwaySegment> segments = new ArrayList<SubwaySegment>();
                do {
                    if ("(".equals(tStations.getNextDelimeter())) {
                        int idx = 0;
                        Double[] delays = tDelays.nextBracket();
                        while (tStations.hasNext() && !")".equals(tStations.getNextDelimeter())) {
                            boolean isForwardDirection = true;
                            String bracketedStationName = tStations.next();
                            if (bracketedStationName.startsWith("-")) {
                                bracketedStationName = bracketedStationName.substring(1);
                                isForwardDirection = !isForwardDirection;
                            }

                            if (bracketedStationName != null && bracketedStationName.length() > 0) {
                                SubwayStation bracketedStation = line.invalidateStation(bracketedStationName);
                                if (isForwardDirection) {
                                    addSegment(segments, thisStation, bracketedStation, delays.length <= idx ? null : delays[idx]);
                                } else {
                                    addSegment(segments, bracketedStation, thisStation, delays.length <= idx ? null : delays[idx]);
                                }
                            }
                            idx++;
                        }

                        fromStation = thisStation;

                        fromDelay = null;
                        toDelay = null;

                        if (!tStations.hasNext()) {
                            break;
                        }

                        stationIndex++;
                        thisStation = line.invalidateStation(
                                tStations.next(),
                                getRect(rects, stationIndex),
                                getPoint(points, stationIndex));

                    } else {

                        stationIndex++;
                        toStation = line.invalidateStation(tStations.next(),
                                getRect(rects, stationIndex),
                                getPoint(points, stationIndex));

                        if (tDelays.beginBracket()) {
                            Double[] delays = tDelays.nextBracket();
                            toDelay = delays[0];
                            fromDelay = delays[1];
                        } else {
                            toDelay = tDelays.next();
                        }

                        if (fromStation != null && line.getSegment(thisStation, fromStation) == null) {
                            if (fromDelay == null) {
                                SubwaySegment opposite = line.getSegment(fromStation, thisStation);
                                fromDelay = opposite != null ? opposite.delay : null;
                            }
                            addSegment(segments, thisStation, fromStation, fromDelay);
                        }
                        if (toStation != null && line.getSegment(thisStation, toStation) == null) {
                            addSegment(segments, thisStation, toStation, toDelay);
                        }


                        fromStation = thisStation;

                        fromDelay = toDelay;
                        toDelay = null;

                        thisStation = toStation;
                    }

                } while (tStations.hasNext());
                line.segments = segments.toArray(new SubwaySegment[segments.size()]);
            }
        }
        subwayMap.lines = lines.toArray(new SubwayLine[lines.size()]);

        // transfers construction
        ArrayList<SubwayStationsTransfer> transfers = new ArrayList<SubwayStationsTransfer>();
        for (TransportTransfer t : trp.getTransfers()) {
            SubwayStation from = subwayMap.getStation(t.startLine, t.startStation);
            SubwayStation to = subwayMap.getStation(t.endLine, t.endStation);
            int flags = 0;
            if (t.status != null && t.status.contains("invisible")) {
                flags = SubwayStationsTransfer.INVISIBLE;
            }
            if (from != null && to != null) {
                transfers.add(new SubwayStationsTransfer(from, to, t.delay, flags));
            }
        }
        subwayMap.transfers = transfers.toArray(new SubwayStationsTransfer[transfers.size()]);


        for (MapAddiditionalLine al : map.getAddiditionalLines()) {
            fillAdditionalLines(subwayMap, al);
        }

        fixDimensions(subwayMap);

        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, String.format("PMZ file '%s' parsing time is %sms", file.getName(), Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        }
        return subwayMap;
    }

    private static SubwaySegment addSegment(ArrayList<SubwaySegment> segments, SubwayStation from, SubwayStation to, Double delay) {
        SubwaySegment sg = new SubwaySegment(from, to, delay);
        newFrom.addSegment(this, SubwaySegment.SEGMENT_BEGIN);
        newTo.addSegment(this, SubwaySegment.SEGMENT_END);

        segments.add(sg);
        SubwaySegment opposite = getSegment(segments, to, from);
        if (opposite != null && (opposite.flags & SubwaySegment.INVISIBLE) == 0) {
            if (delay == null && opposite.delay != null) {
                sg.flags = SubwaySegment.INVISIBLE;
            } else if (delay != null && opposite.delay == null) {
                opposite.flags |= SubwaySegment.INVISIBLE;
            } else if (delay == null && opposite.delay == null) {
                sg.flags |= SubwaySegment.INVISIBLE;
            }
        }
        return sg;
    }

    public static SubwaySegment getSegment(ArrayList<SubwaySegment> segments, SubwayStation from, SubwayStation to) {
        final String fromName = from.name;
        final String toName = to.name;
        for (SubwaySegment seg : segments) {
            if (seg.from.name.equals(fromName) && seg.to.name.equals(toName)) {
                return seg;
            }
        }
        return null;
    }

    private static void fixDimensions(SubwayMap subwayMap) {
        int xmin = Integer.MAX_VALUE;
        int ymin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymax = Integer.MIN_VALUE;

        SubwayLine[] lines = subwayMap.lines;
        int linesCount = lines.length;

        for (int i = 0; i < linesCount; i++) {
            SubwayStation[] stations = lines[i].stations;
            int stationsCount = stations.length;
            for (int j = 0; j < stationsCount; j++) {
                SubwayStation station = stations[j];
                Point p = station.point;
                if (p != null) {
                    if (xmin > p.x) xmin = p.x;
                    if (ymin > p.y) ymin = p.y;

                    if (xmax < p.x) xmax = p.x;
                    if (ymax < p.y) ymax = p.y;
                }
                Rect r = station.rect;
                if (r != null) {
                    if (xmin > r.left) xmin = r.left;
                    if (ymin > r.top) ymin = r.top;
                    if (xmin > r.right) xmin = r.right;
                    if (ymin > r.bottom) ymin = r.bottom;

                    if (xmax < r.left) xmax = r.left;
                    if (ymax < r.top) ymax = r.top;
                    if (xmax < r.right) xmax = r.right;
                    if (ymax < r.bottom) ymax = r.bottom;
                }
            }
        }

        int dx = 50 - xmin;
        int dy = 50 - ymin;

        for (int i = 0; i < linesCount; i++) {
            SubwayLine line = lines[i];
            SubwayStation[] stations = line.stations;
            int stationsCount = stations.length;
            for (int j = 0; j < stationsCount; j++) {
                SubwayStation station = stations[j];
                Point p = station.point;
                if (p != null) {
                    p.offset(dx, dy);
                }
                Rect r = station.rect;
                if (r != null) {
                    r.offset(dx, dy);
                }
            }
            SubwaySegment[] segments = line.segments;
            int segmentsCount = segments.length;
            for (int j = 0; j < segmentsCount; j++) {
                Point[] points = segments[j].additionalNodes;
                if (points != null) {
                    int pointsCount = points.length;
                    for (int k = 0; k < pointsCount; k++) {
                        points[k].offset(dx, dy);
                    }
                }

            }
        }

        subwayMap.width = xmax - xmin + 100;
        subwayMap.height = ymax - ymin + 100;
    }

    private static void fillAdditionalLines(SubwayMap subwayMap, MapAddiditionalLine al) {
        if (al.mPoints == null) return;
        SubwayLine line = subwayMap.getLine(al.mLineName);
        SubwayStation from = subwayMap.getStation(al.mLineName, al.mFromStationName);
        SubwayStation to = subwayMap.getStation(al.mLineName, al.mToStationName);
        if (from != null && to != null) {
            SubwaySegment segment = line.getSegment(from, to);
            if (segment != null) {
                if (segment.additionalNodes == null) {
                    Point[] points = al.mPoints;
                    segment.additionalNodes = points;
                    if (al.mIsSpline) {
                        segment.flags = SubwaySegment.SPLINE;
                    }
                }
            } else {
                SubwaySegment opposite = line.getSegment(to, from);
                if (opposite != null) {
                    if (opposite.additionalNodes == null) {
                        Point[] points = new Point[al.mPoints.length];
                        for (int i = 0; i < points.length; i++) {
                            points[i] = al.mPoints[(points.length - 1) - i];
                        }
                        opposite.additionalNodes = points;
                        if (al.mIsSpline) {
                            opposite.flags = SubwaySegment.SPLINE;
                        }
                    }
                }
            }
        }
    }

    private static final Point zeroPoint = new Point(0, 0);
    private static final Rect zeroRect = new Rect(0, 0, 0, 0);

    private static Point getPoint(Point[] array, int index) {
        if (array == null) return null;
        return index >= array.length ? null : (!zeroPoint.equals(array[index]) ? array[index] : null);
    }

    private static Rect getRect(Rect[] array, int index) {
        if (array == null) return null;
        return index >= array.length ? null : (!zeroRect.equals(array[index]) ? array[index] : null);
    }


    private static class DelaysString {

        private String mText;
        //private String[] mParts;
        private int mPos;
        private int mLen;

        public DelaysString(String text) {
            //text = text.replaceAll("\\(","");
            //text = text.replaceAll("\\)","");
            //mParts = text.split(",");
            mText = text;
            mLen = text != null ? mText.length() : 0;
            mPos = 0;
        }

        public boolean beginBracket() {
            return mText != null && mPos < mLen && mText.charAt(mPos) == '(';
        }

        private String nextBlock() {
            if (mText == null) return null;
            int nextComma = mText.indexOf(",", beginBracket() ? mText.indexOf(")", mPos) : mPos);
            String block = nextComma != -1 ? mText.substring(mPos, nextComma) : mText.substring(mPos);
            mPos = nextComma != -1 ? nextComma + 1 : mLen;
            return block;
        }

        public Double next() {
            return SerializeUtil.parseNullableDouble(nextBlock());
        }

        public Double[] nextBracket() {
            if (mText == null) return null;
            String block = nextBlock();
            return SerializeUtil.parseDoubleArray(block.substring(1, block.length() - 1));
        }

    }

    private static class StationsString {
        private String mText;
        private String mDelimeters;
        private int mPos;
        private int mLen;
        private String mNextDelimeter;


        public String getNextDelimeter() {
            return mNextDelimeter;
        }

        public StationsString(String text) {
            mText = text;
            mDelimeters = ",()";
            mPos = 0;
            mLen = text.length();
            skipToContent();
        }

        public boolean hasNext() {
            int saved = mPos;
            skipToContent();
            boolean result = mPos != mLen;
            mPos = saved;
            return result;
        }

        public String next() {
            skipToContent();
            if (mPos == mLen) {
                return "";
            }
            int pos = mPos;
            String symbol = null;
            boolean quotes = false;
            while (pos < mLen && (!mDelimeters.contains(symbol = mText.substring(pos, pos + 1)) || quotes)) {
                if ("\"".equals(symbol)) {
                    quotes = !quotes;
                }
                pos++;
            }
            int end = symbol == null ? pos - 1 : pos;
            mNextDelimeter = symbol;
            String text = mText.substring(mPos, end);
            mPos = end;
            if (text.startsWith("\"") && text.endsWith("\""))
                text = text.substring(1, text.length() - 1);
            return text;
        }

        private void skipToContent() {
            String symbol;
            String symbolNext = (mPos < mLen) ? mText.substring(mPos, mPos + 1) : null;
            while (mPos < mLen && mDelimeters.contains(symbol = symbolNext)) {
                if ("(".equals(symbol)) {
                    mPos++;
                    return;
                } else if (")".equals(symbol)) {
                }
                mPos++;
                symbolNext = (mPos < mLen) ? mText.substring(mPos, mPos + 1) : null;
                if (",".equals(symbol) && !"(".equals(symbolNext)) return;
            }
        }


    }


}
