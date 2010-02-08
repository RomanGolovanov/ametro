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

package org.ametro.util.csv;

import org.ametro.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 22:10:36
 */
public class CsvReader {

    private static final String DEFAULT_SEPARATOR = ";";

    private BufferedReader mReader;
    private String mSeparator;
    private String[] mRecord;
    private int mCurrentColumn;
    private int mTotalColumns;

    public CsvReader(BufferedReader reader, String separator) {
        mReader = reader;
        mSeparator = separator;
    }

    public CsvReader(BufferedReader reader) {
        this(reader, DEFAULT_SEPARATOR);
    }

    /**
     * Получить следующую нетипизированную запись из потока
     * без сохранения состояния
     *
     * @return Следующая нетипизированная запись из потока
     * @throws IOException
     */
    public String[] readNextRecord() throws IOException {
        final String line = mReader.readLine();
        if (line != null) {
            return line.split(mSeparator);
        } else {
            return null;
        }
    }

    /**
     * Сохранить следующую нетипизированную запись из потока
     *
     * @return true - если запись была считана и сохранена, false - иначе
     * @throws IOException
     */
    public boolean storeNextRecord() throws IOException {
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

    public String getString() {
        if (mCurrentColumn < mTotalColumns) {
            return StringUtil.notEmptyElseNull(mRecord[mCurrentColumn++]);
        } else {
            return null;
        }
    }

    public int getInt() {
        return StringUtil.parseInt(getString(), 0);
    }

    public double getDouble() {
        return StringUtil.parseDouble(getString(), 0);
    }

    public boolean getBoolean() {
        return StringUtil.parseBoolean(getString(), false);
    }

    public Date getDate() {
        return StringUtil.parseDate(getString(), null);
    }
}
