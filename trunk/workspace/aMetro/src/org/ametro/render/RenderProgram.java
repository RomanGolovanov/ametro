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

package org.ametro.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ametro.model.MapView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.TransportSegment;
import org.ametro.model.TransportStation;
import org.ametro.model.TransportTransfer;
import org.ametro.model.ext.ModelSpline;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;


public class RenderProgram {

	public static final int TYPE_LINE = 1;
	public static final int TYPE_TRANSFER_BACKGROUND = 2;
	public static final int TYPE_TRANSFER = 4;
	public static final int TYPE_STATION = 8;
	public static final int TYPE_STATION_NAME = 16;
	public static final int TYPE_BACKGROUND = 32;

	public static final int ONLY_TRANSPORT = TYPE_LINE | TYPE_TRANSFER_BACKGROUND | TYPE_TRANSFER | TYPE_STATION;
	public static final int ALL = ONLY_TRANSPORT | TYPE_STATION_NAME;

	RenderElement[] mElements;
	RenderElement[] mElementsToRender;
	ArrayList<RenderElement> mClipping;
	Rect[] mBounds;
	int[] mTypes;
	int mRenderFilter;
	
	MapView mMapView;
	
	ArrayList<RenderElement> mRenderQueue;

	HashMap<SegmentView, RenderElement> segmentIndex = new HashMap<SegmentView, RenderElement>();
	HashMap<StationView, RenderElement> stationIndex = new HashMap<StationView, RenderElement>();
	HashMap<StationView, RenderElement> stationNameIndex = new HashMap<StationView, RenderElement>();
	HashMap<TransferView, RenderElement> transferBackgroundIndex = new HashMap<TransferView, RenderElement>();
	HashMap<TransferView, RenderElement> transferIndex = new HashMap<TransferView, RenderElement>();
	

	public void setRenderFilter(int renderFilter) {
		mRenderFilter = renderFilter;
	}

	public void setAntiAlias(boolean enabled){
		for(RenderElement element : mElements){
			element.setAntiAlias(enabled);
		}
	}
	
	public RenderProgram(MapView map) {
		mMapView = map;
		mRenderQueue = new ArrayList<RenderElement>();
		mClipping = new ArrayList<RenderElement>();
		drawLines(map, mRenderQueue);
		drawTransfers(map, mRenderQueue);
		drawStations(map, mRenderQueue);
		updateRenderQueue();
		mRenderFilter = ALL;
	}

	private void updateRenderQueue() {
		Collections.sort(mRenderQueue);
		mElements = mRenderQueue.toArray(new RenderElement[mRenderQueue.size()]);
		mElementsToRender = new RenderElement[0];
		final int count = mElements.length;
		mBounds = new Rect[count];
		mTypes = new int[count];
		for (int i = 0; i < count; i++) {
			mBounds[i] = mElements[i].boundingBox;
			mTypes[i] = mElements[i].type;
		}
	}
	
	public void updateSelection(List<StationView> stations, List<SegmentView> segments, List<TransferView> transfers){
		if(stations!=null || segments!=null){
			for(RenderElement elem : mElements){
				elem.setSelection(false);
			}
			//final Model owner = mMapView.owner;
			if(stations!=null){
				for(StationView station : stations){
					stationIndex.get(station).setSelection(true);
					RenderElement stationName = stationNameIndex.get(station);
					if(stationName!=null){
						stationName.setSelection(true);
					}
				}
			}
			if(transfers!=null){
				for(TransferView transfer : transfers){
					RenderElement elem;
					elem = transferBackgroundIndex.get(transfer);
					if(elem!=null){
						elem.setSelection(true);
					}
					elem = transferIndex.get(transfer);
					if(elem!=null){
						elem.setSelection(true);
					}
				}
			}
			if(segments!=null){
				for(SegmentView segment : segments){
					RenderElement elem = segmentIndex.get(segment);
					if(elem!=null){
						elem.setSelection(true);
					}else{
						SegmentView opposite = mMapView.getSegmentView(segment.stationViewToId, segment.stationViewFromId);
						if(opposite!=null){
							elem = segmentIndex.get(opposite);
							if(elem!=null){
								elem.setSelection(true);
							}
						}
					}
				}
			}
			
		}else{
			for(RenderElement elem : mElements){
				elem.setSelection(true);
			}
		}
		updateRenderQueue();
	}

	private void drawStations(MapView map, ArrayList<RenderElement> renderQueue) {
		final TransportStation[] stations = map.owner.stations; 
		for (StationView station : map.stations) {
			TransportStation s = stations[station.stationId];
			if (station.stationPoint != null) {
				RenderElement stationElement = new RenderStation(map, station, s);
				renderQueue.add(stationElement);
				stationIndex.put(station, stationElement);
				if (station.stationNameRect != null && station.stationNameRect != null) {
					RenderElement stationNameElement = new RenderStationName(map, station, s);
					renderQueue.add(stationNameElement);
					stationNameIndex.put(station, stationNameElement);
				}
			}
		}
	}

