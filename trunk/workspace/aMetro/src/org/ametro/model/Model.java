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

package org.ametro.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Model implements Serializable {

    public static final int VERSION = 1;

    private static final long serialVersionUID = -9024425235347648279L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(mapName);
        out.writeObject(countryName);
        out.writeObject(cityName);
        out.writeInt(width);
        out.writeInt(height);

        out.writeInt(stationDiameter);
        out.writeInt(linesWidth);

        out.writeBoolean(wordWrap);
        out.writeBoolean(upperCase);

        out.writeLong(timestamp);
        out.writeLong(crc);
        out.writeLong(sourceVersion);

        final Line[] localLines = lines;
        int linesCount = localLines.length;
        out.writeInt(linesCount);
        for (int i = 0; i < linesCount; i++) {
            out.writeObject(localLines[i]);
        }

        final Transfer[] localTransfers = transfers;
        final int transfersCount = localTransfers.length;
        out.writeInt(transfersCount);
        for (int i = 0; i < transfersCount; i++) {
            out.writeObject(localTransfers[i]);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        mapName = (String) in.readObject();
        countryName = (String) in.readObject();
        cityName = (String) in.readObject();
        width = in.readInt();
        height = in.readInt();

        stationDiameter = in.readInt();
        linesWidth = in.readInt();

        wordWrap = in.readBoolean();
        upperCase = in.readBoolean();

        timestamp = in.readLong();
        crc = in.readLong();
        sourceVersion = in.readLong();

        final HashMap<String, Line> localLinesMap = new HashMap<String, Line>();
        final int lineCount = in.readInt();
        final Line[] localLines = new Line[lineCount];
        for (int i = 0; i < lineCount; i++) {
            Line line = (Line) in.readObject();
            localLinesMap.put(line.name, line);
            localLines[i] = line;
        }
        linesMap = localLinesMap;
        lines = localLines;

        final int transferCount = in.readInt();
        final Transfer[] localTransfers = new Transfer[transferCount];
        for (int i = 0; i < transferCount; i++) {
            localTransfers[i] = (Transfer) in.readObject();
        }
        transfers = localTransfers;
    }


    public Model(String newMapName) {
        mapName = newMapName;
    }

    public long timestamp;
    public long crc;

    public String mapName;

    public String cityName;
    public String countryName;

    public int width;
    public int height;

    public int stationDiameter;
    public int linesWidth;
    public boolean wordWrap;
    public boolean upperCase;

    public long sourceVersion;

    public HashMap<String, Line> linesMap = new HashMap<String, Line>();
    public Line[] lines;
    public Transfer[] transfers;

    public Station getStation(String lineName, String stationName) {
        Line line = linesMap.get(lineName);
        if (line != null) {
            return line.getStation(stationName);
        }
        return null;
    }

    public Line getLine(String lineName) {
        return linesMap.get(lineName);
    }

}
