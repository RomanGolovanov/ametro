package com.ametro.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class Model implements Serializable {

	private static final long serialVersionUID = 8360920748660733331L;

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeObject(mMapName);
		out.writeObject(mCountryName);
		out.writeObject(mCityName);
		out.writeInt(mWidth);
		out.writeInt(mHeight);

		out.writeInt(mStationDiameter);
		out.writeInt(mLinesWidth);

		out.writeBoolean(mWordWrap);
		out.writeBoolean(mUpperCase);

		out.writeInt(mLines.size());
		Enumeration<Line> lines = mLines.elements();
		while(lines.hasMoreElements()) {
			out.writeObject(lines.nextElement());
		}

		out.writeInt(mTransfers.size());
		for (Iterator<Transfer> transfers = mTransfers.iterator(); transfers.hasNext();) {
			out.writeObject(transfers.next());
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{

		mMapName = (String)in.readObject();
		mCountryName = (String)in.readObject();
		mCityName = (String)in.readObject();
		mWidth = in.readInt();
		mHeight = in.readInt();

		mStationDiameter = in.readInt();
		mLinesWidth = in.readInt();

		mWordWrap = in.readBoolean();
		mUpperCase = in.readBoolean();

		mLines = new Hashtable<String, Line>();
		int lineCount = in.readInt();
		for(int i = 0; i < lineCount; i++) {
			Line line = (Line)in.readObject();
			mLines.put(line.getName(), line);
		}

		mTransfers = new ArrayList<Transfer>();
		int transferCount = in.readInt();
		for (int i = 0; i < transferCount; i++) {
			mTransfers.add((Transfer)in.readObject());
		}		
	}


	public Model(String mapName, int size){
		mMapName = mapName;
	}

	private String mMapName;

	private String mCityName;
	private String mCountryName;

	private int mWidth;
	private int mHeight;

	private int mStationDiameter;
	private int mLinesWidth;
	private boolean mWordWrap;
	private boolean mUpperCase;

	private Dictionary<String, Line> mLines = new Hashtable<String, Line>();
	private ArrayList<Transfer> mTransfers = new ArrayList<Transfer>();

	public void setCityName(String cityName) {
		mCityName = cityName;
	}

	public String getCityName() {
		return mCityName;
	}

	public void setCountryName(String countryName) {
		mCountryName = countryName;
	}

	public String getCountryName() {
		return mCountryName;
	}

	public boolean isUpperCase() {
		return mUpperCase;
	}

	public void setUpperCase(boolean mUpperCase) {
		this.mUpperCase = mUpperCase;
	}

	public boolean isWordWrap() {
		return mWordWrap;
	}

	public void setWordWrap(boolean mWordWrap) {
		this.mWordWrap = mWordWrap;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public int getStationDiameter() {
		return mStationDiameter;
	}

	public int getLinesWidth() {
		return mLinesWidth;
	}

	public void setStationDiameter(int mStationDiameter) {
		this.mStationDiameter = mStationDiameter;
	}

	public void setLinesWidth(int mLineWidth) {
		this.mLinesWidth = mLineWidth;
	}

	public String getMapName() {
		return mMapName;
	}

	public void setDimension(int width, int height) {
		mWidth = width;
		mHeight = height;
	}


	public Station getStation(String lineName, String stationName) {
		Line line = mLines.get(lineName);
		if(line!=null){
			return line.getStation(stationName);
		}
		return null;
	}

	public Line getLine(String lineName) {
		return mLines.get(lineName);
	}

	public Transfer addTransfer(Station from, Station to, Double delay, int flags) {
		Transfer tr= new Transfer(from, to, delay, flags);
		mTransfers.add(tr);
		return tr;
	}

	public Line addLine(String lineName, int color, int labelColor, int labelBgColor) {
		Line line = new Line(lineName, color, labelColor, labelBgColor);
		mLines.put(lineName, line);
		return line;
	}

	public Enumeration<Line> getLines() {
		return mLines.elements();
	}

	public Iterator<Transfer> getTransfers(){
		return mTransfers.iterator();
	}

}
