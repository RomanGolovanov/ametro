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
import org.ametro.libs.DelaysString;
import org.ametro.libs.StationsString;
import org.ametro.pmz.FilePackage;
import org.ametro.pmz.GenericResource;
import org.ametro.pmz.MapResource;
import org.ametro.pmz.MapResource.MapAddiditionalLine;
import org.ametro.pmz.MapResource.MapLine;
import org.ametro.pmz.TransportResource;
import org.ametro.pmz.TransportResource.TransportLine;
import org.ametro.pmz.TransportResource.TransportTransfer;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ModelBuilder {

    private static final int BUFFER_SIZE = 8192;

    public static ModelDescription loadModelDescription(String fileName) throws IOException, ClassNotFoundException {
        Date startTimestamp = new Date();
        ObjectInputStream strm = null;
        ZipFile zip = null;
        try {
            zip = new ZipFile(fileName);
            ZipEntry entry = zip.getEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
            strm = new ObjectInputStream(new BufferedInputStream(zip.getInputStream(entry), BUFFER_SIZE));
            ModelDescription modelDescription = (ModelDescription) strm.readObject();
            Log.i("aMetro", String.format("Model description '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
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
                } catch (Exception ingnored) {
                }
            }
        }
    }

    public static Model loadModel(String fileName) throws IOException, ClassNotFoundException {
        Date startTimestamp = new Date();
        ObjectInputStream strm = null;
        ZipFile zip = null;
        try {
            zip = new ZipFile(fileName);
            ZipEntry entry = zip.getEntry(MapSettings.MAP_ENTRY_NAME);
            strm = new ObjectInputStream(new BufferedInputStream(zip.getInputStream(entry), BUFFER_SIZE));
            Model model = (Model) strm.readObject();
            Log.i("aMetro", String.format("Model data '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
            return model;
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

    public static void saveModel(Model model) throws IOException {
        Date startTimestamp = new Date();
        String fileName = MapSettings.getMapFileName(model.getMapName());
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName), BUFFER_SIZE));
        saveModelDescriptionEntry(model, zip);
        saveModelEntry(model, zip);
        zip.flush();
        zip.close();
        Log.i("aMetro", String.format("Model file '%s' saving time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
    }

    private static void saveModelDescriptionEntry(Model model, ZipOutputStream zip) throws IOException {
        ZipEntry entry = new ZipEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
        zip.putNextEntry(entry);
        ObjectOutputStream strm = new ObjectOutputStream(zip);
        strm.writeObject(new ModelDescription(model));
        strm.flush();
        zip.closeEntry();
    }

    private static void saveModelEntry(Model model, ZipOutputStream zip) throws IOException {
        ZipEntry entry = new ZipEntry(MapSettings.MAP_ENTRY_NAME);
        zip.putNextEntry(entry);
        ObjectOutputStream strm = new ObjectOutputStream(zip);
        strm.writeObject(model);
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
        model.setCountryName(countryName);
        model.setCityName(cityName);
        model.setSourceVersion(MapSettings.getSourceVersion());
        File pmzFile = new File(fileName);
        model.setTimestamp(pmzFile.lastModified());
        Log.i("aMetro", String.format("PMZ description '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        return model;
    }


    public static Model importPmz(String fileName) throws IOException {
        Date startTimestamp = new Date();
        File file = new File(fileName);
        FilePackage pkg = new FilePackage(fileName);

        GenericResource info = pkg.getCityGenericResource();
        MapResource map = pkg.getMapResource("metro.map");
        TransportResource trp = pkg.getTransportResource(map.getTransportName() != null ? map.getTransportName() : "metro.trp");

        Model model = new Model(file.getName().replace(".pmz", ""));

        String countryName = info.getValue("Options", "Country");
        String cityName = info.getValue("Options", "RusName");
        if (cityName == null) {
            cityName = info.getValue("Options", "CityName");
        }
        model.setCountryName(countryName);
        model.setCityName(cityName);
        model.setLinesWidth(map.getLinesWidth());
        model.setStationDiameter(map.getStationDiameter());
        model.setWordWrap(map.isWordWrap());
        model.setUpperCase(map.isUpperCase());

        model.setTimestamp(file.lastModified());
        model.setSourceVersion(MapSettings.getSourceVersion());

        Hashtable<String, MapLine> mapLines = map.getMapLines();
        Hashtable<String, TransportLine> transportLines = trp.getLines();

        for (TransportLine tl : transportLines.values()) {
            MapLine ml = mapLines.get(tl.mName);
            if (ml == null && tl.mName != null) {
                continue;
            }
            fillMapLines(model, tl, ml);
        }

        for (TransportTransfer t : trp.getTransfers()) {
            fillTransfer(model, t);
        }

        for (MapAddiditionalLine al : map.getAddiditionalLines()) {
            fillAdditionalLines(model, al);
        }

        fixDimensions(model);

        Log.i("aMetro", String.format("PMZ file '%s' parsing time is %sms", file.getName(), Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        return model;
    }

    private static void fixDimensions(Model model) {
        Enumeration<Line> lines = model.getLines();
        int xmin = Integer.MAX_VALUE;
        int ymin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymax = Integer.MIN_VALUE;
        while (lines.hasMoreElements()) {
            Line line = lines.nextElement();
            Enumeration<Station> stations = line.getStations();
            while (stations.hasMoreElements()) {
                Station station = stations.nextElement();
                Point p = station.getPoint();
                if (p != null) {
                    if (xmin > p.x) xmin = p.x;
                    if (ymin > p.y) ymin = p.y;

                    if (xmax < p.x) xmax = p.x;
                    if (ymax < p.y) ymax = p.y;
                }
                Rect r = station.getRect();
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

        lines = model.getLines();
        while (lines.hasMoreElements()) {
            Line line = lines.nextElement();
            Enumeration<Station> stations = line.getStations();
            while (stations.hasMoreElements()) {
                Station station = stations.nextElement();
                Point p = station.getPoint();
                if (p != null) {
                    p.offset(dx, dy);
                }
                Rect r = station.getRect();
                if (r != null) {
                    r.offset(dx, dy);
                }
            }
            for (Iterator<Segment> segments = line.getSegments(); segments.hasNext();) {
                Segment segment = segments.next();
                Point[] points = segment.getAdditionalNodes();
                if (points != null) {
                    for (int i = 0; i < points.length; i++) {
                        points[i].offset(dx, dy);
                    }
                }

            }
        }

        model.setDimension(xmax - xmin + 100, ymax - ymin + 100);
    }

    private static void fillTransfer(Model model, TransportTransfer t) {
        Station from = model.getStation(t.mStartLine, t.mStartStation);
        Station to = model.getStation(t.mEndLine, t.mEndStation);
        int flags = 0;
        if (t.mStatus != null && t.mStatus.contains("invisible")) {
            flags = Transfer.INVISIBLE;
        }
        if (from != null && to != null) {
            model.addTransfer(from, to, t.mDelay, flags);
        }
    }

    private static void fillAdditionalLines(Model model, MapAddiditionalLine al) {
        if (al.mPoints == null) return;
        Line line = model.getLine(al.mLineName);
        Station from = model.getStation(al.mLineName, al.mFromStationName);
        Station to = model.getStation(al.mLineName, al.mToStationName);
        if (from != null && to != null) {
            Segment segment = line.getSegment(from, to);
            if (segment != null) {
                if (segment.getAdditionalNodes() == null) {
                    Point[] points = al.mPoints;
                    segment.setAdditionalNodes(points);
                    if (al.mIsSpline) {
                        segment.setFlags(Segment.SPLINE);
                    }
                }
            } else {
                Segment opposite = line.getSegment(to, from);
                if (opposite != null) {
                    if (opposite.getAdditionalNodes() == null) {
                        Point[] points = new Point[al.mPoints.length];
                        for (int i = 0; i < points.length; i++) {
                            points[i] = al.mPoints[(points.length - 1) - i];
                        }
                        opposite.setAdditionalNodes(points);
                        if (al.mIsSpline) {
                            opposite.setFlags(Segment.SPLINE);
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


    private static void fillMapLines(Model model, TransportLine tl, MapResource.MapLine ml) throws IOException {
        String lineName = tl.mName;
        int lineColor = ml.linesColor | 0xFF000000;
        int labelColor = ml.labelColor > 0 ? ml.labelColor | 0xFF000000 : lineColor;
        int labelBgColor = ml.backgroundColor;
        if (labelBgColor != -1) {
            labelBgColor = labelBgColor == 0 ? Color.WHITE : labelBgColor | 0xFF000000;
        } else {
            labelBgColor = 0;
        }

        Line line = model.addLine(lineName, lineColor, labelColor, labelBgColor);
        if (ml.coordinates != null) {

            DelaysString tDelays = new DelaysString(tl.mDrivingDelaysText);
            StationsString tStations = new StationsString(tl.mStationText);
            Point[] points = ml.coordinates;
            Rect[] rects = ml.rectangles;

            int stationIndex = 0;

            Station toStation;
            Double toDelay;

            Station fromStation = null;
            Double fromDelay = null;

            Station thisStation = line.invalidateStation(
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
                            Station bracketedStation = line.invalidateStation(bracketedStationName);
                            if (isForwardDirection) {
                                line.addSegment(thisStation, bracketedStation, delays.length <= idx ? null : delays[idx]);
                            } else {
                                line.addSegment(bracketedStation, thisStation, delays.length <= idx ? null : delays[idx]);
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
                            Segment opposite = line.getSegment(fromStation, thisStation);
                            fromDelay = opposite != null ? opposite.getDelay() : null;
                        }
                        line.addSegment(thisStation, fromStation, fromDelay);
                    }
                    if (toStation != null && line.getSegment(thisStation, toStation) == null) {
                        line.addSegment(thisStation, toStation, toDelay);
                    }


                    fromStation = thisStation;

                    fromDelay = toDelay;
                    toDelay = null;

                    thisStation = toStation;
                }

            } while (tStations.hasNext());
        }
    }

}
