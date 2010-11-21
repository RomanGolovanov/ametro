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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ametro.model.ext.ModelLocation;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.ext.ModelRect;
import org.ametro.model.ext.ModelSpline;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 22:21:32
 */
public class StringUtil {

	public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	public static final Collator COLLATOR = Collator.getInstance();

	static{
		COLLATOR.setStrength(Collator.PRIMARY);
	}
	
	public static boolean startsWithoutDiacritics(String text, String prefix){
		final int textLength = text.length();
		final int prefixLength = prefix.length();
		if(textLength<prefixLength){
			return false;
		}
		String textPrefix = text.substring(0, prefixLength);
		return COLLATOR.compare(textPrefix, prefix) == 0;
	}
	
	private static final Pattern csvPattern = Pattern.compile("(?:^|,)(\"(?:[^\"]|\"\")*\"|[^,]*)");

	public static String formatStringArray(String[] value) {
		return join(value,",");
	}

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

	public static ArrayList<String> fastSplitToList(final String line, final char delimeter) {
		final ArrayList<String> parts = new ArrayList<String>();
		final StringBuilder sb = new StringBuilder();
		final int length = line.length(); 
		int position = 0;
		char ch;
		parts.clear();
		sb.setLength(0);
		while( position < length ){
			ch = (char)line.charAt(position);
			if(ch == delimeter){
				parts.add(sb.toString()); 
				sb.setLength(0);
			}else{
				sb.append(ch);
			}
			position++;
		}
		parts.add(sb.toString());
		return parts;	
	}
	
	
	public static String[] fastSplit(final String line, final char delimeter) {
		final ArrayList<String> parts = fastSplitToList(line, delimeter);
		return (String[]) parts.toArray(new String[parts.size()]);
	}

	public static String[] fastSplit(final String line) {
		final ArrayList<String> parts = new ArrayList<String>();
		final StringBuilder sb = new StringBuilder();
		final int length = line.length(); 
		int position = 0;
		char ch;
		parts.clear();
		sb.setLength(0);
		while( position < length ){
			ch = (char)line.charAt(position);
			if(ch == ','){
				parts.add(sb.toString()); 
				sb.setLength(0);
			}else{
				sb.append(ch);
			}
			position++;
		}
		parts.add(sb.toString());
		return (String[]) parts.toArray(new String[parts.size()]);
	}


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
		if(text == null) return null;
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


	public static boolean isNullOrEmpty(String value) {
		return value == null || "".equals(value.trim());
	}

	public static String notEmptyElseNull(String value) {
		return isNullOrEmpty(value) ? null : value;
	}

