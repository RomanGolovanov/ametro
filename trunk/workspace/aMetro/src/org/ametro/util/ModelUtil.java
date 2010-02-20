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

package org.ametro.util;

import android.util.Log;
import org.ametro.MapSettings;
import org.ametro.model.City;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayMapBuilder;
import org.ametro.pmz.FilePackage;
import org.ametro.pmz.GenericResource;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.ametro.Constants.LOG_TAG_MAIN;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 12.02.2010
 *         Time: 15:03:07
 */
public class ModelUtil {

    private static final int BUFFER_SIZE = 8192;

    public static ModelDescription loadModelDescription(String fileName) throws IOException, ClassNotFoundException {
        long startTime = System.currentTimeMillis();
        ObjectInputStream strm = null;
        ZipFile zip = null;
        try {
            zip = new ZipFile(fileName);
            ZipEntry entry = zip.getEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
            strm = new ObjectInputStream(new BufferedInputStream(zip.getInputStream(entry), BUFFER_SIZE));
            ModelDescription modelDescription = (ModelDescription) strm.readObject();
            if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
                Log.i(LOG_TAG_MAIN, "SubwayMap description '" + fileName
                        + "' loading time is " + (System.currentTimeMillis() - startTime) + "ms");
            }
            return modelDescription;
        } finally {
            if (strm != null) {
                try {
                    strm.close();
                } catch (Exception ignored) {
                }
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static SubwayMap loadModel(String fileName) throws IOException, ClassNotFoundException {
        Date startTimestamp = new Date();
        ObjectInputStream strm = null;
        ZipFile zip = null;
        try {
            zip = new ZipFile(fileName);
            ZipEntry entry = zip.getEntry(MapSettings.MAP_ENTRY_NAME);
            strm = new ObjectInputStream(new BufferedInputStream(zip.getInputStream(entry), BUFFER_SIZE));
            SubwayMap subwayMap = (SubwayMap) strm.readObject();
            if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
                Log.i(LOG_TAG_MAIN, String.format("SubwayMap data '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
            }
            return subwayMap;
        } finally {
            if (strm != null) {
                try {
                    strm.close();
                } catch (Exception ignored) {
                }
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void saveModel(SubwayMap subwayMap) throws IOException {
        Date startTimestamp = new Date();
        String fileName = MapSettings.getMapFileName(subwayMap.mapName);
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName), BUFFER_SIZE));
        saveModelDescriptionEntry(subwayMap, zip);
        saveModelEntry(subwayMap, zip);
        zip.flush();
        zip.close();
        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, String.format("SubwayMap file '%s' saving time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        }
    }

    private static void saveModelDescriptionEntry(SubwayMap subwayMap, ZipOutputStream zip) throws IOException {
        ZipEntry entry = new ZipEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
        zip.putNextEntry(entry);
        ObjectOutputStream strm = new ObjectOutputStream(zip);
        strm.writeObject(new ModelDescription(subwayMap));
        strm.flush();
        zip.closeEntry();
    }

    private static void saveModelEntry(SubwayMap subwayMap, ZipOutputStream zip) throws IOException {
        ZipEntry entry = new ZipEntry(MapSettings.MAP_ENTRY_NAME);
        zip.putNextEntry(entry);
        ObjectOutputStream strm = new ObjectOutputStream(zip);
        strm.writeObject(subwayMap);
        strm.flush();
        zip.closeEntry();
    }

    public static ModelDescription indexPmz(String fileName) throws IOException {
        Date startTimestamp = new Date();
        ModelDescription model = new ModelDescription();
        FilePackage pkg = new FilePackage(fileName);
        GenericResource info = pkg.getCityGenericResource();
        String countryName = info.getValue("Options", "Country");
        String cityName = info.getValue("Options", "RusName");
        if (cityName == null) {
            cityName = info.getValue("Options", "CityName");
        }
        model.countryName = countryName;
        model.cityName = cityName;
        model.sourceVersion = MapSettings.getSourceVersion();
        File pmzFile = new File(fileName);
        model.timestamp = pmzFile.lastModified();
        if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
            Log.i(LOG_TAG_MAIN, String.format("PMZ description '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
        }
        return model;
    }


    public static City importPmz(String filename) throws IOException {
        SubwayMapBuilder subwayMapBuilder = new SubwayMapBuilder();
        SubwayMap subwayMap = subwayMapBuilder.importPmz(filename);
        City model = new City();
        model.subwayMap = subwayMap;
        return model;
    }

}
