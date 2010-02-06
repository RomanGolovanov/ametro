package org.ametro.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.Rect;

public class Line implements Serializable {

	private static final long serialVersionUID = -957788093146079549L;

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeObject(mName);

		out.writeInt(mColor);
		out.writeInt(mLabelColor);
		out.writeInt(mLabelBgColor);


		out.writeInt(mSegments.size());
		for (Iterator<Segment> segments = mSegments.iterator(); segments.hasNext();) {
			out.writeObject(segments.next());
		}

		out.writeInt(mStations.size());
		Enumeration<Station> stations = mStations.elements();
		while(stations.hasMoreElements()) {
			out.writeObject(stations.nextElement());
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{

		mName = (String)in.readObject();

		mColor = in.readInt();
		mLabelColor = in.readInt();
		mLabelBgColor = in.readInt();

		mSegments = new ArrayList<Segment>();
		int segmentCount = in.readInt();
		for (int i = 0; i < segmentCount; i++) {
			mSegments.add((Segment)in.readObject());
		}		

		mStations = new Hashtable<String, Station>();
		int stationCount = in.readInt();
		for(int i = 0; i < stationCount; i++) {
			Station station = (Station)in.readObject();
			mStations.put(station.getName(), station);
		}

		for (Iterator<Segment> segments = mSegments.iterator(); segments.hasNext();) {
			Segment segment = segments.next();
			segment.getFrom().addSegment(segment, Segment.SEGMENT_BEGIN);
			segment.getTo().addSegment(segment, Segment.SEGMENT_END);
		}		
		
	}
	
	
	private String mName;
	private int mColor;
	private int mLabelColor;
	private int mLabelBgColor;
	
	private Dictionary<String, Station> mStations = new Hashtable<String, Station>();
	private ArrayList<Segment> mSegments = new ArrayList<Segment>();
	
	public Line(String name, int color, int labelColor, int labelBgColor) {
		super();
		this.mName = name;
		this.mColor = color;
		this.mLabelColor = labelColor;
		this.mLabelBgColor = labelBgColor;
	}

	public String getName() {
		return mName; 
	}

	public int getColor() {
		return mColor;
	}

	public int getLabelColor() {
		return mLabelColor;
	}

	public int getLabelBgColor() {
		return mLabelBgColor;
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
		Segment opposite = getSegment(to, from); 
		if(opposite!=null && (opposite.getFlags() & Segment.INVISIBLE)==0){
			if(delay == null && opposite.getDelay()!=null){
				sg.addFlag(Segment.INVISIBLE);
			}else if(delay != null && opposite.getDelay()==null){
				opposite.addFlag(Segment.INVISIBLE);
			}else if(delay == null && opposite.getDelay()==null){
				sg.addFlag(Segment.INVISIBLE); 
			}
		}
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
