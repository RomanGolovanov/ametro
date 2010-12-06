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

package org.ametro.catalog;

public class CatalogMapState {
	public static final int OFFLINE = 0;
	public static final int NOT_SUPPORTED = 1;
	public static final int CORRUPTED = 2;
	public static final int UPDATE = 3;
	public static final int INSTALLED = 4;
	public static final int IMPORT = 5;
	public static final int DOWNLOAD = 6;
	public static final int UPDATE_NOT_SUPPORTED = 7;
	public static final int NEED_TO_UPDATE = 8;
	public static final int DOWNLOAD_PENDING = 9;
	public static final int IMPORT_PENDING = 10;
	public static final int DOWNLOADING = 11;
	public static final int IMPORTING = 12;
	public static final int CALCULATING = 13;
	
	public static final int IMPORT_UPDATE = 14;
	public static final int IMPORT_NEED_TO_UPDATE = 15;
}
