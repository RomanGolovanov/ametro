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
import java.io.StringReader;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 09.02.2010
 *         Time: 0:17:43
 */
public class CsvReadTest {

    public static void main(String[] args) throws IOException {

        String src = "1;test1;1.1;1;01.01.2010\n2;test2;2.2;0;02.02.2010\n3;;;;;;";
        StringReader sr = new StringReader(src);
        CsvReader csvReader = new CsvReader(new BufferedReader(sr));

        System.out.println("src = " + src);

        while (csvReader.next()) {
            System.out.println("csvReader.next() = true");
            System.out.println("csvReader.readInt() = " + csvReader.readInt());
            System.out.println("csvReader.readString() = " + csvReader.readString());
            System.out.println("csvReader.readDouble() = " + csvReader.readDouble());
            System.out.println("csvReader.readBoolean() = " + csvReader.readBoolean());
            System.out.println("csvReader.readDate() = " + csvReader.readDate());
        }

    }

}
