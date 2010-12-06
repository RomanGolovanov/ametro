/*
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
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

import org.ametro.app.ApplicationEx;
import org.ametro.model.TransportType;

import android.content.Context;

public class CatalogMapSuggestion {

	private final File mFile;
	private final String mCity;
	private final String mCountry;
	private final String mCountryISO;
	private final long mTransports;
	
	private final ImportDirectory.Entity mImportEntity;
	private final CityDirectory.Entity mCityEntity;
	private final CountryDirectory.Entity mCountryEntity;
	
	private CatalogMapSuggestion(File file, String city, String country, String countryIso, long transports, ImportDirectory.Entity importEntity, CityDirectory.Entity cityEntity, CountryDirectory.Entity countryEntity) {
		super();
		this.mFile = file;
		this.mCity = city;
		this.mCountry = country;
		this.mCountryISO = countryIso;
		this.mTransports = transports;
		this.mImportEntity = importEntity;
		this.mCityEntity = cityEntity;
		this.mCountryEntity = countryEntity;
	}

	public ImportDirectory.Entity getImportEntity() {
		return mImportEntity;
	}

	public CityDirectory.Entity getCityEntity() {
		return mCityEntity;
	}

	public CountryDirectory.Entity getCountryEntity() {
		return mCountryEntity;
	}
	
	public File getFile() {
		return mFile;
	}

	public String getCity() {
		return mCity;
	}

	public String getCountry() {
		return mCountry;
	}

	public static CatalogMapSuggestion create(Context context, File file, String city, String country){
		return create(context, file, city, country, null, TransportType.UNKNOWN_ID);
	}

	public String getCity(String languageCode) {
		return mCityEntity!=null ? mCityEntity.getName(languageCode) : mCity;
	}

	public String getCountry(String languageCode) {
		return mCountryEntity!=null ? mCountryEntity.getName(languageCode) : mCountry;
	}

	public String getCountryISO() {
		return mCountryEntity!=null ? mCountryEntity.getISO2() : mCountryISO;
	}

	public long getTransports() {
		return mTransports;
	}

	public static CatalogMapSuggestion create(Context context, File file, String city, String country, String countryIso, long transports){
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
				cityEntity = app.getCityDirectory().getByName( city );
				if(cityEntity!=null){
					countryEntity = app.getCountryDirectory().get(cityEntity.getCountryId());
				}else{
					countryEntity = app.getCountryDirectory().getByName( country );
				}
			}
			return new CatalogMapSuggestion(file, city, country, countryIso, transports, importEntity, cityEntity, countryEntity);
		}	
}
