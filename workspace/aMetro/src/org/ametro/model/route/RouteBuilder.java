package org.ametro.model.route;

import org.ametro.model.MapView;
import org.ametro.model.Model;

public class RouteBuilder {

	public final static int ROUTE_OPTION_SHORTEST = 1;
	public final static int ROUTE_OPTION_SIMPLEST = 2;
	public final static int ROUTE_OPTION_ALL = ROUTE_OPTION_SHORTEST | ROUTE_OPTION_SIMPLEST;
	
	public static RouteContainer createRoutes(Model model, int[] stations, int options)
	{
		RouteContainer set = new RouteContainer();
		set.stations = stations;
		set.exclude = null;
		set.flags = options;
		set.routes = new TransportRoute[0];
		
		//TODO: find routes there
		
		return set;
	}
	
	public static RouteView createRouteView(Model model, MapView view, TransportRoute route)
	{
		RouteView routeView = new RouteView(view, route.stationFromId, route.stationToId);
		
		return routeView;
	}
	
}
