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

package org.ametro;

import java.io.File;

import android.os.Environment;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 10.02.2010
 *         Time: 22:50:44
 */
public class Constants {

    public static final String LOG_TAG_MAIN = "aMetro";
    
	public static final int DEFAULT_BUFFER_SIZE = 8196;

	public static final String PREFERENCE_NAME = "aMetro";

	public final static String STATION_ID = "STATION_ID";
	public final static String STATION_FROM_ID = "STATION_FROM_ID";
	public final static String STATION_TO_ID = "STATION_TO_ID";
	
    public static final String PREFERENCE_PACKAGE_FILE_NAME = "PACKAGE_FILE_NAME";
    public static final String PREFERENCE_SCROLL_POSITION = "SCROLL_POSITION";
    public static final String PREFERENCE_ZOOM_LEVEL = "ZOOM_LEVEL";
    public static final String PREFERENCE_FAVORITE_ROUTES = "FAVORITE_ROUTES";
    public static final String PREFERENCE_ONLINE_CATALOG_URL = "ONLINE_CATALOG_URL";
    public static final String PREFERENCE_DEBUG = "DEBUG";
    public static final String PREFERENCE_AUTO_UPDATE_INDEX = "AUTO_UPDATE_INDEX";
    public static final String PREFERENCE_AUTO_UPDATE_MAPS = "AUTO_UPDATE_MAPS";
    public static final String PREFERENCE_LOCALE = "LOCALE";
    public static final String PREFERENCE_PMZ_IMPORT = "PMZ_IMPORT";

    public static final String DEFAULT_MAP = "metro";

    public static final String NO_MEDIA_TAG = ".nomedia";
    public static final String MAP_FILE_TYPE = ".ametro";
    public static final String PMZ_FILE_TYPE = ".pmz";

    public static final String IMPORT_FILE_TYPE = ".import.ametro";
    public static final String DOWNLOAD_FILE_TYPE = ".download.ametro";
    
	public static final File ROOT_PATH = new File(Environment.getExternalStorageDirectory(), "ametro");
	
	public static final String ONLINE_CATALOG_PATH = "http://dl.dropbox.com/u/8171021/0.2.4.r290/catalog.zip";
	public static final File LOCAL_CATALOG_PATH = new File(ROOT_PATH, "maps");
	public static final File IMPORT_CATALOG_PATH = new File(ROOT_PATH, "import");
	public static final File TEMP_CATALOG_PATH = new File(ROOT_PATH,"temp");
	public static final File ICONS_PATH = new File(ROOT_PATH,"icons");
    
	public static final File ONLINE_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.online.xml");
	public static final File LOCAL_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.local.xml");
	public static final File IMPORT_CATALOG_STORAGE = new File(ROOT_PATH,"catalog.import.xml");
	
	public static final File NO_MEDIA_FILE = new File(ROOT_PATH, NO_MEDIA_TAG);

	public static final String PMETRO_EXTENSION = ".pmz";
	public static final String AMETRO_EXTENSION = ".ametro";

	
    
	
}
