/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com
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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class GenericResource implements IResource {

    private class Parser {
        private String section = null;

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
            mKeys.add(section);
            mSections.put(section, new Hashtable<String, String>());
        }

        private void handleNaveValuePair(String section, String name, String value) {
            mSections.get(section).put(name, value);
        }

    }

    public void beginInitialize(FilePackage owner) {
        mKeys = new ArrayList<String>();
        mSections = new Hashtable<String, Dictionary<String, String>>();
        mParser = new Parser();

    }

    public void doneInitialize() {
        mParser = null;
    }

    public void parseLine(String line) {
        mParser.parseLine(line);
    }

    public String[] getSections() {
        return mKeys.toArray(new String[mKeys.size()]);
    }

    public Dictionary<String, String> getSection(String sectionName) {
        return mSections.get(sectionName);
    }

    public String getValue(String sectionName, String parameter) {
        Dictionary<String, String> section = mSections.get(sectionName);
        return section != null ? section.get(parameter) : null;
    }

    private ArrayList<String> mKeys;
    private Dictionary<String, Dictionary<String, String>> mSections;
    private Parser mParser;
    private long mCrc;

    public long getCrc() {
        return mCrc;
    }

    public void setCrc(long crc) {
        mCrc = crc;
    }
}
