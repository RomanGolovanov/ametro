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

public class CatalogMapDifference {


	public static int NOT_EXIST = 0;
	public static int DEPRECATED = 1;
	public static int UP_TO_DATE = 2;
	public static int OVERRIDE = 3;
	public static int CORRUPTED = 4;
	
	
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
	
}
