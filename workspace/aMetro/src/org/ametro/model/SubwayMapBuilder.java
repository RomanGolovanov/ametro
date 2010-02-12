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
import org.ametro.pmz.TransportResource;
import org.ametro.util.SerializeUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.ametro.Constants.LOG_TAG_MAIN;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 11.02.2010
 *         Time: 22:19:35
 */
public class SubwayMapBuilder {

    private static final Point ZERO_POINT = new Point(0, 0);
    private static final Rect ZERO_RECT = new Rect(0, 0, 0, 0);

    private ArrayList<SubwayLine> lines = new ArrayList<SubwayLine>();
    private HashMap<String, SubwayLine> linesByName = new HashMap<String, SubwayLine>();

    private ArrayList<SubwaySegment> segments = new ArrayList<SubwaySegment>();
    private HashMap<String, ArrayList<SubwaySegment>> segmentsByLine = new HashMap<String, ArrayList<SubwaySegment>>();
    private HashMap<String, ArrayList<SubwaySegment>> segmentsByStation = new HashMap<String, ArrayList<SubwaySegment>>();
    public HashMap<SubwaySegment, ArrayList<Point>> segmentNodes = new HashMap<SubwaySegment, ArrayList<Point>>();

    private ArrayList<SubwayStation> stations = new ArrayList<SubwayStation>();
    private HashMap<String, SubwayStation> stationsByName = new HashMap<String, SubwayStation>();
    private HashMap<String, ArrayList<SubwayStation>> stationsByLine = new HashMap<String, ArrayList<SubwayStation>>();

    public ArrayList<SubwayStationsTransfer> transfers = new ArrayList<SubwayStationsTransfer>();

