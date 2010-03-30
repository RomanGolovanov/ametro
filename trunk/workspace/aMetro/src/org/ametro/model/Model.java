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
package org.ametro.model;

import java.io.IOException;
import java.util.Locale;

import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.StringUtil;


public class Model {

	public final static String LOCALE_RU = "ru";
	public final static String LOCALE_EN = "en";

	public String systemName;
	public long timestamp;

	public String[] locales; // all available locales
	public String[][] localeTexts; // strings for all locales

	public String localeCurrent; // current locale
	public String[] texts; // strings for current locales
	public int textLength; // string table size

	public ModelDescription description; // model description, description.csv

	public TransportMap[] maps;
	public TransportLine[] lines;
	public TransportSegment[] segments;
	public TransportTransfer[] transfers;
	public TransportStation[] stations;

	public MapView[] views;

	public AbstractImage[] backgrounds;

	/****************** VOLATILE FIELDS ********************/
	public String fileSystemName;
	/********************** GETTERS ************************/

	public ModelDescription getDescription() {
		return description;
	}

	/****************** LOCALIZATION **********************/

	private int getLocaleId(Locale locale){
		final int len = locales.length;
		for(int i = 0; i < len; i++){
			Locale l = new Locale(locales[i]);
			if( l.equals(locale) ){
				return i;
			}
		}
		return 0;
	}

	public Locale[] getAvailableLocales(){
		Locale[] res = new Locale[locales.length];
		final int len = locales.length;
		for(int i = 0; i < len; i++){
			res[i] = new Locale(locales[i]);
		}
		return res;
	}	

	public void setLocale(Locale locale){
		int id = getLocaleId(locale);
		String newLocale = locales[id];
		String[] newTexts = localeTexts[id];
		if(newTexts==null){
			try {
				ModelBuilder.loadModelLocale(fileSystemName, this, new Locale(newLocale));
				newTexts = localeTexts[id];
			} catch (IOException e) {
				newLocale = locales[0];
				newTexts = localeTexts[0];
			}
		}
		localeCurrent = newLocale;
		texts = newTexts;
	}

	public void setLocaleTexts(Locale locale, String[] texts){
		Integer id = getLocaleId(locale);
		if(id!=null){
			localeTexts[id] = texts;
		}
	}

	/********************** STATIC METHODS ***********************/

	public static long getSegmentKey(long fromId, long toId) {
		long nodeKey = ( fromId << 32 ) + toId;
		return nodeKey;
	}

	public static String getLocalizedString(Model owner, int id) {
		return owner.texts[id];
	}

	public static String[] getLocalizedStrings(Model owner, int[] ids) {
		final int len = ids.length; 
		final String[] res = new String[len];
		final String[] texts = owner.texts;
		for(int i = 0; i < len; i++){
			res[i] = texts[i];
		}
		return res;
	}

	/*********************** OBJECT METHODS ***********************/

	public String toString() {
		return "[NAME:" + systemName + ";COUNTRY:" + description.getCountryName() + ";CITY:" + description.getCityName() + ";LOCALES=" + "{" + StringUtil.join(locales,",")  + "}]";
	}

	public boolean completeEqual(Model other) {
		return locationEqual(other) 
		&& timestamp == other.timestamp;
	}

	public boolean locationEqual(Model other) {
		return getCountryName().equals(other.getCountryName())
		&& getCityName().equals(other.getCityName());
	}

	public String getCountryName() {
		return texts[description.countryName];
	}

	public Object getCityName() {
		return texts[description.cityName];
	}	
}
