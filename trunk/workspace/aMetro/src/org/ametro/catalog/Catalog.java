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
package org.ametro.catalog;

import java.util.ArrayList;
import java.util.HashMap;

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
			for(CatalogMap map : mMaps){
				index.put(map.getSystemName(), map);
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
		if(mMapIndex.containsKey(systemName)){
			mMapIndex.remove(map.getSystemName());
			mMaps.remove(map);
		}
		mMapIndex.put(systemName, map);
		mMaps.add(map);
		mTimestamp = System.currentTimeMillis();
	}
}
