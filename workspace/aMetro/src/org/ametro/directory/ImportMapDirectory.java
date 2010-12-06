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
import org.ametro.model.TransportType;
import org.ametro.util.csv.CsvReader;

import android.content.Context;
import android.util.Log;

public class ImportMapDirectory {

	public static class ImportMapEntity
	{
		private String mFileName;
		private String mMapFileName;
		private long mTransportType;

		private String[] mNames;
		private String[] mLocales;
		private HashMap<String, String> mLocaleToName;
		private String mDefaultName;

		private boolean mIsMain;
		
		public String getFileName() {
			return mFileName;
		}
		
		public String getMapFileName() {
			return mMapFileName;
		}
		
		public long getTransportType(){
			return mTransportType;
		}
		
		public boolean isMain(){
			return mIsMain;
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
		
		
		public ImportMapEntity(String fileName, String mapFileName, long transportType, String[] names, String[] locales, boolean isMain) {
			super();
			this.mFileName = fileName;
			this.mMapFileName = mapFileName;
			this.mTransportType = transportType;
			this.mNames = names;
			this.mLocales = locales;
			this.mLocaleToName = new HashMap<String, String>();
			this.mIsMain = isMain;
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
	
	public ImportMapDirectory(Context context) {
		mIndex = new HashMap<String, ImportMapEntity>();
		try {
			InputStream strm = context.getAssets().open("maps.dict");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),',');
			if(reader.next()){
				String[] locales = getLocales(reader);
				while(reader.next()){
					String fileName = reader.readString();
					String mapFileName = reader.readString();
					long transports = TransportType.getTransportTypeId( reader.readString() );
					boolean isMain = reader.readBoolean();
					String[] names = getNames(reader, locales.length);
					ImportMapEntity entity = new ImportMapEntity(fileName, mapFileName, transports, names, locales, isMain);
					mIndex.put(getEntityId(entity), entity);
				}
			}
		} catch (Throwable e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN,"Failed import map directory creation: " + e.toString());
			}
		}
	}

	private String[] getLocales(CsvReader reader) {
		ArrayList<String> locales= new ArrayList<String>();
		locales = new ArrayList<String>();
		int start = 4;
		int len = reader.getCount();
		for(int i = start; i<len; i++){
			locales.add(reader.getString(i));
		}
		return (String[]) locales.toArray(new String[locales.size()]);
	}

	private String[] getNames(CsvReader reader, int count) {
		ArrayList<String> names= new ArrayList<String>();
		names = new ArrayList<String>();
		int start = 4;
		int len = reader.getCount();
		for(int i = start; i<len; i++){
			names.add(reader.getString(i));
		}
		for(int i = len; i < count; i++){
			names.add(null);
		}
		return (String[]) names.toArray(new String[names.size()]);
	}	
	
	public ImportMapEntity get(String fileName, String mapFileName){
		return mIndex.get(fileName.toLowerCase() + ":" + mapFileName.toLowerCase());
	}
	
	private static String getEntityId(ImportMapEntity entity){
		return entity.getFileName().toLowerCase()+ ".pmz" + ":" + entity.getMapFileName().toLowerCase() + ".map";
	}
	
	private final HashMap<String, ImportMapEntity> mIndex;
	
}