	private void drawTransfers(MapView map, ArrayList<RenderElement> renderQueue) {
		final TransportTransfer[] transfers = map.owner.transfers; 
		for (TransferView transfer : map.transfers) {
			final TransportTransfer t = transfers[transfer.transferId];
			if( (t.flags & TransportTransfer.TYPE_INVISIBLE) != 0){
				continue;
			}
			
			RenderElement elementBackground = new RenderTransferBackground(map, transfer, t);
			RenderElement elementTransfer = new RenderTransfer(map, transfer, t);

			renderQueue.add(elementBackground);
			renderQueue.add(elementTransfer);
			
			transferIndex.put(transfer, elementTransfer);
			transferBackgroundIndex.put(transfer, elementBackground);
		}
	}

	private void drawLines(MapView map, ArrayList<RenderElement> renderQueue) {
		final TransportSegment[] segments = map.owner.segments; 
		HashSet<Integer> exclusions = new HashSet<Integer>();
		for (SegmentView segment : map.segments) {
			final TransportSegment s = segments[segment.segmentId];
			if (exclusions.contains(segment.id)) continue;
			if ((s.flags & TransportSegment.TYPE_INVISIBLE) == 0) {
				StationView from = map.stations[segment.stationViewFromId];
				StationView to = map.stations[segment.stationViewToId];
				if (from.stationPoint != null && to.stationPoint != null) {
					SegmentView opposite = map.getSegmentView(to, from);
					ModelSpline additionalPoints = segment.spline;
					ModelSpline reversePoints = (opposite == null) ? null : opposite.spline;
					boolean additionalForward = additionalPoints != null;
					boolean additionalBackward = reversePoints != null;
					if (!additionalForward && additionalBackward) {
					} else {
						RenderElement element = new RenderSegment(map, segment, s);
						renderQueue.add(element);
						segmentIndex.put(segment, element);
						if (opposite != null) {
							exclusions.add(opposite.id);
						}
					}
				}
			}
		}
	}

	public void setVisibility(RectF viewport) {
		final int offset = 10;
		final Rect v = new Rect(
				(int) (viewport.left - offset),
				(int) (viewport.top - offset),
				(int) (viewport.right + offset),
				(int) (viewport.bottom + offset));
		final Rect[] bounds = mBounds;
		final RenderElement[] elements = mElements;
		final int count = bounds.length;
		final int[] types = mTypes;
		final ArrayList<RenderElement> elems = mClipping;
		elems.clear();
		for (int i = 0; i < count; i++) {
			if(  (types[i] & mRenderFilter)>0 &&  Rect.intersects(v, bounds[i])){
				elems.add(elements[i]);
			}
		}
		mElementsToRender = (RenderElement[]) elems.toArray(new RenderElement[elems.size()]);
	}
	
	public void setVisibilityTwice(RectF viewport1, RectF viewport2) {
		//final long startTime = System.currentTimeMillis();
		final int offset = 10;
		final Rect v1 = new Rect(
				(int) (viewport1.left - offset),
				(int) (viewport1.top - offset),
				(int) (viewport1.right + offset),
				(int) (viewport1.bottom + offset));
		final Rect v2 = new Rect(
				(int) (viewport2.left - offset),
				(int) (viewport2.top - offset),
				(int) (viewport2.right + offset),
				(int) (viewport2.bottom + offset));
		final ArrayList<RenderElement> elems = mClipping;
		elems.clear();
		final Rect[] bounds = mBounds;
		final RenderElement[] elements = mElements;
		final int[] types = mTypes;
		final int count = bounds.length;
		for (int i = 0; i < count; i++) {
			final Rect box = bounds[i];
			if( (types[i] & mRenderFilter)>0 && ( Rect.intersects(v1, box) || Rect.intersects(v2, box) )){
				elems.add(elements[i]);
			}
		}
		mElementsToRender = (RenderElement[]) elems.toArray(new RenderElement[elems.size()]);
		//final long endTime = System.currentTimeMillis();
		//Log.d("aMetro", "clipping time is " + (endTime-startTime) +", clipped " + mElementsToRender.length + "/" + mElements.length );
	}

	public void draw(Canvas canvas) {
		//final long startTime = System.currentTimeMillis();
		canvas.save();
		final RenderElement[] elements = mElementsToRender;
		final int count = elements.length;
		canvas.drawColor(Color.WHITE);
		for (int i = 0; i < count; i++) {
			elements[i].draw(canvas);
		}
		canvas.restore();
		//final long endTime = System.currentTimeMillis();
		//Log.d("aMetro", "drawing time is " + (endTime-startTime) );
	}
	
}
