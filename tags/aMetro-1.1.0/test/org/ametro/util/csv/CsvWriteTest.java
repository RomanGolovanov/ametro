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

import org.ametro.util.DateUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 08.02.2010
 *         Time: 23:41:49
 */
public class CsvWriteTest {

    public static void main(String[] args) throws IOException, ParseException {
        CsvWriter csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        // first record
        csvWriter.newRecord();
        csvWriter.writeInt(1);
        csvWriter.writeString("test1");
        csvWriter.writeDouble(1.1);
        csvWriter.writeBoolean(true);
        csvWriter.writeDate(DateUtil.parseDate("01.01.2010"));

        // second record
        csvWriter.newRecord();
        csvWriter.writeInt(2);
        csvWriter.writeString("test2");
        csvWriter.writeDouble(2.2);
        csvWriter.writeBoolean(false);
        csvWriter.writeDate(DateUtil.parseDate("02.02.2010"));

        csvWriter.close();

    }

}
