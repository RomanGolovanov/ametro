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

package org.ametro.pmz;


import org.ametro.util.SerializeUtil;

import java.util.ArrayList;
import java.util.HashMap;


public class TransportResource implements IResource {

    //private FilePackage owner;
    private TransportParser parser;

    private String type;

    private HashMap<String, TransportLine> lines;
    private ArrayList<TransportTransfer> transfers;
    private long crc;

    public static class TransportLine {
        public String name;
        public String mapName;
        public String stationText;
        public String drivingDelaysText;
        public String timeDelaysText;
    }

    public static class TransportTransfer {

        public String name;
        public String startLine;
        public String startStation;
        public String endLine;
        public String endStation;

        public Double delay;
        public String status;
    }

    private class TransportParser {
        private String section;
        private TransportLine line;

        public void parseLine(String line) {
            if (line.startsWith(";")) return;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1);
                handleSection(section);
            } else if (line.contains("=")) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    String value = parts.length > 1 ? parts[1].trim() : "";
                    handleNaveValuePair(section, name, value);
                }
            }
        }

        private void handleSection(String section) {
            if (section.equals("Options")) {
                // do nothing ^__^
            } else if (section.equals("Transfers")) {
            } else { // Lines names
                // add line
                line = new TransportLine();
            }
        }

        private void handleNaveValuePair(String section, String name, String value) {
            if (section.equals("Options")) {
                if (name.equals("Type")) {
                    type = value;
                }
            } else if (section.equals("Transfers")) {
                String[] parts = SerializeUtil.parseStringArray(value);
                TransportTransfer transfer = new TransportTransfer();
                transfer.startLine = parts[0].trim();
                transfer.startStation = parts[1].trim();
                transfer.endLine = parts[2].trim();
                transfer.endStation = parts[3].trim();
                transfer.delay = parts.length > 4 && parts[4].length() > 0 ? Double.parseDouble(parts[4]) : null;
                transfer.status = parts.length > 5 ? parts[5] : null;
                transfers.add(transfer);
            } else { // Lines names
                if (name.equals("Name")) {
                    line.name = value;
                    lines.put(line.name, line);
                } else if (name.equals("LineMap")) {
                    line.mapName = value;
                } else if (name.equals("Stations")) {
                    line.stationText = value;
                } else if (name.equals("Driving")) {
                    line.drivingDelaysText = value;
                } else if (name.equals("Delays")) {
                    line.timeDelaysText = value;
                }
            }
        }

    }

    public void beginInitialize(FilePackage owner) {
        lines = new HashMap<String, TransportLine>();
        transfers = new ArrayList<TransportTransfer>();
        parser = new TransportParser();
    }

    public void doneInitialize() {
        parser = null;
    }

    public void parseLine(String line) {
        parser.parseLine(line.trim());
    }

    public TransportResource() {

    }

    public String getType() {
        return type;
    }

    public HashMap<String, TransportLine> getLines() {
        return lines;
    }

    public ArrayList<TransportTransfer> getTransfers() {
        return transfers;
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(long newCrc) {
        crc = newCrc;
    }


}
