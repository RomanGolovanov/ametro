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

package org.ametro.util;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerializeUtil {

    private static String[] splitCommaSeparaterString(String value) {
    	if(value == null || value.length() == 0) return new String[0];
        value = value.replaceAll("/\\(.*\\)/", "");
        return value.split(",");
    }

    private static final Pattern csvPattern = Pattern.compile("(?:^|,)(\"(?:[^\"]|\"\")*\"|[^,]*)");


    public static String[] parseStringArray(String line) {
        ArrayList<String> elements = new ArrayList<String>();
        Matcher m = csvPattern.matcher(line);
        while (m.find()) {
            elements.add(m.group()
                    .replaceAll("^,", "") // remove first comma if any
                            //.replaceAll( "^?\"(.*)\"$", "$1" ) // remove outer quotations if any
                    .replaceAll("^\"(.*)\"$", "$1") // remove outer quotations if any
                    .replaceAll("\"\"", "\"")); // replace double inner quotations if any
        }
        return elements.toArray(new String[elements.size()]);
    }

    public static Double[] parseDoubleArray(String value) {
        String[] parts = splitCommaSeparaterString(value);
        ArrayList<Double> vals = new ArrayList<Double>();
        for (String part : parts) {
            try {
                Double val = Double.parseDouble(part.trim());
                vals.add(val);
            } catch (Exception ex) {
                vals.add(null);
            }
        }
        return vals.toArray(new Double[vals.size()]);
    }

    public static Point[] parsePointArray(String value) {
        String[] parts = splitCommaSeparaterString(value);
        ArrayList<Point> points = new ArrayList<Point>();
        for (int i = 0; i < parts.length / 2; i++) {
            Point point = new Point();
            point.x = Integer.parseInt(parts[i * 2].trim());
            point.y = Integer.parseInt(parts[i * 2 + 1].trim());
            points.add(point);
        }
        return points.toArray(new Point[points.size()]);
    }

    public static Rect[] parseRectangleArray(String value) {
        String[] parts = splitCommaSeparaterString(value);
        ArrayList<Rect> rectangles = new ArrayList<Rect>();
        for (int i = 0; i < parts.length / 4; i++) {
            int x1 = Integer.parseInt(parts[i * 4].trim());
            int y1 = Integer.parseInt(parts[i * 4 + 1].trim());
            int x2 = x1 + Integer.parseInt(parts[i * 4 + 2].trim());
            int y2 = y1 + Integer.parseInt(parts[i * 4 + 3].trim());
            rectangles.add(new Rect(x1, y1, x2, y2));
        }
        return rectangles.toArray(new Rect[rectangles.size()]);
    }

    public static Rect parseRectangle(String value) {
        String[] parts = splitCommaSeparaterString(value);
        int x1 = Integer.parseInt(parts[0].trim());
        int y1 = Integer.parseInt(parts[1].trim());
        int x2 = x1 + Integer.parseInt(parts[2].trim());
        int y2 = y1 + Integer.parseInt(parts[3].trim());
        return new Rect(x1, y1, x2, y2);
    }

    public static Point parsePoint(String value) {
        String[] parts = splitCommaSeparaterString(value);
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        return new Point(x, y);
    }

    public static PointF parsePointF(String value) {
        String[] parts = splitCommaSeparaterString(value);
        float x = Float.parseFloat(parts[0].trim());
        float y = Float.parseFloat(parts[1].trim());
        return new PointF(x, y);
    }

    public static Integer parseNullableInteger(String text) {
        if (text != null && !text.equals("")) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static Double parseNullableDouble(String text) {
        if (text != null && !text.equals("")) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

}
