package org.ametro.model.route;

public class TransportRoute {

	/*package*/ int from;
	/*package*/ int to;

	/*package*/ int[] stations;
	/*package*/ long[] delays;
	
	/*package*/ int[] segments;
	/*package*/ int[] transfers;
	
	/*package*/ long length;

	public long getLength() {
		return length;
	}

	public Long getDelay(int index) {
		return delays[index];
	}
	
	
}
