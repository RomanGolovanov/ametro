/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.ametro.directory;

import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.model.ext.ModelLocation;

public class CityStationDictionary
{

	public static class Entity {
		private String mLineSystemName;
		private String mStationSystemName;
		private ModelLocation mLocation;
		
		public String getLineSystemName() {
			return mLineSystemName;
		}

		public String getStationSystemName() {
			return mStationSystemName;
		}

		public ModelLocation getLocation() {
			return mLocation;
		}

		public Entity(String lineSystemName, String stationSystemName, ModelLocation location) {
			super();
			mLineSystemName = lineSystemName;
			mStationSystemName = stationSystemName;
			mLocation = location;
		}
	}
	
	private final HashMap<String, HashMap<String,Entity>> mData;
	private ArrayList<String> mComments;

	public CityStationDictionary(HashMap<String,HashMap<String,Entity>> index, ArrayList<String> comments){
		mComments = comments;
		mData = index;
	}
	
	public ArrayList<String> getComments(){
		return mComments;
	}
	
	public ModelLocation getStationLocation(String lineSystemName, String stationSystemName){
		Entity r = getRecord(lineSystemName, stationSystemName);
		if(r!=null){
			return r.getLocation();
		}
		return null;
	}

	public Entity getRecord(String lineSystemName, String stationSystemName){
		HashMap<String, Entity> city2rec = mData.get(lineSystemName);
		if(city2rec!=null){
			return city2rec.get(stationSystemName);
		}
		return null;
	}		
}