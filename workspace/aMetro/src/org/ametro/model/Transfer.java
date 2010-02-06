package org.ametro.model;

import java.io.Serializable;

public class Transfer implements Serializable {
	
	private static final long serialVersionUID = 5468026097510797847L;

	public static final int INVISIBLE = 1;
	
	private Double mDelay;
	private Station mFrom;
	private Station mTo;
	private int mFlags;
	
	public Transfer(Station from, Station to,  Double delay, int flags) {
		super();
		this.mDelay = delay;
		this.mFrom = from;
		this.mTo = to;
		this.mFlags = flags;
	}

	public Double getDelay() {
		return mDelay;
	}

	public Station getFrom() {
		return mFrom;
	}

	public Station getTo() {
		return mTo;
	}
	
	public int getFlags() {
		return mFlags;
	}
	
	

}
