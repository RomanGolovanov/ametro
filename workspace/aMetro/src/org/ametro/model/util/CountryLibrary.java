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
package org.ametro.model.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.ametro.model.ext.ModelLocation;
import org.ametro.util.csv.CsvReader;

import android.content.Context;

public  class CountryLibrary
{
	
	public static class CountryLibraryRecord
	{
		public String CountryNameRu;
		public String CityNameRu;
		public String CountryNameEn;
		public String CityNameEn;
		public ModelLocation Location;
	}
	
	public static void setContext(Context context)
	{
		mContext = context;
	}

	public static CountryLibraryRecord search(String country, String city){
		if(mContext==null) return null;
		if(!mInitialized) initialize();
		HashMap<String, CountryLibraryRecord> city2rec = mIndex.get(country);
		if(city2rec!=null){
			return city2rec.get(city);
		}
		return null;
	}
	
	private static void initialize(){
		try {
			InputStream strm = mContext.getAssets().open("cities.csv");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),';');
			while(reader.next()){
				CountryLibraryRecord r = new CountryLibraryRecord();
				r.CityNameRu = reader.getString(1);
				r.CityNameEn = reader.getString(2);
				r.CountryNameRu = reader.getString(3);
				r.CountryNameEn = reader.getString(4);
				r.Location = new ModelLocation(reader.getFloat(5), reader.getFloat(6) );
				HashMap<String, CountryLibraryRecord> city2rec = mIndex.get(r.CountryNameRu);
				if(city2rec==null){
					city2rec = new HashMap<String, CountryLibraryRecord>();
					mIndex.put(r.CountryNameRu, city2rec);
				}
				city2rec.put(r.CityNameRu, r);
			}
		} catch (Throwable e) {
		}
		mInitialized = true;
	}
	
	private static Context mContext;
	private final static HashMap<String, HashMap<String,CountryLibraryRecord>> mIndex = new HashMap<String, HashMap<String,CountryLibraryRecord>>();
	private static boolean mInitialized = false;
	
}
