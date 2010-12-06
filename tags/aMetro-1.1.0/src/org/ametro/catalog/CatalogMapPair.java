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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class CatalogMapPair {

	public static class CatalogMapPairCityComparator implements Comparator<CatalogMapPair>
	{
		private String mCode;
		
		public CatalogMapPairCityComparator(String code){
			mCode = code;
		}

		public int compare(CatalogMapPair left, CatalogMapPair right) {
			return left.getCity(mCode).compareTo(right.getCity(mCode));
		}
		
	}
		
	public static class CatalogMapPairCountryComparator implements Comparator<CatalogMapPair>
	{
		private String mCode;
		
		public CatalogMapPairCountryComparator(String code){
			mCode = code;
		}

		public int compare(CatalogMapPair left, CatalogMapPair right) {
			int res = left.getCountry(mCode).compareTo(right.getCountry(mCode));
			if(res == 0){
				return left.getCity(mCode).compareTo(right.getCity(mCode));
			}
			return res;
		}
		
	}	
	
	public final static int PREFFERED_LOCAL = 1;
	public final static int PREFFERED_REMOTE = 2;
	
	public static final int DIFF_MODE_CROSS = 0;
	public static final int DIFF_MODE_LOCAL = 1;
	public static final int DIFF_MODE_REMOTE = 2;
	
	
	/*package*/ CatalogMap mLocal;
	/*package*/ CatalogMap mRemote;
	/*package*/ int mPreffered;
	
	public CatalogMap getLocal() {
		return mLocal;
	}
	
	public CatalogMap getRemote() {
		return mRemote;
	}

	public CatalogMapPair(CatalogMap mLocal, CatalogMap mRemote, int preffered) {
		super();
		this.mLocal = mLocal;
		this.mRemote = mRemote;
		this.mPreffered = preffered;
	}
	
	public boolean isEquals(){
		return mLocal!=null && mRemote!=null && mLocal.completeEqual(mRemote);
	}
	
	public long getTransports() {
		return preffered().getTransports();
	}
	
	public String getCity(String code) {
		return preffered().getCity(code);
	}
	
	public String getCountry(String code) {
		return preffered().getCountry(code);
	}
	
	public String getDescription(String code) {
		return preffered().getDescription(code);
	}

	private CatalogMap preffered(){
		return (mPreffered == PREFFERED_LOCAL) ? 
				(mLocal!=null ? mLocal : mRemote) : 
				(mRemote!=null ? mRemote: mLocal);
	}

	public String getUrl() {
		return preffered().getUrl();
	}

	public String getSystemName() {
		return preffered().getSystemName();
	}

	public boolean isLocalAvailable() {
		return mLocal != null && !mLocal.isCorrupted() && mLocal.isSupported();
	}
	
	public boolean isRemoteAvailable() {
		return mRemote != null && !mRemote.isCorrupted() && mRemote.isSupported();
	}
	
	public String getLocalUrl(){
		return mLocal != null ? mLocal.getUrl() : null;
	}
	
	public String getRemoteUrl(){
		return mRemote != null ? mRemote.getUrl() : null;
	}

	public boolean isUpdateAvailable() {
		if(mLocal==null){
			if(mRemote.isSupported() && !mRemote.isCorrupted()){
				return true;
			}
		}else{
			if(mRemote!=null && !mRemote.isCorrupted() && mRemote.isSupported()){
				return mLocal.getTimestamp() < mRemote.getTimestamp();
			}
		}
		return false;
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

	private static ArrayList<CatalogMapPair> diffLocal(Catalog localCatalog, Catalog remoteCatalog) {
		final ArrayList<CatalogMapPair> diff = new ArrayList<CatalogMapPair>();
		for(CatalogMap localMap : localCatalog.getMaps()){
			final CatalogMap remoteMap = remoteCatalog==null ? null : remoteCatalog.getMap(localMap.getSystemName());
			diff.add(new CatalogMapPair(localMap, remoteMap, CatalogMapPair.PREFFERED_LOCAL));
		}
		return diff;
	}

	public static ArrayList<CatalogMapPair> diff(Catalog local, Catalog remote, int mode) {
		switch(mode){
		case DIFF_MODE_LOCAL: return diffLocal(local, remote);
		case DIFF_MODE_REMOTE: return diffRemote(local, remote);
		case DIFF_MODE_CROSS: return diffCross(local, remote);
		}
		throw new RuntimeException("Unsupported DIFF mode");
	}

	public long getSize() {
		return preffered().getSize();
	}

	public String getCountryISO() {
		return preffered().getCountryISO();
	}
	
}
