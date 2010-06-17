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

public class CatalogMapDifference {

	public static class CatalogMapDifferenceCityNameComparator implements Comparator<CatalogMapDifference>
	{
		private String mCode;
		
		public CatalogMapDifferenceCityNameComparator(String code){
			mCode = code;
		}

		public int compare(CatalogMapDifference left, CatalogMapDifference right) {
			return left.getCity(mCode).compareTo(right.getCity(mCode));
		}
		
	}

	public final static int OFFLINE = 0;
	public final static int NOT_SUPPORTED = 1;
	public final static int CORRUPTED = 2;
	public final static int DOWNLOAD = 3;
	public final static int UPDATE = 4;
	public final static int INSTALLED = 5;
	
	/*package*/ CatalogMap mLocal;
	/*package*/ CatalogMap mRemote;
	
	public CatalogMap getLocal() {
		return mLocal;
	}
	
	public CatalogMap getRemote() {
		return mRemote;
	}

	public CatalogMapDifference(CatalogMap mLocal, CatalogMap mRemote) {
		super();
		this.mLocal = mLocal;
		this.mRemote = mRemote;
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
		return mLocal!=null ? mLocal : mRemote;
	}

	public String getUrl() {
		return preffered().getUrl();
	}

	public String getSystemName() {
		return preffered().getSystemName();
	}
	
}
