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
package org.ametro.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.ext.ModelRect;
import org.ametro.util.CollectionUtil;

import android.content.Context;

public class SchemeView {

	public int id;
	public String systemName;
	public int name;
	public boolean isMain;
	
	public long transportTypes;

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

	//************************ VOLATILE FIELDS **************************//
	private HashMap<Long, SegmentView> segmentsIndexed;
	private HashMap<Long, TransferView> transfersIndexed;
	//************************** METHODS ********************************//



	public String toString() {
		return "[NAME:" + systemName + ";VECTOR:" + isVector + ";W:" + width + ";H:" + height + "]";
	}

	public boolean equals(Object o) {
		return this == o || o != null && getClass() == o.getClass() && id == ((SchemeView) o).id;
	}

	public int hashCode() {
		return id;
	}

	public boolean hasConnections(StationView station) {
		if (station != null) {
			final int id = station.stationId;
			for(TransportSegment seg : owner.segments){
				if(seg.stationFromId == id || seg.stationToId == id){
					Integer delay = seg.delay;
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

	public StationView getStationViewByDisplayName(String lineName, String stationName) {
		LineView lineView = getLineViewByDisplayName(lineName);
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

	public LineView getLineViewByDisplayName(String lineName) {
		for(LineView line : lines){
			final TransportLine l = owner.lines[line.lineId];
			if(l.getName().equalsIgnoreCase(lineName)){
				return line;
			}
		}
		return null;
	}

	public StationView findViewByStationId(int stationId) {
		for(StationView view : stations){
			if(view.stationId == stationId){
				return view;
			}
		}
		return null;
	}

	public SegmentView findViewBySegmentId(int segmentId) {
		for(SegmentView view : segments){
			if(view.segmentId == segmentId){
				return view;
			}
		}		
		return null;
	}

	public TransferView findViewByTransferId(int transferId) {
		for(TransferView view : transfers){
			if(view.transferId == transferId){
				return view;
			}
		}		
		return null;
	}

	public TransportCollection getTransportCollection(Context context) {
		return new TransportCollection(this,context);
	}	
	

	public static class TransportCollection 
	{
		private TransportMap[] maps;
		private String[] names;
		private boolean[] checks;

		public String[] getNames(){
			return names;
		}
		
		public boolean[] getStates(){
			return checks;
		}
		
		public void setState(int index, boolean isChecked){
			checks[index] = isChecked;
		}
		
		public int[] getCheckedTransports(){
			ArrayList<Integer> lst = new ArrayList<Integer>();
			final int len = maps.length; 
			for(int i=0; i<len; i++){
				if(checks[i]){
					lst.add(maps[i].id);
				}
			}
			return CollectionUtil.toArray(lst);
		}
		
		public TransportCollection(TransportCollection src){
			maps = src.maps.clone();
			names = src.names.clone();
			checks = src.checks.clone();
		}
		
		public TransportCollection(SchemeView view, Context context) {
			//final HashSet<Integer> checkedSet = CollectionUtil.toHashSet(view.getCheckedTransports());
			final String[] transportNames = context.getResources().getStringArray(R.array.transport_types);
			final int[] transports = view.getTransports();
			final int len = transports.length;
			
			maps = new TransportMap[len];
			names = new String[len];
			checks = new boolean[len];

			final TransportMap[] allMaps = view.owner.maps;
			for(int i=0; i<len; i++){
				final int id = transports[i];
				final TransportMap map = allMaps[id];
				maps[i] = map;
				names[i] =  transportNames[map.typeName];
				checks[i] = true;// checkedSet.contains(id);
			}
		}	
		
	}


	public int[] getTransports() {
		return (this.transports!=null && this.transports.length>0) ? this.transports : new int[]{0};
	}

	public int[] getCheckedTransports() {
		return (this.transportsChecked!=null && this.transportsChecked.length>0) ? this.transportsChecked : new int[]{0};
	}

	public StationView[] getStationArray(boolean includeUnderConstruction) {
		ArrayList<StationView> result = getStationList(includeUnderConstruction);
		return (StationView[]) result.toArray(new StationView[result.size()]);
	}

	public ArrayList<StationView> getStationList(boolean includeUnderConstruction) {
		ArrayList<StationView> result = new ArrayList<StationView>();
		final StationView[] stations = this.stations; 
		final int len = stations.length;
		for(int i=0; i<len; i++){
			StationView station = stations[i];
	        if(includeUnderConstruction || hasConnections(station)){
	        	result.add(station);
	        }
		}
		return result;
	}

	public StationView findStation(int x, int y) {
		for(StationView station : this.stations){
			final ModelRect r = station.stationNameRect;
			if(r!=null && r.contains(x,y)){
				return station;
			}
			final ModelPoint p = station.stationPoint;
			if(p!=null && p.distance(x,y) <= this.stationDiameter ){
				return station;
			}
			
		}
		return null;
	}	
}
