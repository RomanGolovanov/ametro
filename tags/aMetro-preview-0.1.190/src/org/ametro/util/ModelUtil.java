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

package org.ametro.util;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import org.ametro.MapSettings;
import org.ametro.model.City;
import org.ametro.model.StationAddon;
import org.ametro.model.SubwayLine;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayMapBuilder;
import org.ametro.model.SubwaySegment;
import org.ametro.model.SubwayStation;
import org.ametro.model.SubwayTransfer;
import org.ametro.pmz.FilePackage;
import org.ametro.pmz.GenericResource;
import org.ametro.pmz.TextResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.ametro.Constants.LOG_TAG_MAIN;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 12.02.2010
 *         Time: 15:03:07
 */
public class ModelUtil {

	public static Rect getDimensions(SubwaySegment[] segments, SubwayStation stations[]) {
		int xmin = Integer.MAX_VALUE;
		int ymin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymax = Integer.MIN_VALUE;

		for (SubwayStation station : stations) {
			Point p = station.point;
			if (p != null) {
				if (xmin > p.x)
					xmin = p.x;
				if (ymin > p.y)
					ymin = p.y;

				if (xmax < p.x)
					xmax = p.x;
				if (ymax < p.y)
					ymax = p.y;
			}
			Rect r = station.rect;
			if (r != null) {
				if (xmin > r.left)
					xmin = r.left;
				if (ymin > r.top)
					ymin = r.top;
				if (xmin > r.right)
					xmin = r.right;
				if (ymin > r.bottom)
					ymin = r.bottom;

				if (xmax < r.left)
					xmax = r.left;
				if (ymax < r.top)
					ymax = r.top;
				if (xmax < r.right)
					xmax = r.right;
				if (ymax < r.bottom)
					ymax = r.bottom;
			}
		}
		return new Rect(xmin, ymin, xmax, ymax);
	}
	
	public static City indexPmz(String fileName) throws IOException {
		Date startTimestamp = new Date();
		City model = new City();
		FilePackage pkg = new FilePackage(fileName);
		GenericResource info = pkg.getCityGenericResource();
		String countryName = info.getValue("Options", "Country");
		String cityName = info.getValue("Options", "RusName");
		if (cityName == null) {
			cityName = info.getValue("Options", "CityName");
		}
		model.countryName = countryName;
		model.cityName = cityName;
		model.sourceVersion = MapSettings.getSourceVersion();
		File pmzFile = new File(fileName);
		model.timestamp = pmzFile.lastModified();
		if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
			Log.i(LOG_TAG_MAIN, String.format("PMZ description '%s' loading time is %sms", fileName, Long.toString((new Date().getTime() - startTimestamp.getTime()))));
		}
		return model;
	}


	public static City importPmz(String filename) throws IOException {
		SubwayMapBuilder subwayMapBuilder = new SubwayMapBuilder();
		SubwayMap subwayMap = subwayMapBuilder.importPmz(filename);
		City model = new City();
		model.subwayMap = subwayMap;
		model.cityName = subwayMap.cityName;
		model.countryName = subwayMap.countryName;
		model.height = subwayMap.height;
		model.id = subwayMap.id;
		model.sourceVersion = MapSettings.getSourceVersion();
		model.timestamp = FileUtil.getLastModified(filename);
		model.width = subwayMap.width;
		return model;
	}


	public static ArrayList<StationAddon> importPmzAddons(City city, String fileName) throws IOException {
		final FilePackage pkg = new FilePackage(fileName);
		final HashMap<String, TextResource> texts = pkg.getTextResources();
		final ArrayList<StationAddon> addons = new ArrayList<StationAddon>();
		final SubwayMap map = city.subwayMap;
		for(SubwayStation station : map.stations){
			final SubwayLine line = map.lines[station.lineId];
			final String lineName = line.name;
			final String stationName = station.name; 

			HashMap<String, ArrayList<String>> entries = new HashMap<String, ArrayList<String>>();

			for(TextResource res : texts.values()){
				ArrayList<String> opts ;
				//opts = res.getValue("Options", "MenuName");
				//if(opts==null || opts.size()==0)
				{
					opts = res.getValue("Options", "Caption");
					String captionText = opts!=null && opts.size()>0 ? opts.get(0) : null;
					opts = res.getValue("Options", "StringToAdd");
					String prefix = opts!=null && opts.size()>0 ? opts.get(0) : null;
					if(captionText!=null && prefix!=null){
						if(prefix.startsWith("'") && prefix.endsWith("'")){
							prefix = prefix.substring(1, prefix.length()-1 );
						}
						ArrayList<String> data = res.getValue(lineName, stationName);
						if(data!=null && data.size()>0){

							ArrayList<String> entryText = entries.get(captionText);
							if(entryText==null){
								entryText = new ArrayList<String>();
								entries.put(captionText, entryText);
							}
							for(String lineText : data){
								entryText.add( (prefix + lineText).trim() );
							}
						}
					}
				}
			}


			StationAddon addon = new StationAddon();
			addon.stationId = station.id;

			final int entryCount = entries.keySet().size();
			int entryNumber = 0;
			addon.entries = new StationAddon.Entry[entryCount];
			for(String entryCaption : entries.keySet()){
				StationAddon.Entry entry = new StationAddon.Entry();
				entry.id = entryNumber;
				entry.caption = entryCaption;
				final ArrayList<String> text = entries.get(entryCaption);
				entry.text = (String[]) text.toArray(new String[text.size()]);
				addon.entries[entryNumber] = entry;
				entryNumber++;
			}

			addons.add(addon);

		}
		return addons;
	}
	
	public static ArrayList<SubwaySegment> copySegments(SubwayMap map, ArrayList<SubwaySegment> segments)
	{
		if(segments==null) return null;
		ArrayList<SubwaySegment> res = new ArrayList<SubwaySegment>(segments.size());
		for(SubwaySegment seg : segments){
			res.add(map.segments[seg.id]);
		}
		return res;
	}
	
	public static ArrayList<SubwayStation> copyStations(SubwayMap map, ArrayList<SubwayStation> stations)
	{
		if(stations==null) return null;
		ArrayList<SubwayStation> res = new ArrayList<SubwayStation>(stations.size());
		for(SubwayStation st : stations){
			res.add(map.stations[st.id]);
		}
		return res;
	}
	
	public static ArrayList<SubwayTransfer> copyTransfer(SubwayMap map, ArrayList<SubwayTransfer> transfers)
	{
		if(transfers==null) return null;
		ArrayList<SubwayTransfer> res = new ArrayList<SubwayTransfer>(transfers.size());
		for(SubwaySegment tr : transfers){
			res.add(map.transfers[tr.id]);
		}
		return res;
	}

}
