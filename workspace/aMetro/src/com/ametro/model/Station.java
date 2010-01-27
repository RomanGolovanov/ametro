package com.ametro.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.ametro.libs.Helpers;

import android.graphics.Point;
import android.graphics.Rect;

public class Station implements Serializable {

	private static final long serialVersionUID = 584570784752337438L;

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeObject(mName);
		Helpers.serializeRect(out,mRect);
		Helpers.serializePoint(out,mPoint);
		out.writeObject(mLine);

	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		mName = (String)in.readObject();
		mRect = Helpers.deserializeRect(in);
		mPoint = Helpers.deserializePoint(in);
		mLine = (Line)in.readObject();
		mSegments = new ArrayList<Segment>();
	}	
	
	private String mName;
	private Rect mRect;
	private Point mPoint;
	private Line mLine;

	private ArrayList<Segment> mSegments = new ArrayList<Segment>();

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
		return segment;
	}

	@Override
	public String toString() {
		return "[NAME:" + mName + "]";
	}
}
