package com.ametro.model;

import java.util.Dictionary;

public class Model {
	
	private Dictionary<String, Station> mStationDictionary;
	
	private Station[] mStations;

	private int[] mLineBegin;
	private int[] mLineEnd;
	
	// Graph model
	private int[] mBegin;
	private int[] mEnd;
	private int[] mDelay;
	
}
