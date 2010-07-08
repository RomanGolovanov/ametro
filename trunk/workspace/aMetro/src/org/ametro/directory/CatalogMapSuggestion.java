/*
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

package org.ametro.directory;

import java.io.File;

import org.ametro.ApplicationEx;

import android.content.Context;

public class CatalogMapSuggestion {

	private final File mFile;
	private final String mCityName;
	private final String mCountryName;
	
	private final ImportDirectory.Entity mImport;
	private final CityDirectory.Entity mCity;
	private final CountryDirectory.Entity mCountry;
	
	private CatalogMapSuggestion(File file, String cityName, String countryName, ImportDirectory.Entity imports, CityDirectory.Entity city, CountryDirectory.Entity country) {
		super();
		this.mFile = file;
		this.mCityName = cityName;
		this.mCountryName = countryName;
		this.mImport = imports;
		this.mCity = city;
		this.mCountry = country;
	}

	public ImportDirectory.Entity getImport() {
		return mImport;
	}

	public CityDirectory.Entity getCity() {
		return mCity;
	}

	public CountryDirectory.Entity getCountry() {
		return mCountry;
	}
	
	public File getFile() {
		return mFile;
	}

	public String getCityName() {
		return mCityName;
	}

	public String getCountryName() {
		return mCountryName;
	}

	public static CatalogMapSuggestion create(Context context, File file, String cityName, String countryName){
		ApplicationEx app = (ApplicationEx)context.getApplicationContext();
		ImportDirectory.Entity importEntity = null;
		CityDirectory.Entity cityEntity = null;
		CountryDirectory.Entity countryEntity = null;
		
		String fileName = file.getName();
		String mapName = fileName.substring(0, fileName.indexOf('.'));
		
		importEntity = app.getImportDirectory().get(mapName);
		if(importEntity!=null){
			cityEntity = app.getCityDirectory().get(importEntity.getCityId());
			if(cityEntity!=null){
				countryEntity = app.getCountryDirectory().get(cityEntity.getCountryId());
			}
		}else{
			cityEntity = app.getCityDirectory().getByName( cityName );
			if(cityEntity!=null){
				countryEntity = app.getCountryDirectory().get(cityEntity.getCountryId());
			}else{
				countryEntity = app.getCountryDirectory().getByName( countryName );
			}
		}
		return new CatalogMapSuggestion(file, cityName, countryName, importEntity, cityEntity, countryEntity);
	}	
	
}
