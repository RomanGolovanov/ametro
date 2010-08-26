/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
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
import java.util.HashMap;

import org.ametro.model.MapView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.util.ModelUtil;

import android.graphics.Rect;

public class RouteView {

	private MapView mMapView;
	private int mFrom;
	private int mTo;

	private ArrayList<SegmentView> mSegments;
	private ArrayList<StationView> mStations;
	private ArrayList<TransferView> mTransfers;
	private HashMap<StationView,Long> mStationDelays;
	private ArrayList<Long> mDelays;
	
	private long mTime;
	private Rect mRect;

	public long getTime(){
		return mTime;
	}
	
	public Rect getRect(){
		return mRect;
	}
	
	public RouteView(MapView map, TransportRoute route) {
		mMapView = map;
		mFrom = route.from;
		mTo = route.to;
		mTime = route.length;

		mStations = findStationViews(map, route.stations);
		mSegments = findSegmentViews(map, route.segments);
		mTransfers = findTransferViews(map, route.transfers);
		
		Rect routeRect = null;
		mStationDelays = new HashMap<StationView, Long>();
		for(int i = 0; i < mStations.size(); i++){
			StationView view = mStations.get(i);
			mStationDelays.put(view,route.getDelay(i));
			if(view.stationNameRect!=null){
				Rect stationRect = ModelUtil.toRect( view.stationNameRect );
				if(routeRect!=null){
					routeRect.union( stationRect );
				}else{
					routeRect = stationRect;
				}
			}
		}
		mRect = routeRect;
	}

	public long getStationDelay(StationView station){
		Long delay = mStationDelays.get(station);
		if(delay!=null){
			return delay;
		}
		return -1;
	}
	
	public ArrayList<StationView> getStations() {
		return mStations;
	}
	
	public ArrayList<Long> getDelays() {
		return mDelays;
	}

	
	public ArrayList<SegmentView> getSegments() {
		return mSegments;
	}

	public ArrayList<TransferView> getTransfers() {
		return mTransfers;
	}

	public StationView getStationFrom() {
		return mMapView.stations[mFrom];
	}

	public StationView getStationTo() {
		return mMapView.stations[mTo];
	}

	private static ArrayList<SegmentView> findSegmentViews(MapView map, int[] segments) {
		ArrayList<SegmentView> res = new ArrayList<SegmentView>();
		for(Integer id : segments) {
			SegmentView view = map.findViewBySegmentId(id);
			if(view!=null){
				res.add(view);
			}
		}
		return res;
	}

	private static ArrayList<StationView> findStationViews(MapView map, int[] stations) {
		ArrayList<StationView> res = new ArrayList<StationView>();
		for(Integer id : stations) {
			StationView view = map.findViewByStationId(id);
			if(view!=null){
				res.add(view);
			}
		}
		return res;
	}

	private static ArrayList<TransferView> findTransferViews(MapView map, int[] transfers) {
		ArrayList<TransferView> res = new ArrayList<TransferView>();
		for(Integer id : transfers) {
			TransferView view = map.findViewByTransferId(id);
			if(view!=null){
				res.add(view);
			}
		}
		return res;
	}
		
}
