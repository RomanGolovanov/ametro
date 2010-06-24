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

import java.util.Comparator;

public class CatalogMapPair {

	public static class CatalogMapDifferenceCityNameComparator implements Comparator<CatalogMapPair>
	{
		private String mCode;
		
		public CatalogMapDifferenceCityNameComparator(String code){
			mCode = code;
		}

		public int compare(CatalogMapPair left, CatalogMapPair right) {
			return left.getCity(mCode).compareTo(right.getCity(mCode));
		}
		
	}

	public static final int OFFLINE = 0; 
	public static final int NOT_SUPPORTED = 1;
	public static final int CORRUPTED = 2;
	public static final int UPDATE = 3; 
	public static final int INSTALLED = 4; 
	public static final int IMPORT = 5; 
	public static final int DOWNLOAD = 6; 
	public static final int UPDATE_NOT_SUPPORTED = 7; 
		
	public final static int PREFFERED_LOCAL = 0;
	public final static int PREFFERED_REMOTE = 0;
	
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
	
	public int getState(){
		if(mLocal!=null && mRemote == null){
			return mLocal.isCorruted() ? CORRUPTED : OFFLINE;
		}
		if(mLocal==null && mRemote!=null){
			return mRemote.isCorruted() ? CORRUPTED : DOWNLOAD;
		}
		if(mLocal.isCorruted() && mRemote.isCorruted()){
			return CORRUPTED;
		}
		
		if(mLocal.getTimestamp() < mRemote.getTimestamp()){
			return UPDATE;
		}
		return INSTALLED;
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
		return mLocal != null && !mLocal.isCorruted() && mLocal.isSupported();
	}
	
	public boolean isRemoteAvailable() {
		return mRemote != null && !mRemote.isCorruted() && mRemote.isSupported();
	}
	
	public String getLocalUrl(){
		return mLocal != null ? mLocal.getUrl() : null;
	}
	
	public String getRemoteUrl(){
		return mRemote != null ? mRemote.getUrl() : null;
	}

	public boolean isUpdateAvailable() {
		if(mLocal==null){
			if(mRemote.isSupported() && !mRemote.isCorruted()){
				return true;
			}
		}else{
			if(mRemote!=null && !mRemote.isCorruted() && mRemote.isSupported()){
				return mLocal.getTimestamp() <= mRemote.getTimestamp();
			}
		}
		return false;
	}
	
}
