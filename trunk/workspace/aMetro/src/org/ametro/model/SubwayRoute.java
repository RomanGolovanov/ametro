package org.ametro.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.ametro.algorithm.DijkstraHeap;

public class SubwayRoute {

	private SubwayMap mSubwayMap;
	private int mFromId;
	private int mToId;

	private ArrayList<SubwaySegment> mSegments;
	private ArrayList<SubwayStation> mStations;
	private HashMap<SubwayStation,Long> mStationDelays;
	private long mTime;
	private boolean mHasRoute;

	public long getTime(){
		return mTime;
	}
	
	public boolean hasRoute(){
		if(mSegments == null){
			findRoute();
		}
		return mHasRoute;
	}
	
	public SubwayRoute(SubwayMap map, int fromId, int toId) {
		mSubwayMap = map;
		mFromId = fromId;
		mToId = toId;
		mSegments = null;
		mStations = null;
		mStationDelays = null;
		mHasRoute = false;
	}

	public long getStationDelay(SubwayStation station){
		Long delay = mStationDelays.get(station);
		if(delay!=null){
			return delay;
		}
		return -1;
	}
	
	public ArrayList<SubwayStation> getStations() {
		if (mSegments == null) {
			findRoute();
		}
		return mStations;
	}
	
	public ArrayList<SubwaySegment> getSegments() {
		if (mSegments == null) {
			findRoute();
		}
		return mSegments;
	}

	public void findRoute() {
		final SubwayMap map = mSubwayMap;
		final int count = map.stations.length;

		DijkstraHeap.Graph g = new DijkstraHeap.Graph(count);
		for (SubwaySegment seg : map.segments) {
			Double delay = seg.delay;
			if (delay != null) {
				double d = (double)delay;
				g.addEdge(seg.fromStationId,seg.toStationId, (int)d);
			}
		}
		for(SubwayTransfer tr : map.transfers){
			Double delay = tr.delay;
			if (delay != null) {
				int d = (int)Math.round(delay);
				g.addEdge(tr.fromStationId,tr.toStationId, d);
				g.addEdge(tr.toStationId, tr.fromStationId, d);
			}
		}
	    long[] distances = new long[count];
	    int[] pred = new int[count];
	    ArrayList<SubwaySegment> segments = new ArrayList<SubwaySegment>();
	    ArrayList<SubwayStation> stations = new ArrayList<SubwayStation>();
	    HashMap<SubwayStation, Long> stationDelays = new HashMap<SubwayStation, Long>();
	    DijkstraHeap.dijkstra(g, mFromId, distances, pred);
		
	    int to = mToId;
	    int from = pred[to];
	    mTime = distances[to];
	    SubwayStation st = map.stations[to];
	    stations.add(st);
	    stationDelays.put(st, distances[to]);
	    while( from!=-1 ){
	    	SubwaySegment seg = map.getSegment(from, to);
	    	if(seg!=null){
	    		segments.add(seg);
	    	}
	    	to = from;
	    	from = pred[to];
	    	
    		st = map.stations[to];
		    stations.add(st);
		    stationDelays.put(st, distances[to]);
	    	
	    }
	    
	    if(segments!=null && segments.size()>0){
	    	mSegments = segments;
    		Collections.reverse(stations);
	    	mStations = stations;
	    	mStationDelays = stationDelays;
	    	mHasRoute = true;
	    }else{
	    	mHasRoute = false;
	    	mSegments = null;
	    	mStations = null;
	    }
	}

}
