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
package org.ametro.model.route;

public class TransportRoute {

	/*package*/ int from;
	/*package*/ int to;

	/*package*/ int[] stations;
	/*package*/ long[] delays;
	
	/*package*/ int[] segments;
	/*package*/ int[] transfers;
	
	/*package*/ long length;
	/*package*/ int steps;

	public long getLength() {
		return length;
	}

	public Long getDelay(int index) {
		return delays[index];
	}
	
	
}
