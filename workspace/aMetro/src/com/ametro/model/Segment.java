package com.ametro.model;

import java.io.Serializable;

import android.graphics.Point;

public class Segment implements Serializable {

	private static final long serialVersionUID = -3800882714522185877L;

	public static final int SPLINE		= 0x01;
	public static final int INVISIBLE 	= 0x02;

	public static final int SEGMENT_BEGIN 	= 0x01;
	public static final int SEGMENT_END 	= 0x02;
	
	private Double mDelay;
	private Point[]	mAdditionalNodes;
	private Station mFrom;
	private Station mTo;
	private int mFlags;
	
	public Segment(Station from, Station to,  Double delay) {
		super();
		this.mDelay = delay;
		this.mFrom = from;
		this.mTo = to;
		this.mFlags = 0;
		
		from.addSegment(this,Segment.SEGMENT_BEGIN);
		to.addSegment(this,Segment.SEGMENT_END);
	}

	public Point[] getAdditionalNodes() {
		return mAdditionalNodes;
	}

	public void setAdditionalNodes(Point[] additionalNodes) {
		this.mAdditionalNodes = additionalNodes;
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

	public void setFlags(int flags) {
		this.mFlags = flags;
	}

	public void addFlag(int flag){
		this.mFlags |= flag;
	}
	
	@Override
	public String toString() {
		return "[FROM:" + mFrom.getName() + ";TO:" + mTo.getName() + "]";
	}
	
	
}
