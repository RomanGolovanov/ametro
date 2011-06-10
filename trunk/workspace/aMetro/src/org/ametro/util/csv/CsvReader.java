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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.ametro.model.ext.ModelLocation;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.ext.ModelRect;
import org.ametro.model.ext.ModelSpline;
import org.ametro.util.StringUtil;

import android.graphics.Point;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com) Date: 08.02.2010 Time: 22:10:36
 */
public class CsvReader {

	public static final String EMPTY_VALUE = "null";
	private static final char DEFAULT_SEPARATOR = ';';

	private BufferedReader mReader;
	private char mSeparator;
	private String[] mRecord;
	private int mCurrentColumn;
	private int mTotalColumns;
	private String mEntireRecord;

	public CsvReader(BufferedReader reader, char separator) {
		mReader = reader;
		mSeparator = separator;
	}

	public CsvReader(BufferedReader reader) {
		this(reader, DEFAULT_SEPARATOR);
	}

	ArrayList<String> mLineParts = new ArrayList<String>();
	StringBuilder mLineBuilder = new StringBuilder();

	/**
	 * Получить следующую нетипизированную
	 * запись из потока без сохранения
	 * состояния
	 * 
	 * @return Следующая нетипизированная запись
	 *         из потока
	 * @throws IOException
	 */
	protected String[] readNextRecord() throws IOException {
		String line = mReader.readLine();
		while(line!=null && line.length() == 0){
			line = mReader.readLine();
		} 
		if (line != null) {
			mEntireRecord = line;
			final ArrayList<String> parts = mLineParts;
			final StringBuilder sb = mLineBuilder;
			final int length = line.length(); 
			int position = 0;
			char ch;
			parts.clear(); 
			sb.setLength(0);
			while( position < length ){
				ch = (char)line.charAt(position);
				if(ch == mSeparator){
					parts.add(sb.toString()); 
					sb.setLength(0);
				}else{
					sb.append(ch);
				}
				position++;
			}
			parts.add(sb.toString());
			return (String[]) parts.toArray(new String[parts.size()]);
		} else {
			mEntireRecord = null;
			return null;
		}
	}


	/**
	 * Сохранить следующую нетипизированную
	 * запись из потока
	 * 
	 * @return true - если запись была считана и
	 *         сохранена, false - иначе
	 * @throws IOException
	 */
	public boolean next() throws IOException {
		final String[] record = readNextRecord();
		mCurrentColumn = 0;
		if (record != null) {
			mRecord = record;
			mTotalColumns = record.length;
			return true;
		} else {
			mRecord = null;
			mTotalColumns = 0;
			return false;
		}
	}

	private static String getValue(String[] record, int currentColumn, int totalColumns) {
		if (currentColumn < totalColumns) {
			return StringUtil.notEmptyElseNull(record[currentColumn]);
		} else {
			return null;
		}
	}

	public String readString() {
		return getValue(mRecord, mCurrentColumn++, mTotalColumns);
	}

	public String getString(int index) {
		return getValue(mRecord, index, mTotalColumns);
	}

	public int readInt() {
		return StringUtil.parseInt(getValue(mRecord, mCurrentColumn++,
				mTotalColumns), 0);
	}

	public int getInt(int index) {
		return StringUtil.parseInt(getValue(mRecord, index, mTotalColumns), 0);
	}

	public long readLong() {
		return StringUtil.parseLong(getValue(mRecord, mCurrentColumn++,
				mTotalColumns), 0);
	}

	public long getLong(int index) {
		return StringUtil.parseLong(getValue(mRecord, index, mTotalColumns), 0);
	}

	public double readDouble() {
		return StringUtil.parseDouble(getValue(mRecord, mCurrentColumn++,
				mTotalColumns), 0);
	}

	public float readFloat() {
		return StringUtil.parseFloat(getValue(mRecord, mCurrentColumn++,
				mTotalColumns), 0);
	}
	
	public Double readNullableDouble() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseDouble(value, 0);
		} else {
			return null;
		}
	}

	public Integer readNullableInteger() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if ( !StringUtil.isNullOrEmpty(value) && !EMPTY_VALUE.equals(value)) {
			return StringUtil.parseInt(value, 0);
		} else {
			return null;
		}
	}
	
	public double getDouble(int index) {
		return StringUtil.parseDouble(getValue(mRecord, index, mTotalColumns), 0);
	}

	public Double getNullableDouble(int index) {
		String value = getValue(mRecord, index, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseDouble(value, 0);
		} else {
			return null;
		}
	}

	public Double getNullableDouble(int index, double defaultValue) {
		String value = getValue(mRecord, index, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseDouble(value, defaultValue);
		} else {
			return defaultValue;
		}
	}

	public boolean readBoolean() {
		return StringUtil.parseBoolean(getValue(mRecord, mCurrentColumn++,
				mTotalColumns), false);
	}

	public boolean getBoolean(int index) {
		return StringUtil.parseBoolean(getValue(mRecord, index, mTotalColumns),
				false);
	}

	public Date readDate() {
		return StringUtil.parseDate(getValue(mRecord, mCurrentColumn++,
				mTotalColumns), null);
	}

	public Date getDate(int index) {
		return StringUtil.parseDate(getValue(mRecord, index, mTotalColumns),
				null);
	}


	public Point[] readPointArray() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parsePointArray(value);
		} else {
			return null;
		}
	}


	public ModelPoint readModelPoint() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseModelPoint(value);
		} else {
			return null;
		}
	}

	public ModelPoint[] readModelPointArray() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseModelPointArray(value);
		} else {
			return null;
		}
	}

	public ModelRect readModelRect() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseModelRect(value);
		} else {
			return null;
		}
	}	
	public int getCount() {
		return mTotalColumns;
	}

	public float getFloat(int index) {
		return StringUtil.parseFloat(getValue(mRecord, index, mTotalColumns), 0);
	}

	public ModelLocation readModelLocation() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseModelLocation(value);
		} else {
			return null;
		}
	}

	public String[] readStringArray() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			if(value == null){
				return new String[0];
			}
			return StringUtil.parseStringArray(value);
		} else {
			return null;
		}
	}

	public int[] readIntArray() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			if(value == null){
				return new int[0];
			}
			return StringUtil.parseIntArray(value);
		} else {
			return null;
		}
	}

	public long[] readLongArray() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			if(value == null){
				return new long[0];
			}
			return StringUtil.parseLongArray(value);
		} else {
			return null;
		}
	}

	
	public Integer[] readIntegerArray() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			if(value == null){
				return new Integer[0];
			}
			return StringUtil.parseIntegerArray(value);
		} else {
			return null;
		}
	}

	public boolean[] readBoolArray() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			if(value == null){
				return new boolean[0];
			}
			return StringUtil.parseBoolArray(value);
		} else {
			return null;
		}
	}
	
	public Integer readInteger() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return Integer.parseInt(value);
		} else {
			return null;
		}
	}

	public ModelSpline readModelSpline() {
		String value = getValue(mRecord, mCurrentColumn++, mTotalColumns);
		if (!EMPTY_VALUE.equals(value)) {
			return StringUtil.parseModelSpline(value);
		} else {
			return null;
		}
	}

	public void skipColumns(int count) {
		mCurrentColumn+=count;
	}

	public String getEntireRecord() {
		return mEntireRecord;
	}

	public boolean isComment(){
		return mEntireRecord!=null && (mEntireRecord.startsWith("#") || mEntireRecord.startsWith(";"));
	}
}
