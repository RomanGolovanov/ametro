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

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 23:14:49
 */
public class CsvWriter {

    private static final String DEFAULT_SEPARATOR = ";";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

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
        if (!StringUtil.isEmpty(value)) {
            mWriter.write(value);
        }
        mColumn++;
    }

    public void writeInt(int value) throws IOException {
        writeString(Integer.toString(value));
    }

    public void writeDouble(double value) throws IOException {
        writeString(Double.toString(value));
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

    public void flush() throws IOException {
        mWriter.flush();
    }

    public void close() throws IOException {
        mWriter.close();
    }

}
