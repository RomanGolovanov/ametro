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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.catalog.storage.CatalogSerializer;
import org.ametro.directory.CatalogMapSuggestion;
import org.ametro.directory.CityDirectory;
import org.ametro.directory.CountryDirectory;
import org.ametro.model.Model;
import org.ametro.model.TransportType;
import org.ametro.util.StringUtil;

public class Catalog {

	/*package*/ long mTimestamp;
	/*package*/ String mBaseUrl;
	/*package*/ ArrayList<CatalogMap> mMaps;
	/*package*/ boolean mIsCorrupted;

	public Catalog(){
		mIsCorrupted = false;
	}

	public boolean isCorrupted(){
		return mIsCorrupted;
	}
	
	public void setCorrupted(boolean isCorrupted){
		mIsCorrupted = isCorrupted;
	}

	public void setTimestamp(long timestamp){
		mTimestamp = timestamp;
	}

	public long getTimestamp() {
		return mTimestamp;
	}
	
	public String getBaseUrl() {
		return mBaseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		mBaseUrl = baseUrl;
	}
	
	public void setMaps(ArrayList<CatalogMap> maps){
		mMaps = maps;
	}
	
	public ArrayList<CatalogMap> getMaps() {
		return mMaps;
	}

	public Catalog(long timestamp, String baseUrl, ArrayList<CatalogMap> maps) {
		mTimestamp = timestamp;
		mBaseUrl = baseUrl;
		mMaps = maps;
		mIsCorrupted = false;
	}
	
	public String toString() {
		return "[TIME:" + getTimestamp() + ";URL:" + getBaseUrl() + ";COUNT:" + (getMaps()!=null ? getMaps().size() : "null") + "]";
	}
	
	/* VOLATILE FIELDS */
	private HashMap<String, CatalogMap> mMapIndex;
	
	public CatalogMap getMap(String systemName){
		if(mMapIndex == null){
			final HashMap<String, CatalogMap> index = new HashMap<String, CatalogMap>();
			if(mMaps!=null){
				for(CatalogMap map : mMaps){
					index.put(map.getSystemName(), map);
				}
			}
			mMapIndex = index;
		}
		return mMapIndex.get(systemName);
	}

	public static boolean equals(Catalog left, Catalog right) {
		return left!=null && right!=null && left.equals(right);
	}
	
	public boolean equals(Object o) {
		Catalog obj = (Catalog)o;
		return mTimestamp == obj.mTimestamp && mBaseUrl.equals(obj.mBaseUrl) && mMaps.size() == obj.mMaps.size();
	}

	public Catalog deleteMap(CatalogMap map) {
		mMapIndex.remove(map.getSystemName());
		mMaps.remove(map);
		mTimestamp = System.currentTimeMillis();
		return this;
	}

	public void appendMap(CatalogMap map) {
		final String systemName = map.getSystemName(); 
		if(getMap(systemName)!=null){
			CatalogMap old = mMapIndex.get(systemName);
			mMapIndex.remove(systemName);
			mMaps.remove(old);
		}
		mMapIndex.put(systemName, map);
		mMaps.add(map);
		mTimestamp = System.currentTimeMillis();
	}

	public static CatalogMap makeBadCatalogMap(Catalog catalog, File file, final String fileName) {
		
		String suggestedMapName = fileName.substring(0, fileName.indexOf('.'));

		final int len = 2;
		final String[] locales = new String[]{"en","ru"};
		final String[] country = new String[]{UNKNOWN_EN,UNKNOWN_RU};
		final String[] city = new String[]{suggestedMapName,suggestedMapName};
		final String[] description = new String[]{"",""};
		final String[] changeLog = new String[]{"",""};
		String iso2 = null;

		
		String systemName = fileName;
		if(fileName.endsWith(Constants.PMETRO_EXTENSION)){
			systemName += Constants.AMETRO_EXTENSION;
		}

		// try to suggest map city/country from directories
		CatalogMapSuggestion suggestion = CatalogMapSuggestion.create(ApplicationEx.getInstance(), file, null, null);
		CityDirectory.Entity cityEntity = suggestion.getCityEntity();
		CountryDirectory.Entity countryEntity = suggestion.getCountryEntity();

		if(cityEntity!=null){
			for(int i=0;i<len;i++){
				city[i] = cityEntity.getName(locales[i]);
			}
		}
		if(countryEntity!=null){
			for(int i=0;i<len;i++){
				country[i] = countryEntity.getName(locales[i]);
			}
			iso2 = countryEntity.getISO2();
		}
		
		CatalogMap map = new CatalogMap(
				 catalog,
				 systemName,
				 fileName,
				 file.lastModified(),
				 file.lastModified(),
				 TransportType.UNKNOWN_ID,
				 Constants.MODEL_VERSION,
				 file.length(),
				 Constants.MODEL_COMPATIBILITY_VERSION,
				 locales,
				 country,
				 iso2,
				 city,
				 description,
				 changeLog,
				 true
				 );
		return map;
	}
	
	public static CatalogMap extractCatalogMap(Catalog catalog, File file, final String fileName, Model model) {
		final String[] locales = model.locales;
		final int len = locales.length;
		final int countryId = model.countryName;
		final int cityId = model.cityName;
		final String[][] texts = model.localeTexts;
		final String iso = model.countryIso;
		final String[] country = new String[len];
		final String[] city = new String[len];
		final String[] description = new String[len];
		final String[] changeLog = new String[len];
		
		final TreeSet<ModelDescription> modelLocales = new TreeSet<ModelDescription>();
		
		for(int i=0; i<len;i++){
			modelLocales.add( new ModelDescription(locales[i], texts[i][cityId], texts[i][countryId], StringUtil.join(model.getAuthors(locales[i]), "\n") ) );
		}

		int index = 0;
		for(ModelDescription m : modelLocales){
			locales[index] = m.locale;
			city[index] = m.city;
			country[index] = m.country;
			description[index] = m.description;
			changeLog[index] = "";
			index++;
		}
		
		String systemName = fileName;
		if(fileName.endsWith(Constants.PMETRO_EXTENSION)){
			systemName += Constants.AMETRO_EXTENSION;
		}
	
		CatalogMap map = new CatalogMap(
				 catalog,
				 systemName,
				 fileName,
				 model.timestamp,
				 file.lastModified(),
				 model.transportTypes,
				 Constants.MODEL_VERSION,
				 file.length(),
				 Constants.MODEL_COMPATIBILITY_VERSION,
				 locales,
				 country,
				 iso,
				 city,
				 description,
				 changeLog,
				 false
				 );
		return map;
	}
	
	private static class ModelDescription implements Comparable<ModelDescription>
	{
		String locale;
		String city;
		String country;
		String description;
		
		public int compareTo(ModelDescription another) {
			return locale.compareTo(another.locale);
		}

		public ModelDescription(String locale, String city, String country, String description) {
			super();
			this.locale = locale;
			this.city = city;
			this.country = country;
			this.description = description;
		}
	}
	
	private static final String UNKNOWN_EN = "Unknown";
	private static final String UNKNOWN_RU = "Неизвестно";

	public static void save(Catalog catalog, File storage) throws IOException {
		BufferedOutputStream strm = null;
		try{
			strm = new BufferedOutputStream(new FileOutputStream(storage));
			CatalogSerializer.serializeCatalog(catalog, strm);
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}
	}

	public void save(File storage) throws IOException {
		Catalog.save(this, storage);
	}

	public int getSize() {
		return mMaps!=null ? mMaps.size() : 0;
	}	
}
