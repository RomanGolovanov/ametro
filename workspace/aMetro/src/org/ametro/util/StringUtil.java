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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 22:21:32
 */
public class StringUtil {

    static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    static final HashMap<String, String> patternsMap = new HashMap<String, String>();
    private static final String[] charTable = new String[81];
    private static final char START_CHAR = 'Ё';
    
    static {
        patternsMap.put(" ", "\\s+");
        patternsMap.put("-", "\\s*-\\s*");
        patternsMap.put("_", "\\s*_\\s*");

        charTable['А'- START_CHAR] = "A";
        charTable['Б'- START_CHAR] = "B";
        charTable['В'- START_CHAR] = "V";
        charTable['Г'- START_CHAR] = "G";
        charTable['Д'- START_CHAR] = "D";
        charTable['Е'- START_CHAR] = "E";
        charTable['Ё'- START_CHAR] = "YO";
        charTable['Ж'- START_CHAR] = "ZH";
        charTable['З'- START_CHAR] = "Z";
        charTable['И'- START_CHAR] = "I";
        charTable['Й'- START_CHAR] = "J";
        charTable['К'- START_CHAR] = "K";
        charTable['Л'- START_CHAR] = "L";
        charTable['М'- START_CHAR] = "M";
        charTable['Н'- START_CHAR] = "N";
        charTable['О'- START_CHAR] = "O";
        charTable['П'- START_CHAR] = "P";
        charTable['Р'- START_CHAR] = "R";
        charTable['С'- START_CHAR] = "S";
        charTable['Т'- START_CHAR] = "T";
        charTable['У'- START_CHAR] = "U";
        charTable['Ф'- START_CHAR] = "F";
        charTable['Х'- START_CHAR] = "X";
        charTable['Ц'- START_CHAR] = "CZ";
        charTable['Ч'- START_CHAR] = "CH";
        charTable['Ш'- START_CHAR] = "SH";
        charTable['Щ'- START_CHAR] = "SHH";
        charTable['Ъ'- START_CHAR] = "'";
        charTable['Ы'- START_CHAR] = "Y'";
        charTable['Ь'- START_CHAR] = "'";
        charTable['Э'- START_CHAR] = "E";
        charTable['Ю'- START_CHAR] = "YU";
        charTable['Я'- START_CHAR] = "YA";

        for (int i = 0; i < charTable.length; i++) {
            char idx = (char)((char)i + START_CHAR);
            char lower = new String(new char[]{idx}).toLowerCase().charAt(0);
            if (charTable[i] != null) {
                charTable[lower - START_CHAR] = charTable[i].toLowerCase();
            }
        }
    }

    public static String toTranslit(String text) {
        char charBuffer[] = text.toCharArray();
        StringBuilder sb = new StringBuilder(text.length());
        for (char symbol : charBuffer) {
            int i = symbol - START_CHAR;
            if (i>=0 && i<charTable.length) {
                String replace = charTable[i];
                sb.append(replace == null ? symbol : replace);
            }
            else {
                sb.append(symbol);
            }
        }
        return sb.toString();
    }
    
    
    public static boolean isEmpty(String value) {
        return value == null || "".equals(value.trim());
    }

    public static String notEmptyElseNull(String value) {
        return isEmpty(value) ? null : value;
    }

    public static String notEmptyElseDefault(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }

    public static int parseInt(String value, int defaultValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static long parseLong(String value, long defaultValue) {
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static double parseDouble(String value, double defaultValue) {
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static boolean parseBoolean(String value, boolean defaultValue) {
        return value != null
                ? "true".equalsIgnoreCase(value.trim()) || parseInt(value, 0) > 0
                : defaultValue;
    }

    public static Date parseDate(String value, String format) {
        if (value != null) {
            try {
                SimpleDateFormat dateFormat;
                if (format == null) {
                    dateFormat = DEFAULT_DATE_FORMAT;
                } else {
                    dateFormat = new SimpleDateFormat(format);
                }
                return dateFormat.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    public static Rect parseRect(String value) {
        String[] parts = value.split(",");
        int x1 = Integer.parseInt(parts[0].trim());
        int y1 = Integer.parseInt(parts[1].trim());
        int x2 = Integer.parseInt(parts[2].trim());
        int y2 = Integer.parseInt(parts[3].trim());
        return new Rect(x1, y1, x2, y2);
    }

    public static Point parsePoint(String value) {
        String[] parts = value.split(",");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        return new Point(x, y);
    }

	public static String formatRect(Rect rect) {
		return rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom;
	}

	public static String formatPoint(Point point) {
		return point.x + "," + point.y;
	}

	public static String formatPointF(PointF point) {
		return point.x + "," + point.y;
	}
	
    public static <T> String join(Collection<T> objs, String delimiter) {
        StringBuilder builder = new StringBuilder();

        for (Iterator<T> it = objs.iterator(); it.hasNext();) {
            T obj = it.next();
            builder.append(obj);
            if (it.hasNext()) {
                builder.append(delimiter);
            }
        }

        return builder.toString();
    }

    public static String capitalize(String src) {
        return src.substring(0, 1).toUpperCase() + src.substring(1);
    }

    public static String capitalize2(String src, boolean useDelimiters) {
        StringBuilder result = new StringBuilder(src);
        boolean processed = false;
        for (String patternReplacement : patternsMap.keySet()) {
            String[] splitted = result.toString().split(patternsMap.get(patternReplacement));
            if (splitted.length > 1) {
                result = new StringBuilder();
                processed = true;
                for (String subString : splitted) {
                    if (useDelimiters && result.length() != 0) {
                        result.append(patternReplacement);
                    }
                    result.append(capitalize(subString));
                }
                break;
            }
        }
        if (processed) {
            return result.toString();
        } else {
            return capitalize(src);
        }
    }


}
