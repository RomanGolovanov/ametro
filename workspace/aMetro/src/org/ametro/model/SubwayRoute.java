package org.ametro.model;

import java.util.ArrayList;

import org.ametro.algorithm.DijkstraHeap;

public class SubwayRoute {

	private SubwayMap mSubwayMap;
	private int mFromId;
	private int mToId;

	private ArrayList<SubwaySegment> mRoute;

	public SubwayRoute(SubwayMap map, int fromId, int toId) {
		mSubwayMap = map;
		mFromId = fromId;
		mToId = toId;
		mRoute = null;
	}

	public ArrayList<SubwaySegment> getRoute() {
		if (mRoute == null) {
			findRoute();
		}
		return mRoute;
	}

	private void findRoute() {
		final SubwayMap map = mSubwayMap;
		final int count = map.stations.length;

		DijkstraHeap.Graph g = new DijkstraHeap.Graph(count);
		for (SubwaySegment seg : map.segments) {
			Double delay = seg.delay;
			if (delay != null) {
				int d = (int)Math.round(delay);
				g.addEdge(seg.fromStationId,seg.toStationId, d);
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
	    ArrayList<SubwaySegment> route = new ArrayList<SubwaySegment>();
	    DijkstraHeap.dijkstra(g, mFromId, distances, pred);
		
	    int to = mToId;
	    int from = pred[to];
	    while( from!=-1 ){
	    	SubwaySegment seg = map.getSegment(from, to);
	    	if(seg!=null){
	    		route.add(seg);
	    	}
	    	to = from;
	    	from = pred[to];
	    }
	    
		mRoute = route;
	}
}
