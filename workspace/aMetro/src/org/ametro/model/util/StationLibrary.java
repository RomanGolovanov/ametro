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
