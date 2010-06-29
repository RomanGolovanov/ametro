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
package org.ametro.directory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.ametro.model.ext.ModelLocation;
import org.ametro.util.csv.CsvReader;

import android.content.Context;

public  class CountryLibrary
{
	
	public CountryLibrary(Context context) {
		try {
			InputStream strm = context.getAssets().open("cities.csv");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),';');
			while(reader.next()){
				String cityNameRu = reader.getString(1);
				String cityNameEn = reader.getString(2);
				String countryNameRu = reader.getString(3);
				String countryNameEn = reader.getString(4);
				ModelLocation location = new ModelLocation(reader.getFloat(5), reader.getFloat(6) );
				CityInfo r = new CityInfo(countryNameRu, cityNameRu, countryNameEn, cityNameEn,location);
				HashMap<String, CityInfo> city2rec = mIndex.get(r.getCountryNameRu());
				if(city2rec==null){
					city2rec = new HashMap<String, CityInfo>();
					mIndex.put(r.getCountryNameRu(), city2rec);
				}
				city2rec.put(r.getCityNameRu(), r);
			}
		} catch (Throwable e) {
		}
	}

	public CityInfo search(String country, String city){
		HashMap<String, CityInfo> city2rec = mIndex.get(country);
		if(city2rec!=null){
			return city2rec.get(city);
		}
		return null;
	}
	
	private final HashMap<String, HashMap<String,CityInfo>> mIndex = new HashMap<String, HashMap<String,CityInfo>>();
	
}
