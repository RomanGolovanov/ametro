package com.ametro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.Rect;

public class Station implements Serializable {

	private static final long serialVersionUID = 584570784752337438L;

	private String mName;
	private Rect mRect;
	private Point mPoint;
	private Line mLine;

	private ArrayList<Segment> mSegments = new ArrayList<Segment>();
	private ArrayList<Segment> mSegmentsIn = new ArrayList<Segment>();
	private ArrayList<Segment> mSegmentsOut = new ArrayList<Segment>();

	public Station(String mName, Rect mRect, Point mPoint, Line mLine) {
		super();
		this.mName = mName;
		this.mRect = mRect;
		this.mPoint = mPoint;
		this.mLine = mLine;
	}

	public Rect getRect() {
		return mRect;
	}

	public void setRect(Rect mRect) {
		this.mRect = mRect;
	}

	public Point getPoint() {
		return mPoint;
	}

	public void setPoint(Point mPoint) {
		this.mPoint = mPoint;
	}

	public String getName() {
		return mName;
	}

	public Line getLine() {
		return mLine;
	}

	public boolean hasConnections() {
		for (Iterator<Segment> segments = mSegments.iterator(); segments.hasNext();) {
			Segment segment = segments.next();
			Double delay = segment.getDelay();
			if(delay != null && delay != 0){
				return true;
			}
		}
		return false;
	}
	
	public Segment addSegment(Segment segment, int segmentMode){
		mSegments.add(segment);
		if(segmentMode == Segment.SEGMENT_BEGIN){
			mSegmentsOut.add(segment);
		}
		if(segmentMode == Segment.SEGMENT_END){
			mSegmentsIn.add(segment);
		}
		return segment;
	}

	@Override
	public String toString() {
		return "[NAME:" + mName + "]";
	}
}
