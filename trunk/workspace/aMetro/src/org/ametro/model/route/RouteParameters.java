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

import android.os.Parcel;
import android.os.Parcelable;

public class RouteParameters implements Parcelable {

	public static final Parcelable.Creator<RouteParameters> CREATOR = new Parcelable.Creator<RouteParameters>() {
		public RouteParameters createFromParcel(Parcel in) {
		    return new RouteParameters(in);
		}
		public RouteParameters[] newArray(int size) {
		    return new RouteParameters[size];
		}
	};
	
	/*package*/ int from;
	/*package*/ int to;
	
	/*package*/ int[] include;
	/*package*/ int[] exclude;

	/*package*/ int flags;
	/*package*/ int delay;

	/*package*/ int[] transports;
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.from);
		dest.writeInt(this.to);
		dest.writeInt(this.flags);
		dest.writeInt(this.delay);
		dest.writeInt(this.include.length);
		dest.writeIntArray(this.include);
		dest.writeInt(this.exclude.length);
		dest.writeIntArray(this.exclude);
		dest.writeInt(this.transports.length);
		dest.writeIntArray(this.transports);
	}


	private RouteParameters(Parcel in) {
		from = in.readInt();
		to = in.readInt();
		flags = in.readInt();
		delay = in.readInt();
		include = new int[in.readInt()];		
		in.readIntArray(include);
		exclude =new int[in.readInt()];		
		in.readIntArray(exclude);
		transports = new int[in.readInt()];		
		in.readIntArray(transports);
	}
		
	public RouteParameters(int from, int to, int[] include, int[] exclude, int flags, int[] transports, int delay)
	{
		this.from = from;
		this.to = to;
		this.include = include;
		this.exclude = exclude;
		this.flags = flags;
		this.transports = transports;
		this.delay = delay;
	}
	
}
