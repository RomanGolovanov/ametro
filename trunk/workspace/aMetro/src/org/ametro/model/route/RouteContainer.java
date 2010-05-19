package org.ametro.model.route;

public class RouteContainer {

	/*package*/ int from;
	/*package*/ int to;
	
	/*package*/ int[] include;
	/*package*/ int[] exclude;

	/*package*/ int flags;
	
	/*package*/ TransportRoute[] routes;

	public RouteContainer(int from, int to, int[] include, int[] exclude, int flags, TransportRoute[] routes){
		this.from = from;
		this.to = to;
		this.include = include;
		this.exclude = exclude;
		this.flags = flags;
		this.routes = routes;
	}
	
	public boolean hasRoutes() {
		return routes!=null && routes.length>0;
	}

	public int getStationFromId() {
		return from;
	}

	public int getStationToId() {
		return to;
	}

	public TransportRoute getDefaultRoute() {
		return hasRoutes() ? routes[0] : null;
	}
	
}
