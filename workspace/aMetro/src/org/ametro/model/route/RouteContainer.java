package org.ametro.model.route;

public class RouteContainer {

	/*package*/ RouteParameters parameters;
	/*package*/ TransportRoute[] routes;

	/*package*/ RouteContainer(RouteParameters parameters, TransportRoute[] routes){
		this.parameters = parameters;
		this.routes = routes;
	}

	public boolean hasRoutes() {
		return routes!=null && routes.length>0;
	}

	public int getStationFromId() {
		return parameters.from;
	}

	public int getStationToId() {
		return parameters.to;
	}

	public TransportRoute getDefaultRoute() {
		return hasRoutes() ? routes[0] : null;
	}
	
}
