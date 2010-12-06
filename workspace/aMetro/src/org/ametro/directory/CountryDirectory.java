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
import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.app.Constants;
import org.ametro.util.csv.CsvReader;

import android.content.Context;
import android.util.Log;

public class CountryDirectory {
	
	public static final float DEFAULT_WIDTH = 30;
	public static final float DEFAULT_HEIGHT = 30;
	
	public static class Entity
	{
		private int mId;
		private String mISO2;
		private String mISO3;
		
		private String[] mNames;
		private String[] mLocales;
		
		private HashMap<String, String> mLocaleToName;
		private String mDefaultName;

		public int getId() {
			return mId;
		}

		public String getISO2() {
			return mISO2;
		}

		public String getISO3() {
			return mISO3;
		}
		
		public String getName(String code) {
			return mLocaleToName.containsKey(code) ? mLocaleToName.get(code) : mDefaultName;
		}

		public String getDefaultName() {
			return mDefaultName;
		}
		
		public String[] getNames() {
			return mNames;
		}
		
		public String[] getLocales() {
			return mLocales;
		}

		public Entity(int id, String iso2, String iso3, String[] names, String[] locales) {
			super();
			this.mId = id;
			this.mISO2 = iso2;
			this.mISO3 = iso3;
			this.mNames = names;
			this.mLocales = locales;
			this.mLocaleToName = new HashMap<String, String>();
			fillNames(names, locales);
		}

		private void fillNames(String[] names, String[] locales) {
			final int len = locales.length;
			final int namesLen = names.length;
			this.mDefaultName = null;
			for(int i = 0; i < len; i++){
				String loc = locales[i].toLowerCase();
				if(i<namesLen){
					String name = names[i]!=null ? names[i].trim() : null;
					mLocaleToName.put(loc,name!=null ? name : null);
					if(mDefaultName==null){
						mDefaultName = name;
					}
				}else{
					mLocaleToName.put(loc, null);
				}
			}
		}
	}
	
	public CountryDirectory(Context context) {
		mIndex = new HashMap<Integer, Entity>();
		mNameIndex = new HashMap<String, Entity>();
		try {
			InputStream strm = context.getAssets().open("countries.dict");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),',');
			if(reader.next()){
				String[] locales = getLocales(reader);
				while(reader.next()){
					int id = reader.getInt(0);
					String iso2 = reader.getString(1);
					String iso3 = reader.getString(2);
					String[] names = getNames(reader, locales.length);
					Entity entity = new Entity(id, iso2, iso3, names, locales);
					mIndex.put(id, entity);
					for(String name : names){
						mNameIndex.put(name, entity);						
					}
				}
			}
		} catch (Throwable e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN,"Failed country directory creation: " + e.toString());
			}
		}
	}

	private String[] getLocales(CsvReader reader) {
		ArrayList<String> locales= new ArrayList<String>();
		locales = new ArrayList<String>();
		int start = 2;
		int len = reader.getCount();
		for(int i = start; i<len; i++){
			locales.add(reader.getString(i));
		}
		return (String[]) locales.toArray(new String[locales.size()]);
	}

	private String[] getNames(CsvReader reader, int count) {
		ArrayList<String> names= new ArrayList<String>();
		names = new ArrayList<String>();
		int start = 2;
		int len = reader.getCount();
		for(int i = start; i<len; i++){
			names.add(reader.getString(i));
		}
		for(int i = len; i < count; i++){
			names.add(null);
		}
		return (String[]) names.toArray(new String[names.size()]);
	}
	
	public Entity get(int id){
		return mIndex.get(id);
	}
	
	public Entity getByName(String name){
		return mNameIndex.get(name);
	}
	
	private final HashMap<Integer, Entity> mIndex;
	private final HashMap<String, Entity> mNameIndex;

}
