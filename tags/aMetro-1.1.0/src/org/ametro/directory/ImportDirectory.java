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

package org.ametro.directory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.ametro.app.Constants;
import org.ametro.util.csv.CsvReader;

import android.content.Context;
import android.util.Log;

public class ImportDirectory {
	
	public static class Entity
	{
		private String mFileName;
		private int mCityId;
		private String mCharset;
		
		public String getFileName() {
			return mFileName;
		}
		
		public int getCityId() {
			return mCityId;
		}
		
		public String getCharSet(){
			return mCharset;
		}
		
		public Entity(String mFileName, int mCity, String mCharSet) {
			super();
			this.mFileName = mFileName;
			this.mCityId = mCity;
			this.mCharset = mCharSet;
		}

		public String getMapSystemName() {
			return mFileName.toLowerCase() + ".pmz.ametro";
		}
	}
	
	public ImportDirectory(Context context) {
		mIndex = new HashMap<String, Entity>();
		try {
			InputStream strm = context.getAssets().open("imports.dict");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),',');
			if(reader.next()){
				while(reader.next()){
					String fileName = reader.getString(0).toLowerCase();
					int cityId = reader.getInt(1);
					String charSet = reader.getString(2);
					Entity entity = new Entity(fileName, cityId, charSet);
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

	public Entity getByCityId(int cityId) {
		for(String key: mIndex.keySet()){
			Entity entity = mIndex.get(key);
			if(entity.getCityId() == cityId){
				return entity;
			}
		}
		return null;
	}
		

}
