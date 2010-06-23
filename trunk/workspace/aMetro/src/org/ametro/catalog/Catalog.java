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
import java.util.HashSet;

public class Catalog {

	public static final int MODE_CROSS_JOIN = 0;
	public static final int MODE_LEFT_JOIN = 1;
	public static final int MODE_RIGHT_JOIN = 2;
	
	/*package*/ long mTimestamp;
	/*package*/ String mBaseUrl;
	/*package*/ ArrayList<CatalogMap> mMaps;

	/*package*/ void setTimestamp(long timestamp){
		mTimestamp = timestamp;
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
	
	public long getTimestamp() {
		return mTimestamp;
	}
	
	public String getBaseUrl() {
		return mBaseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		mBaseUrl = baseUrl;
	}
	
	/*package*/ void setMaps(ArrayList<CatalogMap> maps){
		mMaps = maps;
	}
	
	public ArrayList<CatalogMap> getMaps() {
		return mMaps;
	}

	public Catalog(long timestamp, String baseUrl, ArrayList<CatalogMap> maps) {
		mTimestamp = timestamp;
		mBaseUrl = baseUrl;
		mMaps = maps;
	}
	
	public String toString() {
		return "[TIME:" + getTimestamp() + ";URL:" + getBaseUrl() + ";COUNT:" + (getMaps()!=null ? getMaps().size() : "null") + "]";
	}
	
	public static ArrayList<CatalogMapDifference> diff(Catalog localCatalog, Catalog remoteCatalog, int mode)
	{
		final int preffered = (mode == MODE_RIGHT_JOIN) ? CatalogMapDifference.PREFFERED_REMOTE : CatalogMapDifference.PREFFERED_LOCAL;
		final ArrayList<CatalogMapDifference> diff = new ArrayList<CatalogMapDifference>();
		HashSet<String> systemMapNames = new HashSet<String>(  );
		if(localCatalog!=null && mode != MODE_RIGHT_JOIN){
			for(CatalogMap map : localCatalog.getMaps()){
				systemMapNames.add(map.getSystemName());
			}
		}
		if(remoteCatalog!=null && mode != MODE_LEFT_JOIN){
			for(CatalogMap map : remoteCatalog.getMaps()){
				systemMapNames.add(map.getSystemName());
			}
		}
		for(String systemName : systemMapNames){
			final CatalogMap local = localCatalog!=null ? localCatalog.getMap(systemName) : null;
			final CatalogMap remote = remoteCatalog!=null ? remoteCatalog.getMap(systemName) : null;
			diff.add(new CatalogMapDifference(local, remote, preffered));
		}
		return diff;
	}
	
	public static ArrayList<CatalogMapDifference> diffImport(Catalog importCatalog, Catalog localCatalog)
	{
		final ArrayList<CatalogMapDifference> diff = new ArrayList<CatalogMapDifference>();
		for(CatalogMap map : importCatalog.getMaps()){
			final CatalogMap local = localCatalog.getMap(map.getSystemName());
			diff.add(new CatalogMapDifference(local, map, CatalogMapDifference.PREFFERED_REMOTE));
		}
		return diff;
	}
	
//	public static void updateDiffLocal(ArrayList<CatalogMapDifference> diff, Catalog localCatalog){
//		for(CatalogMapDifference d : diff){
//			final String systemName = d.getSystemName();
//			final CatalogMap local = localCatalog!=null ? localCatalog.getMap(systemName) : null;
//		}
//	}

}
