/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.ametro.model.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.ametro.model.SchemeView;
import org.ametro.model.Model;
import org.ametro.model.TransportLine;
import org.ametro.model.TransportSegment;
import org.ametro.model.TransportStation;
import org.ametro.model.TransportTransfer;
import org.ametro.util.CollectionUtil;
import org.ametro.util.DijkstraHeap;

public class RouteBuilder {

	public final static int ROUTE_OPTION_SHORTEST = 1;
	public final static int ROUTE_OPTION_SIMPLEST = 2;
	public final static int ROUTE_OPTION_ALL = ROUTE_OPTION_SHORTEST | ROUTE_OPTION_SIMPLEST;
	
	public static RouteContainer createRoutes(Model model, RouteParameters parameters)
	{
		TransportRoute[] routes = findRoutes(model, parameters);// new TransportRoute[0];
		RouteContainer set = new RouteContainer(parameters, routes);
		return set;
	}
	
	public static RouteView createRouteView(Model model, SchemeView view, TransportRoute route)
	{
		return new RouteView(view, route);
	}

	private static TransportRoute[] findRoutes(Model model, RouteParameters parameters) {
		TransportRoute route = findRoute(model, parameters);
		if(route!=null && route.steps>0){
			TransportRoute[] routes = new TransportRoute[1];
			routes[0] = route;
			return routes;
		}
		return null;
	}

	public static TransportRoute findRoute(Model model, RouteParameters parameters) {
		
		final int from = parameters.from;
		final int to = parameters.to;
		final int delayMode = parameters.delay;
//		final int[] include = parameters.include;
//		final int[] exclude = parameters.exclude;
//		final int flags = parameters.flags;
		final int count = model.stations.length;
		
		HashSet<Integer> checkedTransports = null;
		if(parameters.transports!=null){
			checkedTransports = new HashSet<Integer>();
			for(int transportId : parameters.transports){
				checkedTransports.add(transportId);
			}
		}
		
		DijkstraHeap.Graph g = new DijkstraHeap.Graph(count);
		
		for (TransportSegment seg : model.segments) {
			if(checkedTransports==null || checkedTransports.contains(seg.mapId)){
				Integer delay = seg.delay;
				if (delay != null && delay != 0) {
					g.addEdge(seg.stationFromId,seg.stationToId, (int)delay);
				}
			}
		}
		
		for(TransportTransfer tr : model.transfers){
			if(checkedTransports==null || (checkedTransports.contains(tr.mapFromId) && checkedTransports.contains(tr.mapToId))){
				Integer delay = tr.delay;
				if (delay != null && delay != 0) {
					if(delayMode!=-1 ){
						final TransportLine line = model.lines[tr.lineToId];
						final Integer[] lineDelays = line.delays; 
						if(lineDelays!=null && delayMode < lineDelays.length){
							final Integer lineDelay = lineDelays[delayMode];
							if(lineDelay!=null){
								delay+=lineDelay; // add line waiting delay to transfer delay
							}
						}
					}
					g.addEdge(tr.stationFromId,tr.stationToId, delay);
					g.addEdge(tr.stationToId, tr.stationFromId, delay);
				}
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
	    int steps = 0;
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
	    	
		    steps++;
	    }
	    
	    
	    if(length!=-1){
		    TransportRoute route = new TransportRoute();
		    route.from = from;
		    route.to = to;
	    	route.transfers =CollectionUtil.toArray(transfers);
	    	route.segments = CollectionUtil.toArray(segments);
    		Collections.reverse(stations);
    		route.stations = CollectionUtil.toArray(stations);
    		Collections.reverse(delays);
    		route.delays = CollectionUtil.toArray(delays);
    		route.length = length;
    		route.steps = steps;
	    	 //mStationDelays = stationToDelay;

			if(delayMode!=-1 ){
				final TransportStation station = model.stations[from];
				final TransportLine line = model.lines[station.lineId];
				final Integer[] lineDelays = line.delays; 
				if(lineDelays!=null && delayMode < lineDelays.length){
					final Integer lineDelay = lineDelays[delayMode];
					route.length+=lineDelay;
					if(lineDelay!=null){
						final int len = route.delays.length;
						for(int i=0;i<len;i++){
							route.delays[i] += lineDelay; // add start line delay to all steps 
						}
					}
				}
			}
    		
    		
    		return route;
	    }
    	return null;
	}	
	
}
