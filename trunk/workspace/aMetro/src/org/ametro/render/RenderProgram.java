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

package org.ametro.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.TransportSegment;
import org.ametro.model.TransportStation;
import org.ametro.model.TransportTransfer;
import org.ametro.model.ext.ModelSpline;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

public class RenderProgram {

	private static class ClippingTreeNode
	{
		public Rect Clip;
		public ArrayList<RenderElement> Elements;
		public ClippingTreeNode Left;
		public ClippingTreeNode Right;

		public ClippingTreeNode(Rect clip){
			this.Clip = clip;
			this.Elements= new ArrayList<RenderElement>();
			this.Left = null;
			this.Right = null;
		}
	}
	
	public static final int TYPE_LINE_DASHED = 0x00010000;
	public static final int TYPE_LINE = 0x00020000;
	public static final int TYPE_TRANSFER_BACKGROUND = 0x00040000;
	public static final int TYPE_TRANSFER = 0x00080000;
	public static final int TYPE_STATION = 0x000100000;
	public static final int TYPE_STATION_NAME = 0x000200000;
	public static final int TYPE_BACKGROUND = 0x000400000;

	public static final int ONLY_TRANSPORT = TYPE_LINE_DASHED | TYPE_LINE | TYPE_TRANSFER_BACKGROUND | TYPE_TRANSFER | TYPE_STATION;
	public static final int ALL = ONLY_TRANSPORT | TYPE_STATION_NAME;

	private static final int CLIPPING_OFFSET = 10;
	private static final int CLIPPING_TREE_GRANULARITY = 100;
	
	private int mRenderFilter;

	private SchemeView mMapView;
	
	private ArrayList<RenderElement> mElements;
	private ClippingTreeNode mClippingTree;

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

	public RenderProgram(SchemeView map) {
		mMapView = map;
		mElements = new ArrayList<RenderElement>();
		drawLines(map, mElements);
		drawTransfers(map, mElements);
		drawStations(map, mElements);
		updateRenderQueue();
		mRenderFilter = ALL;
	} 

	private void updateRenderQueue() {
		Collections.sort(mElements);
		Rect bounds = new Rect(0,0,mMapView.width,mMapView.height);
		mClippingTree = new ClippingTreeNode(bounds);
		makeClippingTreeNodes(mClippingTree);
		for (RenderElement elem : mElements) {
			addClippingTreeElement(elem.boundingBox, elem, mClippingTree);
		}
	}

	public void appendClipping(Rect v1, ArrayList<RenderElement> renderElements, ClippingTreeNode node){
		Rect clip = node.Clip;
		if(Rect.intersects(clip, v1)){
			for (RenderElement elem : node.Elements) {
				final Rect box = elem.boundingBox;
				final int type = elem.type;
				if( (type & mRenderFilter)>0 && Rect.intersects(v1, box) ){
					renderElements.add(elem);
				}
			}			
			if(node.Left!=null){
				appendClipping(v1, renderElements, node.Left);
				appendClipping(v1, renderElements, node.Right);
			}
		}
	}

	public void appendDoubleClipping(Rect v1, Rect v2, ArrayList<RenderElement> renderElements, ClippingTreeNode node){
		Rect clip = node.Clip;
		if(Rect.intersects(clip, v1) || Rect.intersects(clip, v2)){
			for (RenderElement elem : node.Elements) {
				final Rect box = elem.boundingBox;
				final int type = elem.type;
				if( (type & mRenderFilter)>0 && ( Rect.intersects(v1, box) || Rect.intersects(v2, box) )){
					renderElements.add(elem);
				}
			}			
			if(node.Left!=null){
				appendDoubleClipping(v1,v2, renderElements, node.Left);
				appendDoubleClipping(v1,v2, renderElements, node.Right);
			}
		}
	}

	private void addClippingTreeElement(Rect rect, RenderElement renderElement, ClippingTreeNode node) {
		if(node.Left==null || node.Right == null){
			node.Elements.add(renderElement);
		}else{
			final Rect left = node.Left.Clip;
			final Rect right = node.Right.Clip;

			if(left.contains(rect)){
				addClippingTreeElement(rect, renderElement, node.Left);
			}else if(right.contains(rect)){
				addClippingTreeElement(rect, renderElement, node.Right);
			}else{
				node.Elements.add(renderElement);
			}
		}
	}

	private static void makeClippingTreeNodes(ClippingTreeNode root) {
		final Rect clip = root.Clip;
		final int width = clip.width();
		final int height = clip.height();
		if(width<CLIPPING_TREE_GRANULARITY && height <CLIPPING_TREE_GRANULARITY)
			return;
		final int x = clip.left;
		final int y = clip.top;
		final Rect left = new Rect(clip);
		final Rect right = new Rect(clip);
		if(width>height){
			int half = x + width/2;
			left.right = half;
			right.left = half;
		}else{
			int half = y + height/2;
			left.bottom = half;
			right.top = half;
		}
		root.Left = new ClippingTreeNode(left);
		makeClippingTreeNodes(root.Left);
		root.Right = new ClippingTreeNode(right);
		makeClippingTreeNodes(root.Right);
	}

	public void setSelection(List<StationView> stations, List<SegmentView> segments, List<TransferView> transfers){
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

	private void drawStations(SchemeView map, ArrayList<RenderElement> renderQueue) {
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

	private void drawTransfers(SchemeView map, ArrayList<RenderElement> renderQueue) {
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

	private void drawLines(SchemeView map, ArrayList<RenderElement> renderQueue) {
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

	public ArrayList<RenderElement> setVisibilityAll() {
		return mElements;
	}

	public ArrayList<RenderElement> setVisibility(RectF viewport) {
		final Rect v = new Rect(
				(int) (viewport.left - CLIPPING_OFFSET),
				(int) (viewport.top - CLIPPING_OFFSET),
				(int) (viewport.right + CLIPPING_OFFSET),
				(int) (viewport.bottom + CLIPPING_OFFSET));
		ArrayList<RenderElement> clipping = new ArrayList<RenderElement>(100);
		appendClipping(v,clipping, mClippingTree);
		Collections.sort(clipping);
		return clipping;
	}

	public ArrayList<RenderElement> setVisibilityTwice(RectF viewport1, RectF viewport2) {
		final Rect v1 = new Rect(
				(int) (viewport1.left - CLIPPING_OFFSET),
				(int) (viewport1.top - CLIPPING_OFFSET),
				(int) (viewport1.right + CLIPPING_OFFSET),
				(int) (viewport1.bottom + CLIPPING_OFFSET));
		final Rect v2 = new Rect(
				(int) (viewport2.left - CLIPPING_OFFSET),
				(int) (viewport2.top - CLIPPING_OFFSET),
				(int) (viewport2.right + CLIPPING_OFFSET),
				(int) (viewport2.bottom + CLIPPING_OFFSET));

		ArrayList<RenderElement> clipping = new ArrayList<RenderElement>(100);
		appendDoubleClipping(v1, v2, clipping, mClippingTree);
		Collections.sort(clipping);
		return clipping;
	}

	public static int getGrayedColor(int color) {
		if(color == Color.BLACK){
			return 0xFFd0d0d0;
		}
		float r = (float)Color.red(color) / 255;
		float g = (float)Color.green(color) / 255;
		float b = (float)Color.blue(color) / 255;
		float t = 0.8f;
		r = r*(1-t) + 1.0f * t;
		g = g*(1-t) + 1.0f * t;
		b = b*(1-t) + 1.0f * t;
		return Color.argb(0xFF, (int)Math.min(r * 255,255), (int)Math.min(g * 255,255), (int)Math.min(b * 255,255));
	}

}
