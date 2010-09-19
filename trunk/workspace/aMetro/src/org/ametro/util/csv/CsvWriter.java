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

package org.ametro.util.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ametro.model.ext.ModelLocation;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.ext.ModelRect;
import org.ametro.model.ext.ModelSpline;
import org.ametro.util.StringUtil;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 23:14:49
 */
public class CsvWriter {

	public static final String EMPTY_VALUE = "null";
	private static final String DEFAULT_SEPARATOR = ";";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss z");

	private BufferedWriter mWriter;
	private String mSeparator;
	private int mRow = -1;
	private int mColumn;

	public SimpleDateFormat dateFormat = DATE_FORMAT;

	public CsvWriter(BufferedWriter writer, String separator) {
		mWriter = writer;
		mSeparator = separator;
	}

	public CsvWriter(BufferedWriter writer) {
		this(writer, DEFAULT_SEPARATOR);
	}

	public void newRecord() throws IOException {
		if (mRow < 0) {
			mRow = 0;
		} else {
			mRow++;
			mColumn = 0;
			mWriter.newLine();
		}
	}

	public void writeString(String value) throws IOException {
		if (mColumn > 0) {
			mWriter.write(mSeparator);
		}
		if (!StringUtil.isNullOrEmpty(value)) {
			mWriter.write(value);
		}
		mColumn++;
	}

	public void writeInt(int value) throws IOException {
		writeString(Integer.toString(value));
	}

	public void writeLong(long value) throws IOException {
		writeString(Long.toString(value));
	}

	public void writeDouble(double value) throws IOException {
		writeString(Double.toString(value));
	}

	public void writeNullableDouble(Double value) throws IOException {
		if (value != null) {
			writeString(Double.toString(value));
		} else {
			writeString(EMPTY_VALUE);
		}
	}

	public void writeFloat(float value) throws IOException {
		writeString(Float.toString(value));
	}

	public void writeBoolean(boolean value) throws IOException {
		if (value) {
			writeString("1");
		} else {
			writeString("0");
		}
	}

	public void writeDate(Date value) throws IOException {
		if (value != null) {
			writeString(dateFormat.format(value));
		}
	}

	public void writeRect(Rect rect) throws IOException {
		if(rect!=null){
			writeString(StringUtil.formatRect(rect));
		}else{
			writeString(EMPTY_VALUE);
		}
	}

	public void writePoint(Point point) throws IOException {
		if(point!=null){
			writeString(StringUtil.formatPoint(point));
		}else{
			writeString(EMPTY_VALUE);
		}
	}

	public void writePointArray(Point[] points) throws IOException {
		StringBuffer sb = new StringBuffer(points.length * 3 * 3);
		for(Point p : points){
			sb.append(p.x);
			sb.append(",");
			sb.append(p.y);
			sb.append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		writeString(sb.toString());
	}

	public void writeModelRect(ModelRect rect) throws IOException {
		if(rect!=null){
			writeString(StringUtil.formatModelRect(rect));
		}else{
			writeString(EMPTY_VALUE);
		}
	}

	public void writeModelPoint(ModelPoint point) throws IOException {
		if(point!=null){
			writeString(StringUtil.formatModelPoint(point));
		}else{
			writeString(EMPTY_VALUE);
		}
	}

	public void writeModelPointArray(ModelPoint[] points) throws IOException {
		StringBuffer sb = new StringBuffer(points.length * 3 * 3);
		for(ModelPoint p : points){
			sb.append(p.x);
			sb.append(",");
			sb.append(p.y);
			sb.append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		writeString(sb.toString());
	}	

	public void flush() throws IOException {
		mWriter.flush();
	}

	public void close() throws IOException {
		mWriter.close();
	}

	public void writeIntArray(int[] value) throws IOException {
		if(value != null){
			writeString(StringUtil.formatIntArray(value));	
		}else{
			writeString(EMPTY_VALUE);
		}
	}
	
	public void writeIntegerArray(Integer[] value) throws IOException {
		if(value != null){
			writeString(StringUtil.formatIntegerArray(value));	
		}else{
			writeString(EMPTY_VALUE);
		}
	}

	public void writeModelLocation(ModelLocation value) throws IOException {
		if (value != null) {
			writeString(StringUtil.formatModelLocation(value));
		} else {
			writeString(EMPTY_VALUE);
		}	
	}

	public void writeStringArray(String[] value) throws IOException {
		if (value != null) {
			writeString(StringUtil.formatStringArray(value));
		} else {
			writeString(EMPTY_VALUE);
		}		
	}

	public void writeInteger(Integer value) throws IOException {
		if (value != null) {
			writeString(Integer.toString(value));
		} else {
			writeString(EMPTY_VALUE);
		}
	}

	public void writeModelSpline(ModelSpline value) throws IOException {
		if (value != null) {
			writeString(StringUtil.formatModelSpline(value));
		} else {
			writeString(EMPTY_VALUE);
		}	
	}

	public void writeLongArray(long[] value) throws IOException{
		if(value != null){
			writeString(StringUtil.formatLongArray(value));	
		}else{
			writeString(EMPTY_VALUE);
		}	
	}

	public void writeBoolArray(boolean[] value) throws IOException{
		if(value != null){
			writeString(StringUtil.formatBoolArray(value));	
		}else{
			writeString(EMPTY_VALUE);
		}
	}


}
