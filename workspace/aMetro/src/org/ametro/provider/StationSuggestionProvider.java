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

package org.ametro.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class StationSuggestionProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://org.ametro.provider.stationsuggestionprovider");

	
	/**
	 * @see android.content.ContentProvider#delete(Uri,String,String[])
	 */
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	/**
	 * @see android.content.ContentProvider#getType(Uri)
	 */
	public String getType(Uri uri) {
		return null;
	}

	/**
	 * @see android.content.ContentProvider#insert(Uri,ContentValues)
	 */
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	/**
	 * @see android.content.ContentProvider#onCreate()
	 */
	public boolean onCreate() {
		return false;
	}

	/**
	 * @see android.content.ContentProvider#query(Uri,String[],String,String[],String)
	 */
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	/**
	 * @see android.content.ContentProvider#update(Uri,ContentValues,String,String[])
	 */
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
