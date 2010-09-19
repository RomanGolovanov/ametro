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

import java.util.HashMap;

import org.ametro.util.StringUtil;

public class CatalogMap {

	/*package*/ String mSystemName;
	
	/*package*/ String mUrl;
	
	/*package*/ long mTimestamp;
	/*package*/ long mFileTimestamp;
	/*package*/ long mTransports;
	/*package*/ long mVersion;

	/*package*/ long mSize;
	/*package*/ String mMinVersion;
	
	/*package*/ String[] mLocales;
	
	/*package*/ String mCountryISO;
	
	/*package*/ String[] mCity;
	/*package*/ String[] mCountry;
	/*package*/ String[] mDescription;
	/*package*/ String[] mChangeLog;
	
	/*package*/ boolean mCorrupted;

	/*package*/ Catalog mOwner;
	
	public CatalogMap(Catalog owner, String systemName, String url, long timestamp, long fileTimestamp, long transports, long version,
			long size, String minVersion,
			String[] locales, String[] country, String countryISO,
			String[] city, String[] description, String[] changeLog, 
			boolean corrupted) {
		this.mOwner = owner;
		this.mSystemName = systemName;
		this.mUrl = url;
		this.mTimestamp = timestamp;
		this.mFileTimestamp = fileTimestamp;
		this.mTransports = transports;
		this.mVersion = version;
		this.mSize = size;
		this.mMinVersion = minVersion;
		this.mLocales = locales;
		this.mCountry = country;
		this.mCountryISO = countryISO;
		this.mCity = city;
		this.mDescription = description;
		this.mCorrupted = corrupted;
		this.mChangeLog = changeLog;
	}

	public String getSystemName() {
		return mSystemName;
	}

	public String getCountryISO(){
		return mCountryISO;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public String getAbsoluteUrl() {
		return mOwner.getBaseUrl() + "/" + mUrl;
	}
	
	public long getTimestamp() {
		return mTimestamp;
	}
	
	public long getFileTimestamp() {
		return mFileTimestamp;
	}
	
	public long getTransports() {
		return mTransports;
	}
	
	public long getVersion() {
		return mVersion;
	}
	
	public String[] getLocales() {
		return mLocales;
	}
	
	public String getCity(String code) {
		return mCity[getLocale(code)];
	}
	
	public String getCountry(String code) {
		return mCountry[getLocale(code)];
	}
	
	public String getDescription(String code) {
		return mDescription[getLocale(code)];
	}

	public String getChangeLog(String code) {
		return mChangeLog[getLocale(code)];
	}
	
	public long getSize() {
		return mSize;
	}
	
	public String getMinVersion() {
		return mMinVersion;
	}

	public boolean completeEqual(CatalogMap other) {
		return locationEqual(other) 
		&& mTimestamp == other.mTimestamp;
	}

	public boolean locationEqual(CatalogMap other) {
		return mCountry[0].equals(other.mCountry[0])
		&& mCity[0].equals(other.mCity[0]);
	}	
	
	private HashMap<String, Integer> mLocaleIndex;
	
	private int getLocale(String code){
		if(mLocaleIndex==null){
			HashMap<String, Integer> idx = new HashMap<String, Integer>();
			final String[] locales = mLocales;
			final int len = locales.length;
			for(int i=0;i<len;i++){
				idx.put(locales[i], i);
			}
			mLocaleIndex = idx;
		}
		Integer localeId = mLocaleIndex.get(code);
		return localeId != null ? localeId : 0;
	}
	
	public String toString() {
		return "[NAME:" + mSystemName + ";TRAN:" + mTransports + ";VER:" + mVersion + ";COUNTRY:" + StringUtil.join(mCountry,",") + ";CITY:" + StringUtil.join(mCity,",") + ";LOCALES=" + StringUtil.join(mLocales,",") + ";URL=" + mUrl  + "]";
	}

	public boolean isCorrupted() {
		return mCorrupted;
	}

	public boolean isSupported() {
		return true;
	}

	public boolean isNotSupported() {
		return false;
	}

	public Catalog getOwner() {
		return mOwner;
	}

	public boolean isAvailable() {
		return !(isCorrupted() || !isSupported());
	}


}