	public static String notEmptyElseDefault(String value, String defaultValue) {
		return isNullOrEmpty(value) ? defaultValue : value;
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
		? "true".equalsIgnoreCase(value.trim()) || "yes".equalsIgnoreCase(value.trim()) || parseInt(value, 0) > 0
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

	public static String formatRect(Rect rect) {
		return rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom;
	}

	public static String formatRectF(RectF rect) {
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

	public static String join(String[] objs, String delimiter) {
		StringBuilder builder = new StringBuilder();
		final int len = objs.length;
		for (int i = 0; i < len; i++) {
			if(i>0){
				builder.append(delimiter);
			}
			builder.append(objs[i]);
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


	public static float parseFloat(String value, float defaultValue) {
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	public static int[] parseIntArray(String text) {
		final String[] parts = fastSplit(text);
		if(parts==null){
			return new int[0];
		}		
		final int len = parts.length;
		final int[] r = new int[len];
		for (int i = 0; i < len; i++) {
			try {
				r[i] = Integer.parseInt(parts[i]);
			} catch (NumberFormatException e) {
				r[i] = -1;
			}
		}
		return r;
	}
	
	public static long[] parseLongArray(String text) {
		final String[] parts = fastSplit(text);
		if(parts==null){
			return new long[0];
		}		
		final int len = parts.length;
		final long[] r = new long[len];
		for (int i = 0; i < len; i++) {
			try {
				r[i] = Long.parseLong(parts[i]);
			} catch (NumberFormatException e) {
				r[i] = -1;
			}
		}
		return r;
	}

	public static Integer[] parseIntegerArray(String text) {
		final String[] parts = fastSplit(text);
		if(parts==null){
			return new Integer[0];
		}		
		final int len = parts.length;
		final Integer[] r = new Integer[len];
		for (int i = 0; i < len; i++) {
			try {
				r[i] = Integer.parseInt(parts[i]);
			} catch (NumberFormatException e) {
				r[i] = null;
			}
		}
		return r;
	}
	
	public static Integer parseDelay(String text) {
		if(text==null || text.length()==0) return null;
		try {
			double value = Double.parseDouble(text);
			int minutes = (int)value;
			int seconds = (int)((value - minutes) * 100);
			int delay = (minutes * 60) + seconds;
			return delay;
		} catch (Exception ex) {
			return null;
		}
	}
		
	public static Integer[] parseDelayArray(String text) {
		String[] parts = fastSplit(text);
		if(parts==null){
			return new Integer[0];
		}
		ArrayList<Integer> vals = new ArrayList<Integer>();
		for (String part : parts) {
			try {
				double value = Double.parseDouble(part.trim());
				int minutes = (int)value;
				int seconds = (int)((value - minutes) * 100);
				int delay = (minutes * 60) + seconds;
				vals.add( delay );
			} catch (Exception ex) {
				vals.add(null);
			}
		}
		return vals.toArray(new Integer[vals.size()]);
	}


	public static Integer parseNullableDelay(String text){
		if (text != null && !text.equals("")) {
			try {
				double value = Double.parseDouble(text);
				int minutes = (int)value;
				int seconds = (int)((value - minutes) * 100);
				return (minutes * 60) + seconds;
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	public static ModelPoint[] parseModelPointArray(String value) {
		if(StringUtil.isNullOrEmpty(value)) return new ModelPoint[0];
		ArrayList<ModelPoint> points = parseModelPointList(value);
		return points.toArray(new ModelPoint[points.size()]);
	}

	public static ArrayList<ModelPoint> parseModelPointList(String value) {
		if(StringUtil.isNullOrEmpty(value)) return new ArrayList<ModelPoint>();
		String[] parts = StringUtil.fastSplit(value);
		ArrayList<ModelPoint> points = new ArrayList<ModelPoint>();
		for (int i = 0; i < parts.length / 2; i++) {
			ModelPoint point = new ModelPoint();
			point.x = Integer.parseInt(parts[i * 2].trim());
			point.y = Integer.parseInt(parts[i * 2 + 1].trim());
			points.add(point);
		}
		return points;
	}

	public static String formatModelRect(ModelRect rect) {
		return rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom;
	}

	public static ModelRect parseModelRect(String value) {
		if(StringUtil.isNullOrEmpty(value)) return null;
		String[] parts = StringUtil.fastSplit(value);
		int x1 = Integer.parseInt(parts[0].trim());
		int y1 = Integer.parseInt(parts[1].trim());
		int x2 = Integer.parseInt(parts[2].trim());
		int y2 = Integer.parseInt(parts[3].trim());
		return new ModelRect(x1, y1, x2, y2);
	}

	public static ModelRect[] parseModelRectArray(String value) {
		if(StringUtil.isNullOrEmpty(value)) return new ModelRect[0];
		String[] parts = StringUtil.fastSplit(value);
		ArrayList<ModelRect> rects = new ArrayList<ModelRect>();
		for (int i = 0; i < parts.length / 4; i++) {
			int x1 = Integer.parseInt(parts[i * 4].trim());
			int y1 = Integer.parseInt(parts[i * 4 + 1].trim());
			int x2 = Integer.parseInt(parts[i * 4 + 2].trim());
			int y2 = Integer.parseInt(parts[i * 4 + 3].trim());
			rects.add(new ModelRect(x1, y1, x2, y2));
		}
		return rects.toArray(new ModelRect[rects.size()]);
	}

	public static ModelRect[] parsePmzModelRectArray(String value) {
		if(StringUtil.isNullOrEmpty(value)) return new ModelRect[0];
		String[] parts = StringUtil.fastSplit(value);
		ArrayList<ModelRect> rects = new ArrayList<ModelRect>();
		for (int i = 0; i < parts.length / 4; i++) {
			int x1 = Integer.parseInt(parts[i * 4].trim());
			int y1 = Integer.parseInt(parts[i * 4 + 1].trim());
			int x2 = x1+Integer.parseInt(parts[i * 4 + 2].trim());
			int y2 = y1+Integer.parseInt(parts[i * 4 + 3].trim());
			rects.add(new ModelRect(x1, y1, x2, y2));
		}
		return rects.toArray(new ModelRect[rects.size()]);
	}


	public static ModelPoint parseModelPoint(String value) {
		if(StringUtil.isNullOrEmpty(value)) return null;
		String[] parts = StringUtil.fastSplit(value);
		int x = Integer.parseInt(parts[0].trim());
		int y = Integer.parseInt(parts[1].trim());
		return new ModelPoint(x, y);
	}

	public static int parseColor(String value) {
		if(StringUtil.isNullOrEmpty(value)) return 0;
		return Integer.parseInt(value, 16);
	}

	public static int parseColor(String value, int defaultValue) {
		if(StringUtil.isNullOrEmpty(value)) return defaultValue;
		return Integer.parseInt(value, 16);
	}
	
	public static Double[] parseDoubleArray(String value) {
		String[] parts = fastSplit(value);
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
		ArrayList<Point> points = parsePointList(value);
		return points.toArray(new Point[points.size()]);
	}

	public static ArrayList<Point> parsePointList(String value) {
		String[] parts = fastSplit(value);
		ArrayList<Point> points = new ArrayList<Point>();
		for (int i = 0; i < parts.length / 2; i++) {
			Point point = new Point();
			point.x = Integer.parseInt(parts[i * 2].trim());
			point.y = Integer.parseInt(parts[i * 2 + 1].trim());
			points.add(point);
		}
		return points;
	}

	public static PointF parsePointF(String value) {
		String[] parts = fastSplit(value);
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

	public static Float parseNullableFloat(String text) {
		if (text != null && !text.equals("")) {
			try {
				return Float.parseFloat(text);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	
	public static String formatModelPoint(ModelPoint point) {
		return point.x + "," + point.y;
	}

	public static String formatIntArray(int[] value) {
		StringBuffer sb = new StringBuffer(value.length * 3 * 3);
		for(int i : value){
			sb.append(i);
			sb.append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();	
	}

	public static String formatLongArray(long[] value) {
		StringBuffer sb = new StringBuffer(value.length * 3 * 3);
		for(long i : value){
			sb.append(i);
			sb.append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();	
	}

	public static String formatIntegerArray(Integer[] value) {
		StringBuffer sb = new StringBuffer(value.length * 3 * 3);
		for(Integer i : value){
			if(i != null){
				sb.append(i);
			}
			sb.append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();	
	}


	public static String formatModelLocation(ModelLocation value) {
		return value.latitude + "," + value.longtitude + "," + value.height + "," + value.radius;
	}

	public static ModelLocation parseModelLocation(String value) {
		String[] parts = fastSplit(value);
		float latitude = Float.parseFloat(parts[0]);
		float longtitude = Float.parseFloat(parts[1]);
		float height = Float.parseFloat(parts[2]);
		float radius = Float.parseFloat(parts[3]);
		return new ModelLocation(latitude, longtitude, height, radius);
	}

	public static String formatModelSpline(ModelSpline value) {
		StringBuffer sb = new StringBuffer();
		sb.append(Boolean.toString(value.isSpline));
		sb.append(",");
		final int len = value.points.length;
		
		for(int i = 0; i < len; i++){
			ModelPoint p = value.points[i];
			sb.append(formatModelPoint(p));		
			if( (i+1)<len ){
				sb.append(",");
			}
		}
		return sb.toString();			
	}

	public static ModelSpline parseModelSpline(String value) {
		if(StringUtil.isNullOrEmpty(value)){
			return null;
		}
		final int firstCommaPosition = value.indexOf(',');
		ModelSpline spline = new ModelSpline();
		spline.isSpline = Boolean.parseBoolean(value.substring(0, firstCommaPosition));
		spline.points = parseModelPointArray(value.substring(firstCommaPosition+1));
		return spline;
	}

	public static String[] getResourceStringArray(Context context, int[] names) {
		Resources r = context.getResources();
		final int len = names.length;
		String[] res = new String[len];
		for(int i =0;i<len;i++){
			res[i] = r.getString(names[i]);
		}
		return res;
	}

	public static String formatFileSize(long longSize, int decimalPos)
	  {
	     NumberFormat fmt = NumberFormat.getNumberInstance();
	     if (decimalPos >= 0)
	     {
	        fmt.setMaximumFractionDigits(decimalPos);
	     }
	     final double size = longSize;
	     double val = size / (1024 * 1024);
	     if (val > 1)
	     {
	        return fmt.format(val).concat(" MB");
	     }
	     val = size / 1024;
	     if (val > 1)
	     {
	        return fmt.format(val).concat(" KB");
	     }
	     return fmt.format(size).concat(" bytes");
	  }

	public static boolean[] parseBoolArray(String text) {
		final String[] parts = fastSplit(text);
		if(parts==null){
			return new boolean[0];
		}		
		final int len = parts.length;
		final boolean[] r = new boolean[len];
		for (int i = 0; i < len; i++) {
			try {
				r[i] = Boolean.parseBoolean(parts[i]);
			} catch (NumberFormatException e) {
				r[i] = false;
			}
		}
		return r;
	}

	public static String formatBoolArray(boolean[] value) {
		StringBuffer sb = new StringBuffer(value.length * 3 * 3);
		for(boolean i : value){
			sb.append(i);
			sb.append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();	
	}


	public static String toString(Throwable th){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    th.printStackTrace(new PrintStream(out));
	    return new String(out.toByteArray());
	}

}
