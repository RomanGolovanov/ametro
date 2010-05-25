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
package org.ametro.catalog.storage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.ametro.catalog.Catalog;

public class OnlineCatalogStorage {

	public final static String DEFAULT_URL = "http://sites.google.com/site/ametrohome/file-cabinet/catalog.xml";
	
	public static Catalog loadCatalog(String url){
		BufferedInputStream strm = null;
		try{
			URL catalogUrl = new URL(url);
			HttpURLConnection  conn = (HttpURLConnection)catalogUrl.openConnection();
			conn.setUseCaches(false);
			conn.setRequestMethod("GET");
			strm = new BufferedInputStream(conn.getInputStream());
			Catalog catalog = CatalogDeserializer.deserializeCatalog(strm);
			catalog.setBaseUrl(url.substring(0, url.lastIndexOf('/')));
			return catalog;
		}catch(Exception ex){
			return null;
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}
	}
	
}
