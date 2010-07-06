package org.ametro.directory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.Constants;
import org.ametro.model.ext.ModelLocation;
import org.ametro.util.csv.CsvReader;

import android.content.Context;
import android.util.Log;

public class CityDirectory {
	
	public static final float DEFAULT_WIDTH = 30;
	public static final float DEFAULT_HEIGHT = 30;
	
	public static class Entity
	{
		private int mId;
		private int mCountryId;
		private ModelLocation mLocation;
		private String[] mNames;
		private String[] mLocales;
		
		private HashMap<String, String> mLocaleToName;
		private String mDefaultName;

		public int getId() {
			return mId;
		}

		public int getCountryId() {
			return mCountryId;
		}

		public ModelLocation getLocation() {
			return mLocation;
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
		
		public Entity(int id, int countryId, ModelLocation location, String[] names, String[] locales) {
			super();
			this.mId = id;
			this.mCountryId = countryId;
			this.mLocation = location;
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
	
	public CityDirectory(Context context) {
		mIndex = new HashMap<Integer, Entity>();
		try {
			InputStream strm = context.getAssets().open("cities.dict");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),',');
			if(reader.next()){
				String[] locales = getLocales(reader);
				while(reader.next()){
					int id = reader.readInt();
					int countryId = reader.readInt();
					float latitude = reader.readFloat();
					float longtitude = reader.readFloat();
					Integer width = reader.readNullableInteger();
					Integer height = reader.readNullableInteger();
					ModelLocation location = new ModelLocation(
						latitude, 
						longtitude, 
						height!=null ? height : DEFAULT_HEIGHT, 
						width!=null ? width : DEFAULT_WIDTH 
					);
					String[] names = getNames(reader, locales.length);
					Entity entity = new Entity(id, countryId, location, names, locales);
					mIndex.put(id, entity);
				}
			}
		} catch (Throwable e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN,"Failed city directory creation: " + e.toString());
			}
		}
	}

	private String[] getLocales(CsvReader reader) {
		ArrayList<String> locales= new ArrayList<String>();
		locales = new ArrayList<String>();
		int start = 6;
		int len = reader.getCount();
		for(int i = start; i<len; i++){
			locales.add(reader.getString(i));
		}
		return (String[]) locales.toArray(new String[locales.size()]);
	}

	private String[] getNames(CsvReader reader, int count) {
		ArrayList<String> names= new ArrayList<String>();
		names = new ArrayList<String>();
		int start = 6;
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
	
	private final HashMap<Integer, Entity> mIndex;
		

}
