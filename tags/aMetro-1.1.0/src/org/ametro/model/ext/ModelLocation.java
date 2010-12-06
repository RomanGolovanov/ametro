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
package org.ametro.model.ext;

public class ModelLocation {

	public ModelLocation(float latitude, float longtitude) {
		this.latitude = latitude;
		this.longtitude = longtitude;
		height = 0;
		radius = 0;

	}

	public ModelLocation(float latitude, float longtitude, float height, float radius) {
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.height = height;
		this.radius = radius;	
	}

	public float latitude; 	// degrees
	public float longtitude; 	// degrees
	public float height; 		// height above sea level, meters
	public float radius; 		// horizontal size, meters

}
