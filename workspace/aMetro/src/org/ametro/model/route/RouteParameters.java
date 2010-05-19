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

	/*package*/ int[] transports;
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.from);
		dest.writeInt(this.to);
		dest.writeInt(this.flags);
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
		include = new int[in.readInt()];		
		in.readIntArray(include);
		exclude =new int[in.readInt()];		
		in.readIntArray(exclude);
		transports = new int[in.readInt()];		
		in.readIntArray(transports);
	}
		
	
}
