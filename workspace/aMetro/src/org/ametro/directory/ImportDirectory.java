package org.ametro.directory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.ametro.Constants;
import org.ametro.util.csv.CsvReader;

import android.content.Context;
import android.util.Log;

public class ImportDirectory {
	
	public static class Entity
	{
		private String mFileName;
		private int mCityId;
		
		public String getFileName() {
			return mFileName;
		}
		
		public int getCityId() {
			return mCityId;
		}
		
		public Entity(String mFileName, int mCity) {
			super();
			this.mFileName = mFileName;
			this.mCityId = mCity;
		}
	}
	
	public ImportDirectory(Context context) {
		mIndex = new HashMap<String, Entity>();
		try {
			InputStream strm = context.getAssets().open("imports.dict");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),',');
			if(reader.next()){
				while(reader.next()){
					String fileName = reader.getString(0);
					int cityId = reader.getInt(1);
					Entity entity = new Entity(fileName, cityId);
					mIndex.put(fileName, entity);
				}
			}
		} catch (Throwable e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN,"Failed import directory creation: " + e.toString());
			}
		}
	}

	public Entity get(String fileName){
		return mIndex.get(fileName);
	}
	
	private final HashMap<String, Entity> mIndex;
		

}
