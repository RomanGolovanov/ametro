/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.ametro.model;

import java.util.HashMap;


public class MapView {

	public int id;
	public String systemName;

	public int width;
	public int height;

	public int stationDiameter;
	public int lineWidth;

	public String backgroundSystemName;

	public boolean isVector;
	public boolean isWordWrap;
	public boolean isUpperCase;

	public int[] transports; // TransportMap objects
	public int[] transportsChecked; // TransportMap objects, checked by default

	public LineView[] lines;
	public StationView[] stations;
	public SegmentView[] segments;
	public TransferView[] transfers;

	public Model owner;

	/************************ VOLATILE FIELDS **************************/
	private HashMap<Integer, SegmentView[]> segmentsByStationId;
	private HashMap<Long, SegmentView> segmentsIndexed;
	private HashMap<Long, TransferView> transfersIndexed;
	//private HashMap<Integer, LineView> linesIndexed;

	/************************** METHODS ********************************/

	public String toString() {
		return "[NAME:" + systemName + ";VECTOR:" + isVector + ";W:" + width + ";H:" + height + "]";
	}

	public boolean equals(Object o) {
		return this == o || o != null && getClass() == o.getClass() && id == ((MapView) o).id;
	}

	public int hashCode() {
		return id;
	}

	public boolean hasConnections(StationView station) {
		if (station != null) {
			SegmentView[] stationSegments = segmentsByStationId.get(station.id);
			if (stationSegments != null) {
				for (SegmentView segment : stationSegments) {
					Integer delay = owner.segments[segment.id].delay;
					if (delay != null && delay != 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public SegmentView getSegmentView(StationView from, StationView to) {
		if (to != null && from != null) {
			return getSegmentView(from.id, to.id);
		}
		return null;
	}

	public TransferView getTransferView(int stationViewFromId, int stationViewToId) {
		if (transfersIndexed == null) {
			transfersIndexed = new HashMap<Long, TransferView>();
			for (TransferView transfer : transfers) {
				long fromId = transfer.stationViewFromId;
				long toId = transfer.stationViewToId;
				transfersIndexed.put((fromId << 32) + toId, transfer);
			}
		}
		long fromId = stationViewFromId;
		long toId = stationViewToId;
		return transfersIndexed.get( (fromId << 32) + toId );
	}

	public SegmentView getSegmentView(int stationViewFromId, int stationViewToId) {
		if (segmentsIndexed == null) {
			segmentsIndexed = new HashMap<Long, SegmentView>();
			for (SegmentView segment : segments) {
				long segFromId = segment.stationViewFromId;
				long segToId = segment.stationViewToId;
				segmentsIndexed.put((segFromId << 32) + segToId, segment);
			}
		}
		long fromId = stationViewFromId;
		long toId = stationViewToId;
		return segmentsIndexed.get( (fromId << 32) + toId );
	}

	public StationView getStationView(String lineName, String stationName) {
		LineView lineView = getLineView(lineName);
		if(lineView!=null){
			for(StationView station : stations){
				if(station.lineViewId == lineView.id){
					if(station.getName().equalsIgnoreCase(stationName)){
						return station;
					}
				}
			}
		}
		return null;
	}

	public LineView getLineView(String lineName) {
		for(LineView line : lines){
			final TransportLine l = owner.lines[line.id];
			if(l.systemName.equalsIgnoreCase(lineName)){
				return line;
			}
		}
		return null;
	}
	
//		public LineView getLineView(int lineId) {
//			if(linesIndexed == null){
//				linesIndexed = new HashMap<Integer, LineView>();
//				for(LineView line : lines){
//					linesIndexed.put(line.id, line);
//				}
//			}
//			return linesIndexed.get(lineId);
//		}




}
