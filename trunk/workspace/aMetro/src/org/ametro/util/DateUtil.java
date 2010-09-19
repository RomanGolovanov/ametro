/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 23:50:40
 */
public class DateUtil {

    public static String getDate(Date date, String format) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String getDateUTC(Date date, String format) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        formatter.setTimeZone(timeZone);
        return formatter.format(date);
    }

    public static String getDate(Date date) {
        return getDate(date, "dd.MM.yyyy");
    }

    public static String getDateTime(Date date) {
        return getDate(date, "dd.MM.yyyy HH:mm");
    }

	public static String getLocalDate(Date date) {
        return getDate(date, "dd.MM.yyyy");
	}    
	
	public static String getLocalDate(long timestamp) {
        return getLocalDate(new Date(timestamp));
	}    
	
    public static String getTimeDate(Date date) {
        return getDate(date, "HH:mm dd.MM.yyyy");
    }

    public static String getTimeDateUTC(Date date) {
        return getDateUTC(date, "HH:mm dd.MM.yyyy");
    }

    public static String getTime(Date date) {
        return getDate(date, "HH:mm");
    }

    public static String getTimeHHMM(long seconds) {
		Date date = new Date(seconds * 1000);
        return getDateUTC(date, "HH:mm");
	}

    public static Date parseDate(String date, String format) throws ParseException {
        if (date == null || date.length() == 0) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(date);
    }

    public static Date parseDate(String date) throws ParseException {
        return parseDate(date, "dd.MM.yyyy");
    }

    public static Date parseShortDate(String date) throws ParseException {
        return parseDate(date, "dd.MM.yy");
    }

    public static Date parseTime(String date) throws ParseException {
        return parseDate(date, "HH:mm");
    }

    public static Date parseDateTime(String date) throws ParseException {
        return parseDate(date, "dd.MM.yyyy HH:mm");
    }

    public static Date parseTimeDate(String date) throws ParseException {
        return parseDate(date, "HH:mm dd.MM.yyyy");
    }

	public static long toUTC(long timestamp) {
		Calendar cal = Calendar.getInstance();
		int offset = cal.getTimeZone().getOffset(timestamp);
		return timestamp + offset;
	}

	public static long toLocal(long timestamp) {
		Calendar cal = Calendar.getInstance();
		int offset = cal.getTimeZone().getOffset(timestamp);
		return timestamp - offset;
	}

	public static long createTimestamp(int year, int month, int day) {
		Calendar cal = new GregorianCalendar(year, month, day);
		long timestamp = cal.getTime().getTime();
		int offset = cal.getTimeZone().getOffset(timestamp);
		return timestamp + offset;
	}



}
