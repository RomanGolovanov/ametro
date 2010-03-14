package org.ametro.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.ametro.algorithm.DijkstraHeap;
import org.ametro.util.ModelUtil;

import android.graphics.Rect;

public class SubwayRoute {

	private SubwayMap mSubwayMap;
	private int mFromId;
	private int mToId;

	private ArrayList<SubwaySegment> mSegments;
	private ArrayList<SubwayStation> mStations;
	private ArrayList<SubwayTransfer> mTransfers;
	private HashMap<SubwayStation,Long> mStationToDelay;
	private ArrayList<Long> mDelays;
	
	private long mTime;
	private boolean mHasRoute;
	private Rect mRect;

	public long getTime(){
		return mTime;
	}
	
	public boolean hasRoute(){
		if(mSegments == null){
			findRoute();
		}
		return mHasRoute;
	}
	
	public Rect getRect(){
		if(mSegments == null){
			findRoute();
		}
		return mRect;
	}
	
	public SubwayRoute(SubwayMap map, int fromId, int toId) {
		mSubwayMap = map;
		mFromId = fromId;
		mToId = toId;
		mSegments = null;
		mStations = null;
		mStationToDelay = null;
		mHasRoute = false;
	}

	public long getStationDelay(SubwayStation station){
		Long delay = mStationToDelay.get(station);
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
	
	public ArrayList<Long> getDelays() {
		if (mSegments == null) {
			findRoute();
		}
		return mDelays;
	}

	
	public ArrayList<SubwaySegment> getSegments() {
		if (mSegments == null) {
			findRoute();
		}
		return mSegments;
	}

	public ArrayList<SubwayTransfer> getTransfers() {
		if (mSegments == null){
			findRoute();
		}
		return mTransfers;
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
			if (delay != null && delay != 0) {
				int d = (int)Math.round(delay);
				g.addEdge(tr.fromStationId,tr.toStationId, d);
				g.addEdge(tr.toStationId, tr.fromStationId, d);
			}
		}
	    long[] distances = new long[count];
	    int[] pred = new int[count];
	    ArrayList<SubwaySegment> segments = new ArrayList<SubwaySegment>();
	    ArrayList<SubwayStation> stations = new ArrayList<SubwayStation>();
	    ArrayList<SubwayTransfer> transfers = new ArrayList<SubwayTransfer>();
	    ArrayList<Long> delays = new ArrayList<Long>();
	    HashMap<SubwayStation, Long> stationToDelay = new HashMap<SubwayStation, Long>();
	    DijkstraHeap.dijkstra(g, mFromId, distances, pred);
		
	    int to = mToId;
	    int from = pred[to];
	    mTime = distances[to];
	    SubwayStation st = map.stations[to];
	    stations.add(st);
	    delays.add(distances[to]);
	    stationToDelay.put(st, distances[to]);
	    while( from!=-1 ){
	    	SubwaySegment seg = map.getSegment(from, to);
	    	if(seg!=null){
	    		segments.add(seg);
	    	}else{
	    		SubwayTransfer transfer = map.getTransfer(from, to);
	    		if(transfer!=null){
	    			transfers.add(transfer);
	    		}else{
	    			transfer = map.getTransfer(to, from);
	    			if(transfer!=null){
		    			transfers.add(transfer);
		    		}
	    		}
	    	}
	    	to = from;
	    	from = pred[to];
	    	
    		st = map.stations[to];
		    stations.add(st);
		    delays.add(distances[to]);
		    stationToDelay.put(st, distances[to]);
	    	
	    }
	    
	    if(segments!=null && segments.size()>0){
	    	mTransfers = transfers;
	    	mSegments = segments;
    		Collections.reverse(stations);
	    	mStations = stations;
    		Collections.reverse(delays);
    		mDelays = delays;
	    	mStationToDelay = stationToDelay;
	    	mHasRoute = true;
	    	mRect = ModelUtil.getDimensions(
	    			(SubwaySegment[]) segments.toArray(new SubwaySegment[segments.size()]), 
	    			(SubwayStation[]) stations.toArray(new SubwayStation[stations.size()]));
	    }else{
	    	mHasRoute = false;
	    	mSegments = null;
	    	mStations = null;
	    	mRect = null;
	    }
	}

	public SubwayStation getStationFrom() {
		return mSubwayMap.stations[mFromId];
	}

	public SubwayStation getStationTo() {
		return mSubwayMap.stations[mToId];
	}

}
