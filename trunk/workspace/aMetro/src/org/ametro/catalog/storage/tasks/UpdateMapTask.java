package org.ametro.catalog.storage.tasks;

import android.os.Parcel;

public abstract class UpdateMapTask extends BaseTask {

	protected final String mSystemName;
	
	public UpdateMapTask(String systemName) {
		this.mSystemName = systemName;
	}
	
	public UpdateMapTask(Parcel in) {
		this.mSystemName = in.readString();
	}

	public Object getTaskId() {
		return mSystemName;
	}

	public boolean isAsync() {
		return false;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mSystemName);
	}

}
