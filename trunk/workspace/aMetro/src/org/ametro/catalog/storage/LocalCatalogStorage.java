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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;

public class LocalCatalogStorage {

	public final static int FILE_TYPE_AMETRO = 1;
	public final static int FILE_TYPE_PMETRO = 2;

	public final static String AMETRO_EXTENSION = ".ametro";
	public final static String PMETRO_EXTENSION = ".pmz";
	
	private static final String UNKNOWN_EN = "Unknown";
	private static final String UNKNOWN_RU = "Неизвестно";

	public static Catalog loadCatalog(File url, File path, boolean refresh, int fileTypes)
	{
		Catalog cat = null;
		if(!refresh && url.exists()){
			cat = loadCatalog(url);			
		}
		if(cat==null || isDerpecated(cat)){
			cat = scanCatalog(path, fileTypes);
			if(cat!=null){
				saveCatalog(url, cat);
			}
		}
		return cat;
		
	}
	
	public static boolean isDerpecated(Catalog catalog){
		return new File(catalog.getBaseUrl()).lastModified() > catalog.getTimestamp();
	}
	
	public static Catalog loadCatalog(File url){
		BufferedInputStream strm = null;
		try{
			strm = new BufferedInputStream(new FileInputStream(url));
			Catalog catalog = CatalogDeserializer.deserializeCatalog(strm);
			return catalog;
		}catch(Exception ex){
			return null;
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}
	}
	
	public static Catalog scanCatalog(File baseUrl, int fileTypes){
		ArrayList<CatalogMap> maps = new ArrayList<CatalogMap>();
		if(baseUrl.exists() && baseUrl.isDirectory() ){
			for(File file: baseUrl.listFiles()){
				final String fileName = file.getName().toLowerCase();
				if( ((fileTypes & FILE_TYPE_PMETRO)!=0 && fileName.endsWith(PMETRO_EXTENSION))||
					((fileTypes & FILE_TYPE_AMETRO)!=0 && fileName.endsWith(AMETRO_EXTENSION))){

					Model model = ModelBuilder.loadModelDescription(file.getAbsolutePath());
					if(model!=null){
				    	maps.add(extractCatalogMap(file, fileName, model));
					}else{
						maps.add(makeBadCatalogMap(file, fileName));
						
					}
				}
			}
		}
		return new Catalog(baseUrl.lastModified(), baseUrl.getAbsolutePath().toLowerCase(), maps);
	}

	private static CatalogMap makeBadCatalogMap(File file, final String fileName) {
		
		final String suggestedMapName = fileName.substring(0, fileName.indexOf('.'));
		
		final String[] locales = new String[]{"en","ru"};
		final String[] country = new String[]{UNKNOWN_EN,UNKNOWN_RU};
		final String[] city = new String[]{suggestedMapName,suggestedMapName};
		final String[] description = new String[]{"",""};
		
		String systemName = fileName;
		if(fileName.endsWith(PMETRO_EXTENSION)){
			systemName += AMETRO_EXTENSION;
		}
		
		CatalogMap map = new CatalogMap(
				 systemName,
				 fileName,
				 0,
				 0,
				 Model.VERSION,
				 0,
				 Model.COMPATIBILITY_VERSION,
				 locales,
				 country,
				 city,
				 description,
				 true
				 );
		return map;
	}
	
	private static CatalogMap extractCatalogMap(File file, final String fileName, Model model) {
		final String[] locales = model.locales;
		final int len = locales.length;
		final int countryId = model.countryName;
		final int cityId = model.cityName;
		final String[][] texts = model.localeTexts;
		
		final TreeSet<ModelDescription> modelLocales = new TreeSet<ModelDescription>();
		
		for(int i=0; i<len;i++){
			modelLocales.add( new ModelDescription(locales[i], texts[i][cityId], texts[i][countryId], "Not supported yet.") );
		}

		int index = 0;
		final String[] country = new String[len];
		final String[] city = new String[len];
		final String[] description = new String[len];
		for(ModelDescription m : modelLocales){
			locales[index] = m.locale;
			city[index] = m.city;
			country[index] = m.country;
			description[index] = m.description;
			index++;
		}
		
		String systemName = fileName;
		if(fileName.endsWith(PMETRO_EXTENSION)){
			systemName += AMETRO_EXTENSION;
		}
		
		CatalogMap map = new CatalogMap(
				 systemName,
				 fileName,
				 model.timestamp,
				 model.transportTypes,
				 Model.VERSION,
				 file.length(),
				 Model.COMPATIBILITY_VERSION,
				 locales,
				 country,
				 city,
				 description,
				 false
				 );
		return map;
	}
	
	public static void saveCatalog(File url, Catalog catalog){
		BufferedOutputStream strm = null;
		try{
			strm = new BufferedOutputStream(new FileOutputStream(url));
			CatalogSerializer.serializeCatalog(catalog, strm);
		}catch(Exception ex){
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}
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
}