    public SubwayMap importPmz(String fileName) throws IOException {
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

        HashMap<String, MapResource.MapLine> mapLines = map.getMapLines();
        HashMap<String, TransportResource.TransportLine> transportLines = trp.getLines();

        // lines construction
        for (TransportResource.TransportLine tl : transportLines.values()) {
            MapResource.MapLine ml = mapLines.get(tl.name);
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
            linesByName.put(line.name, line);

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

                SubwayStation thisStation = invalidateStation(
                        line,
                        tStations.next(),
                        getRect(rects, stationIndex),
                        getPoint(points, stationIndex));

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
                                SubwayStation bracketedStation = invalidateStation(line, bracketedStationName);
                                if (isForwardDirection) {
                                    addSegment(line, thisStation, bracketedStation, delays.length <= idx ? null : delays[idx]);
                                } else {
                                    addSegment(line, bracketedStation, thisStation, delays.length <= idx ? null : delays[idx]);
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
                        thisStation = invalidateStation(
                                line, tStations.next(),
                                getRect(rects, stationIndex),
                                getPoint(points, stationIndex));

                    } else {

                        stationIndex++;
                        toStation = invalidateStation(
                                line, tStations.next(),
                                getRect(rects, stationIndex),
                                getPoint(points, stationIndex));

                        if (tDelays.beginBracket()) {
                            Double[] delays = tDelays.nextBracket();
                            toDelay = delays[0];
                            fromDelay = delays[1];
                        } else {
                            toDelay = tDelays.next();
                        }

                        if (fromStation != null && getSegment(line, thisStation, fromStation) == null) {
                            if (fromDelay == null) {
                                SubwaySegment opposite = getSegment(line, fromStation, thisStation);
                                fromDelay = opposite != null ? opposite.delay : null;
                            }
                            addSegment(line, thisStation, fromStation, fromDelay);
                        }
                        if (toStation != null && getSegment(line, thisStation, toStation) == null) {
                            addSegment(line, thisStation, toStation, toDelay);
                        }


                        fromStation = thisStation;

                        fromDelay = toDelay;
                        toDelay = null;

                        thisStation = toStation;
                    }

                } while (tStations.hasNext());
            }
        }

        // transfers construction
        for (TransportResource.TransportTransfer t : trp.getTransfers()) {
            SubwayStation from = stationsByName.get(t.startStation);
            SubwayStation to = stationsByName.get(t.endStation);
            int flags = 0;
            if (t.status != null && t.status.contains("invisible")) {
                flags = SubwayStationsTransfer.INVISIBLE;
            }
            if (from != null && to != null) {
                transfers.add(new SubwayStationsTransfer(from, to, t.delay, flags));
            }
        }


        for (MapResource.MapAddiditionalLine al : map.getAddiditionalLines()) {
            fillAdditionalLines(subwayMap, al);
        }

        fixDimensions(subwayMap);

        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, String.format("PMZ file '%s' parsing time is %sms", file.getName(), Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        }
        return subwayMap;
    }

    private SubwayStation addStation(SubwayLine line, String stationName, Rect r, Point p) {
        SubwayStation st = new SubwayStation(stationName, r, p, line);
        stations.add(st);
        stationsByName.put(stationName, st);
        String lineName = line.name;
        ArrayList<SubwayStation> lineStations = stationsByLine.get(lineName);
        if (lineStations == null) {
            lineStations = new ArrayList<SubwayStation>();
            stationsByLine.put(lineName, lineStations);
        }
        lineStations.add(st);
        return st;
    }

    public SubwayStation invalidateStation(SubwayLine line, String stationName) {
        SubwayStation st = stationsByName.get(stationName);
        if (st == null) {
            st = addStation(line, stationName, null, null);
        }
        return st;
    }

    public SubwayStation invalidateStation(SubwayLine line, String stationName, Rect r, Point p) {
        SubwayStation st = stationsByName.get(stationName);
        if (st == null) {
            st = addStation(line, stationName, r, p);
        } else {
            st.point = p;
            st.rect = r;
        }
        return st;
    }

    public SubwaySegment getSegment(SubwayLine line, SubwayStation from, SubwayStation to) {
        ArrayList<SubwaySegment> lineSegments = segmentsByLine.get(line.name);
        if (lineSegments != null) {
            final String fromName = from.name;
            final String toName = to.name;
            for (SubwaySegment seg : lineSegments) {
                if (seg.from.name.equals(fromName) && seg.to.name.equals(toName)) {
                    return seg;
                }
            }
        }
        return null;
    }

    public boolean hasConnections() {
        if (segments != null) {
            for (SubwaySegment segment : segments) {
                Double delay = segment.delay;
                if (delay != null && delay != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private SubwaySegment addSegment(SubwayLine line, SubwayStation from, SubwayStation to, Double delay) {
        SubwaySegment sg = new SubwaySegment(from, to, delay);

        String fromName = from.name;
        ArrayList<SubwaySegment> stationSegments = segmentsByStation.get(fromName);
        if (stationSegments == null) {
            stationSegments = new ArrayList<SubwaySegment>();
            segmentsByStation.put(fromName, stationSegments);
        }
        stationSegments.add(sg);

        String toName = to.name;
        stationSegments = segmentsByStation.get(toName);
        if (stationSegments == null) {
            stationSegments = new ArrayList<SubwaySegment>();
            segmentsByStation.put(toName, stationSegments);
        }
        stationSegments.add(sg);

        String lineName = line.name;
        ArrayList<SubwaySegment> lineSegments = segmentsByLine.get(lineName);
        if (lineSegments == null) {
            lineSegments = new ArrayList<SubwaySegment>();
            segmentsByLine.put(toName, stationSegments);
        }
        lineSegments .add(sg);

        segments.add(sg);

        SubwaySegment opposite = getSegment(to, from);
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

    public SubwaySegment getSegment(SubwayStation from, SubwayStation to) {
        final String fromName = from.name;
        final String toName = to.name;
        for (SubwaySegment seg : segments) {
            if (seg.from.name.equals(fromName) && seg.to.name.equals(toName)) {
                return seg;
            }
        }
        return null;
    }

    private void fixDimensions(SubwayMap subwayMap) {
        int xmin = Integer.MAX_VALUE;
        int ymin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymax = Integer.MIN_VALUE;

        SubwayLine[] lines = subwayMap.lines;
        int linesCount = lines.length;

        for (int i = 0; i < linesCount; i++) {
            ArrayList<SubwayStation> lineStations = stationsByLine.get(lines[i].name);
            int stationsCount = lineStations.size();
            for (int j = 0; j < stationsCount; j++) {
                SubwayStation station = lineStations.get(j);
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
            ArrayList<SubwayStation> lineStations = stationsByLine.get(line.name);
            int stationsCount = lineStations.size();
            for (int j = 0; j < stationsCount; j++) {
                SubwayStation station = lineStations.get(j);
                Point p = station.point;
                if (p != null) {
                    p.offset(dx, dy);
                }
                Rect r = station.rect;
                if (r != null) {
                    r.offset(dx, dy);
                }
            }
            ArrayList<SubwaySegment> lineSegments = segmentsByLine.get(line.name);
            int segmentsCount = lineSegments.size();
            for (int j = 0; j < segmentsCount; j++) {
                SubwaySegment segment = lineSegments.get(j);
                ArrayList<Point> points = segmentNodes.get(segment);
                if (points != null) {
                    int pointsCount = points.size();
                    for (int k = 0; k < pointsCount; k++) {
                        points.get(k).offset(dx, dy);
                    }
                }

            }
        }

        subwayMap.width = xmax - xmin + 100;
        subwayMap.height = ymax - ymin + 100;
    }

    private void fillAdditionalLines(SubwayMap subwayMap, MapResource.MapAddiditionalLine al) {
        if (al.mPoints == null) return;
        SubwayLine line = linesByName.get(al.mLineName);
        SubwayStation from = stationsByName.get(al.mFromStationName);
        SubwayStation to = stationsByName.get(al.mToStationName);
        if (from != null && to != null) {
            SubwaySegment segment = getSegment(line, from, to);
            if (segment != null) {
                if (segment.additionalNodes == null) {
                    Point[] points = al.mPoints;
                    segment.additionalNodes = points;
                    if (al.mIsSpline) {
                        segment.flags = SubwaySegment.SPLINE;
                    }
                }
            } else {
                SubwaySegment opposite = getSegment(line, to, from);
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

    private Point getPoint(Point[] array, int index) {
        if (array == null) return null;
        return index >= array.length ? null : (!ZERO_POINT.equals(array[index]) ? array[index] : null);
    }

    private Rect getRect(Rect[] array, int index) {
        if (array == null) return null;
        return index >= array.length ? null : (!ZERO_RECT.equals(array[index]) ? array[index] : null);
    }

    private class DelaysString {

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

    private class StationsString {
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
