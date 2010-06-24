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
import java.util.List;

public class Catalog {

	public static final int DIFF_MODE_CROSS = 0;
	public static final int DIFF_MODE_LEFT = 1;
	public static final int DIFF_MODE_RIGHT = 2;
	
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
	
	private static ArrayList<CatalogMapPair> diffCross(Catalog localCatalog, Catalog remoteCatalog)
	{
		final int preffered = CatalogMapPair.PREFFERED_LOCAL;
		final ArrayList<CatalogMapPair> diff = new ArrayList<CatalogMapPair>();
		HashSet<String> systemMapNames = new HashSet<String>(  );
		if(localCatalog!=null){
			for(CatalogMap map : localCatalog.getMaps()){
				systemMapNames.add(map.getSystemName());
			}
		}
		if(remoteCatalog!=null){
			for(CatalogMap map : remoteCatalog.getMaps()){
				systemMapNames.add(map.getSystemName());
			}
		}
		for(String systemName : systemMapNames){
			final CatalogMap localMap = localCatalog!=null ? localCatalog.getMap(systemName) : null;
			final CatalogMap remoteMap = remoteCatalog!=null ? remoteCatalog.getMap(systemName) : null;
			diff.add(new CatalogMapPair(localMap, remoteMap, preffered));
		}
		return diff;
	}
	
	private static ArrayList<CatalogMapPair> diffRemote(Catalog localCatalog, Catalog importCatalog)
	{
		final ArrayList<CatalogMapPair> diff = new ArrayList<CatalogMapPair>();
		for(CatalogMap remote : importCatalog.getMaps()){
			final CatalogMap local = localCatalog==null ? null : localCatalog.getMap(remote.getSystemName());
			diff.add(new CatalogMapPair(local, remote, CatalogMapPair.PREFFERED_REMOTE));
		}
		return diff;
	}

	private static List<CatalogMapPair> diffLocal(Catalog localCatalog, Catalog remoteCatalog) {
		final ArrayList<CatalogMapPair> diff = new ArrayList<CatalogMapPair>();
		for(CatalogMap localMap : localCatalog.getMaps()){
			final CatalogMap remoteMap = remoteCatalog==null ? null : remoteCatalog.getMap(localMap.getSystemName());
			diff.add(new CatalogMapPair(localMap, remoteMap, CatalogMapPair.PREFFERED_LOCAL));
		}
		return diff;
	}

	public static List<CatalogMapPair> diff(Catalog local, Catalog remote, int mode) {
		switch(mode){
		case DIFF_MODE_LEFT: return diffLocal(local, remote);
		case DIFF_MODE_RIGHT: return diffRemote(local, remote);
		case DIFF_MODE_CROSS: return diffCross(local, remote);
		}
		throw new RuntimeException("Unsupported DIFF mode");
	}

}
