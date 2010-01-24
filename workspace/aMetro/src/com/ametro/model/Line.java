package com.ametro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.Rect;

public class Line implements Serializable {

	private static final long serialVersionUID = -6298848654051375920L;
	
	private String mName;
	private int mColor;
	
	private Dictionary<String, Station> mStations = new Hashtable<String, Station>();
	private ArrayList<Segment> mSegments = new ArrayList<Segment>();
	
	public Line(String mName, int mColor) {
		super();
		this.mName = mName;
		this.mColor = mColor;
	}

	public String getName() {
		return mName;
	}

	public int getColor() {
		return mColor;
	}

	public Station getStation(String name){
		return mStations.get(name);
	}
	
	private Station addStation(String name, Rect r, Point p)
	{
		Station st = new Station(name, r, p, this);
		mStations.put(name,st);
		return st;
	}

	public Station invalidateStation(String name)
	{
		Station st = mStations.get(name);
		if(st == null){
			st = addStation(name, null, null);
		}
		return st;
	}
	
	public Station invalidateStation(String name, Rect r, Point p)
	{
		Station st = mStations.get(name);
		if(st == null){
			st = addStation(name, r, p);
		}else{
			st.setPoint(p);
			st.setRect(r);
		}
		return st;
	}
	
	public Segment addSegment(Station from, Station to, Double delay){
		Segment sg = new Segment(from, to, delay);
		mSegments.add(sg);
		return sg;
	}
	
	public Enumeration<Station> getStations(){
		return mStations.elements();
	}
	
	public Iterator<Segment> getSegments(){
		return mSegments.iterator();
	}

	public Segment getSegment(Station from, Station to) {
		final String fromName = from.getName();
		final String toName = to.getName();
		for (Iterator<Segment> iterator = mSegments.iterator(); iterator.hasNext();) {
			Segment seg = iterator.next();
			if(seg.getFrom().getName().equals(fromName) && seg.getTo().getName().equals(toName)){
				return seg;
			}
		}
		return null;
	}
	
}
