package org.ametro.model.route;

import java.util.ArrayList;
import java.util.Collections;

import org.ametro.algorithm.DijkstraHeap;
import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.model.TransportSegment;
import org.ametro.model.TransportTransfer;
import org.ametro.model.util.ModelUtil;

public class RouteBuilder {

	public final static int ROUTE_OPTION_SHORTEST = 1;
	public final static int ROUTE_OPTION_SIMPLEST = 2;
	public final static int ROUTE_OPTION_ALL = ROUTE_OPTION_SHORTEST | ROUTE_OPTION_SIMPLEST;
	
	public static RouteContainer createRoutes(Model model, int from, int to, int[] include, int[] exclude, int flags)
	{
		TransportRoute[] routes = findRoutes(model, from, to, include, exclude, flags);// new TransportRoute[0];
		RouteContainer set = new RouteContainer(from, to, include, exclude, flags, routes);
		return set;
	}
	
	public static RouteView createRouteView(Model model, MapView view, TransportRoute route)
	{
		return new RouteView(view, route);
	}

	private static TransportRoute[] findRoutes(Model model, int from, int to, int[] include, int[] exclude, int flags) {
		TransportRoute route = findRoute(model, from, to, include, exclude, flags);
		if(route!=null){
			TransportRoute[] routes = new TransportRoute[1];
			routes[0] = route;
			return routes;
		}
		return null;
	}

	public static TransportRoute findRoute(Model model, int from, int to, int[] include, int[] exclude, int flags) {
		
		final int count = model.stations.length;
		
		DijkstraHeap.Graph g = new DijkstraHeap.Graph(count);
		
		for (TransportSegment seg : model.segments) {
			Integer delay = seg.delay;
			if (delay != null) {
				g.addEdge(seg.stationFromId,seg.stationToId, (int)delay);
			}
		}
		
		for(TransportTransfer tr : model.transfers){
			Integer delay = tr.delay;
			if (delay != null && delay != 0) {
				g.addEdge(tr.stationFromId,tr.stationToId, delay);
				g.addEdge(tr.stationToId, tr.stationFromId, delay);
			}
		}
		
	    long[] distances = new long[count];
	    int[] pred = new int[count];
	    DijkstraHeap.dijkstra(g, from, distances, pred);
		
	    ArrayList<Integer> stations = new ArrayList<Integer>();
	    ArrayList<Integer> segments = new ArrayList<Integer>();
	    ArrayList<Integer> transfers = new ArrayList<Integer>();
	    ArrayList<Long> delays = new ArrayList<Long>();
	    //HashMap<Integer, Long> stationToDelay = new HashMap<Integer, Long>();
	    
		
	    int _to = to;
	    int _from = pred[_to];
	    stations.add(_to);
	    delays.add(distances[_to]);
	    long length = distances[_to];
	    while( _from!=-1 ){
	    	TransportSegment seg = model.getTransportSegment(_from, _to);
	    	if(seg!=null){
	    		segments.add(seg.id);
	    	}else{
	    		TransportTransfer transfer = model.getTransportTransfer(_from, _to);
	    		if(transfer!=null){
	    			transfers.add(transfer.id);
	    		}else{
	    			transfer = model.getTransportTransfer(_to, _from);
	    			if(transfer!=null){
		    			transfers.add(transfer.id);
		    		}
	    		}
	    	}
	    	_to = _from;
	    	_from = pred[_to];
	    	
		    stations.add(_to);
		    delays.add(distances[_to]);
	    	
	    }
	    
	    
	    if(length!=-1){
		    TransportRoute route = new TransportRoute();
		    route.from = from;
		    route.to = to;
	    	route.transfers = ModelUtil.toIntArray(transfers);
	    	route.segments = ModelUtil.toIntArray(segments);
    		Collections.reverse(stations);
    		route.stations = ModelUtil.toIntArray(stations);
    		Collections.reverse(delays);
    		route.delays = ModelUtil.toLongArray(delays);
    		route.length = length;
	    	 //mStationDelays = stationToDelay;
    		return route;
	    }
    	return null;
	}	
	
}
