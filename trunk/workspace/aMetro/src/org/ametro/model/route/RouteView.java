package org.ametro.model.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.ametro.algorithm.DijkstraHeap;
import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.TransportSegment;
import org.ametro.model.TransportTransfer;
import org.ametro.model.util.ModelUtil;

import android.graphics.Rect;

public class RouteView {

	private MapView mMapView;
	private int mFrom;
	private int mTo;

	private ArrayList<SegmentView> mSegments;
	private ArrayList<StationView> mStations;
	private ArrayList<TransferView> mTransfers;
	private HashMap<StationView,Long> mStationDelays;
	private ArrayList<Long> mDelays;
	
	private long mTime;
	private Rect mRect;

	public long getTime(){
		return mTime;
	}
	
	public Rect getRect(){
		return mRect;
	}
	
	public RouteView(MapView map, TransportRoute route) {
		mMapView = map;
		mFrom = route.from;
		mTo = route.to;
		mTime = route.length;

		mStations = findStationViews(map, route.stations);
		mSegments = findSegmentViews(map, route.segments);
		mTransfers = findTransferViews(map, route.transfers);
		
		Rect routeRect = null;
		mStationDelays = new HashMap<StationView, Long>();
		for(int i = 0; i < mStations.size(); i++){
			StationView view = mStations.get(i);
			mStationDelays.put(view,route.getDelay(i));
			if(view.stationNameRect!=null){
				Rect stationRect = ModelUtil.toRect( view.stationNameRect );
				if(routeRect!=null){
					routeRect.union( stationRect );
				}else{
					routeRect = stationRect;
				}
			}
		}
		mRect = routeRect;
	}

	public long getStationDelay(StationView station){
		Long delay = mStationDelays.get(station);
		if(delay!=null){
			return delay;
		}
		return -1;
	}
	
	public ArrayList<StationView> getStations() {
		return mStations;
	}
	
	public ArrayList<Long> getDelays() {
		return mDelays;
	}

	
	public ArrayList<SegmentView> getSegments() {
		return mSegments;
	}

	public ArrayList<TransferView> getTransfers() {
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
	    DijkstraHeap.dijkstra(g, map.stations[mFrom].stationId, distances, pred);
		
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
	    	mStationDelays = stationToDelay;
	    	//mHasRoute = true;
//	    	mRect = ModelUtil.getDimensions(
//	    			(SegmentView[]) segments.toArray(new SegmentView[segments.size()]), 
//	    			(StationView[]) stations.toArray(new StationView[stations.size()]));
	    }else{
	    	//mHasRoute = false;
	    	mSegments = null;
	    	mStations = null;
	    	mRect = null;
	    }
	}

	public StationView getStationFrom() {
		return mMapView.stations[mFrom];
	}

	public StationView getStationTo() {
		return mMapView.stations[mTo];
	}

	private static ArrayList<SegmentView> findSegmentViews(MapView map, int[] segments) {
		ArrayList<SegmentView> res = new ArrayList<SegmentView>();
		for(Integer id : segments) {
			SegmentView view = map.findViewBySegmentId(id);
			if(view!=null){
				res.add(view);
			}
		}
		return res;
	}

	private static ArrayList<StationView> findStationViews(MapView map, int[] stations) {
		ArrayList<StationView> res = new ArrayList<StationView>();
		for(Integer id : stations) {
			StationView view = map.findViewByStationId(id);
			if(view!=null){
				res.add(view);
			}
		}
		return res;
	}

	private static ArrayList<TransferView> findTransferViews(MapView map, int[] transfers) {
		ArrayList<TransferView> res = new ArrayList<TransferView>();
		for(Integer id : transfers) {
			TransferView view = map.findViewByTransferId(id);
			if(view!=null){
				res.add(view);
			}
		}
		return res;
	}
		
}
