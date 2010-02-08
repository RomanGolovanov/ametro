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

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 22:21:32
 */
public class StringUtil {

    static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    static final HashMap<String, String> patternsMap = new HashMap<String, String>();

    static {
        patternsMap.put(" ", "\\s+");
        patternsMap.put("-", "\\s*-\\s*");
        patternsMap.put("_", "\\s*_\\s*");
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
