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
 */package org.ametro.model;

import java.util.ArrayList;

import org.ametro.model.ext.ModelRect;

public class LineView {

	public int id;
	public int lineId;
	
	public int lineWidth;
	public int lineColor; 
	public int labelColor;
	public int labelBackgroundColor;
	public ModelRect lineNameRect;

	public Model owner;

	public String getName() {
		return owner.lines[lineId].getName();
	}

	public ArrayList<StationView> getStations(SchemeView scheme) {
		ArrayList<StationView> stations = new ArrayList<StationView>();
		for(StationView station : scheme.stations){
			if(station.lineViewId == id && station.stationPoint!=null && station.stationNameRect!=null){
				stations.add(station);
			}
		}
		return stations;
	}
	
}
