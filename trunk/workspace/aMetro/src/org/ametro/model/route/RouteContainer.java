package org.ametro.model.route;

public class RouteContainer {

	/*package*/ int[] stations;
	/*package*/ int[] exclude;

	/*package*/ int flags;
	
	/*package*/ TransportRoute[] routes;

	public boolean hasRoutes() {
		return routes!=null && routes.length>0;
	}
	
}
