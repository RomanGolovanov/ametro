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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.ametro.model.ext.ModelLocation;
import org.ametro.util.csv.CsvReader;

import android.content.Context;

public  class StationLibrary
{
	
	public static class StationLibraryRecord
	{
		public String LineSystemName;
		public String StationSystemName;
		public ModelLocation Location;
	}
	

	private StationLibrary(HashMap<String,HashMap<String,StationLibraryRecord>> index){
		mIndex = index;
	}

	public ModelLocation getStationLocation(String lineSystemName, String stationSystemName){
		StationLibraryRecord r = getRecord(lineSystemName, stationSystemName);
		if(r!=null){
			return r.Location;
		}
		return null;
	}
	
	public StationLibraryRecord getRecord(String lineSystemName, String stationSystemName){
		HashMap<String, StationLibraryRecord> city2rec = mIndex.get(lineSystemName);
		if(city2rec!=null){
			return city2rec.get(stationSystemName);
		}
		return null;
	}
	
	public static StationLibrary load(File pmzFile){
		try {
			InputStream strm;
			File gpsFile = new File(pmzFile.getAbsolutePath().replace(".pmz", ".gps"));
			if(gpsFile.exists()){
				strm = new FileInputStream(gpsFile);
			}else if (mContext!=null){
				String assetName = pmzFile.getName().replace(".pmz", ".gps").toLowerCase();
				strm = mContext.getAssets().open(assetName);
			}else{
				return null;
			}
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),';');
			HashMap<String, HashMap<String,StationLibraryRecord>> index = new HashMap<String, HashMap<String,StationLibraryRecord>>();
			reader.next(); // skip header
			while(reader.next()){
				StationLibraryRecord r = new StationLibraryRecord();
				r.LineSystemName = reader.getString(1);
				r.StationSystemName = reader.getString(2);
				if(reader.getCount()>=5){
					r.Location = new ModelLocation(reader.getFloat(3), reader.getFloat(4) );
				}
				HashMap<String, StationLibraryRecord> city2rec = index.get(r.LineSystemName);
				if(city2rec==null){
					city2rec = new HashMap<String, StationLibraryRecord>();
					index.put(r.LineSystemName, city2rec);
				}
				city2rec.put(r.StationSystemName, r);
			}
			return new StationLibrary(index);
		} catch (Throwable e) {
		}
		return null;
	}
	
	private final HashMap<String, HashMap<String,StationLibraryRecord>> mIndex;
	private static Context mContext = null;
	
	public static void setContext(Context context)
	{
		mContext = context;
	}	
}
