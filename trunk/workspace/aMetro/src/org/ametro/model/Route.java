package org.ametro.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.ametro.algorithm.DijkstraHeap;

import android.graphics.Rect;

public class Route {

	private MapView mMapView;
	private int mFromId;
	private int mToId;

	private ArrayList<SegmentView> mSegments;
	private ArrayList<StationView> mStations;
	private ArrayList<TransferView> mTransfers;
	private HashMap<StationView,Long> mStationToDelay;
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
	
	public Route(MapView map, int fromId, int toId) {
		mMapView = map;
		mFromId = fromId;
		mToId = toId;
		mSegments = null;
		mStations = null;
		mStationToDelay = null;
		mHasRoute = false;
	}
	
//	public Route(MapView map, Route src){
//		mMapView = map;
//		mFromId = src.mFromId;
//		mToId = src.mToId;
//		mTime = src.mTime;
//		mHasRoute = src.mHasRoute;
//		mRect = src.mRect;
//		if(src.mSegments!=null){
//			mSegments = ModelUtil.copySegments(map, src.mSegments);
//			mStations = ModelUtil.copyStations(map, src.mStations);
//			mTransfers = ModelUtil.copyTransfer(map, src.mTransfers);
//			mStationToDelay = new HashMap<StationView, Long>();
//			for(StationView st : src.mStationToDelay.keySet()){
//				Long delay = src.mStationToDelay.get(st);
//				mStationToDelay.put(map.stations[st.id],delay);
//				
//			}
//			mHasRoute = false;
//			
//		}else{
//			mSegments = null;
//			mStations = null;
//			mTransfers = null;
//			mStationToDelay = null;
//			mHasRoute = false;
//		}
//	}

	public long getStationDelay(StationView station){
		Long delay = mStationToDelay.get(station);
		if(delay!=null){
			return delay;
		}
		return -1;
	}
	
	public ArrayList<StationView> getStations() {
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

	
	public ArrayList<SegmentView> getSegments() {
		if (mSegments == null) {
			findRoute();
		}
		return mSegments;
	}

	public ArrayList<TransferView> getTransfers() {
		if (mSegments == null){
			findRoute();
		}
		return mTransfers;
	}
	

	public void findRoute() {
		
		final Model model = mMapView.owner;
		final MapView map = mMapView;
		
		final int count = model.stations.length;
		
		DijkstraHeap.Graph g = new DijkstraHeap.Graph(count);
		
		for (SegmentView seg : map.segments) {
			TransportSegment tseg = model.segments[seg.segmentId];
			Integer delay = tseg.delay;
			if (delay != null) {
				g.addEdge(tseg.stationFromId,tseg.stationToId, (int)delay);
			}
		}
		
		for(TransferView tr : map.transfers){
			TransportTransfer ttr = model.transfers[tr.transferId];
			Integer delay = ttr.delay;
			if (delay != null && delay != 0) {
				g.addEdge(ttr.stationFromId,ttr.stationToId, delay);
				g.addEdge(ttr.stationToId, ttr.stationFromId, delay);
			}
		}
		
	    long[] distances = new long[count];
	    int[] pred = new int[count];
	    DijkstraHeap.dijkstra(g, map.stations[mFromId].stationId, distances, pred);
		
	    ArrayList<SegmentView> segments = new ArrayList<SegmentView>();
	    ArrayList<StationView> stations = new ArrayList<StationView>();
	    ArrayList<TransferView> transfers = new ArrayList<TransferView>();
	    ArrayList<Long> delays = new ArrayList<Long>();
	    HashMap<StationView, Long> stationToDelay = new HashMap<StationView, Long>();
	    
//		
//	    int to = mToId;
//	    int from = pred[to];
//	    mTime = distances[to];
//	    StationView st = map.stations[to];
//	    stations.add(st);
//	    delays.add(distances[to]);
//	    stationToDelay.put(st, distances[to]);
//	    while( from!=-1 ){
//	    	SegmentView seg = map.getSegment(from, to);
//	    	if(seg!=null){
//	    		segments.add(seg);
//	    	}else{
//	    		TransferView transfer = map.getTransfer(from, to);
//	    		if(transfer!=null){
//	    			transfers.add(transfer);
//	    		}else{
//	    			transfer = map.getTransfer(to, from);
//	    			if(transfer!=null){
//		    			transfers.add(transfer);
//		    		}
//	    		}
//	    	}
//	    	to = from;
//	    	from = pred[to];
//	    	
//    		st = map.stations[to];
//		    stations.add(st);
//		    delays.add(distances[to]);
//		    stationToDelay.put(st, distances[to]);
//	    	
//	    }
	    
	    if(segments!=null && segments.size()>0){
	    	mTransfers = transfers;
	    	mSegments = segments;
    		Collections.reverse(stations);
	    	mStations = stations;
    		Collections.reverse(delays);
    		mDelays = delays;
	    	mStationToDelay = stationToDelay;
	    	mHasRoute = true;
//	    	mRect = ModelUtil.getDimensions(
//	    			(SegmentView[]) segments.toArray(new SegmentView[segments.size()]), 
//	    			(StationView[]) stations.toArray(new StationView[stations.size()]));
	    }else{
	    	mHasRoute = false;
	    	mSegments = null;
	    	mStations = null;
	    	mRect = null;
	    }
	}

	public StationView getStationFrom() {
		return mMapView.stations[mFromId];
	}

	public StationView getStationTo() {
		return mMapView.stations[mToId];
	}

}
