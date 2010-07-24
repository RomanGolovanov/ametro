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
import android.util.Log;


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

	public static final int TYPE_LINE = 1;
	public static final int TYPE_TRANSFER_BACKGROUND = 2;
	public static final int TYPE_TRANSFER = 4;
	public static final int TYPE_STATION = 8;
	public static final int TYPE_STATION_NAME = 16;
	public static final int TYPE_BACKGROUND = 32;

	public static final int ONLY_TRANSPORT = TYPE_LINE | TYPE_TRANSFER_BACKGROUND | TYPE_TRANSFER | TYPE_STATION;
	public static final int ALL = ONLY_TRANSPORT | TYPE_STATION_NAME;

	private RenderElement[] mElements;
	private RenderElement[] mElementsToRender;
	private ArrayList<RenderElement> mClipping;
	private Rect[] mBounds;
	private int[] mTypes;
	private int mRenderFilter;

	private MapView mMapView;
	private ArrayList<RenderElement> mRenderQueue;

	private ClippingTreeNode mClippingRoot;
	private final static int CLIPPING_GRANULARITY = 50;

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
		final long startTime = System.currentTimeMillis();
		makeClippingTree();
		fillClippingTree();
		final long endTime = System.currentTimeMillis();
		Log.d("aMetro", "make clipping tree time is " + (endTime-startTime) );
	}

	private void fillClippingTree() {
		final int count = mElements.length;
		final Rect[] bounds = mBounds;
		final RenderElement[] elems = mElements;
		for (int i = 0; i < count; i++) {
			addClippingTreeElement(bounds[i], elems[i], mClippingRoot);
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

	private void makeClippingTree() {
		int width = mMapView.width;
		int height = mMapView.height;
		ClippingTreeNode root = new ClippingTreeNode(new Rect(0,0,width,height));
		makeClippingTreeNodes(root);
		mClippingRoot = root;
	}

	private static void makeClippingTreeNodes(ClippingTreeNode root) {
		final Rect clip = root.Clip;
		final int width = clip.width();
		final int height = clip.height();
		if(width<CLIPPING_GRANULARITY && height <CLIPPING_GRANULARITY)
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

	public void setVisibilityAll() {
		final RenderElement[] elements = mElements;
		final int count = elements.length;
		final int[] types = mTypes;
		final ArrayList<RenderElement> elems = mClipping;
		elems.clear();
		for (int i = 0; i < count; i++) {
			if(  (types[i] & mRenderFilter)>0){
				elems.add(elements[i]);
			}
		}
		mElementsToRender = (RenderElement[]) elems.toArray(new RenderElement[elems.size()]);
	}

	public void setVisibility(RectF viewport) {
		final int offset = 10;
		final Rect v = new Rect(
				(int) (viewport.left - offset),
				(int) (viewport.top - offset),
				(int) (viewport.right + offset),
				(int) (viewport.bottom + offset));
		ArrayList<RenderElement> preClipped = new ArrayList<RenderElement>(100);
		appendClipping(v,preClipped, mClippingRoot);
		mElementsToRender = (RenderElement[]) preClipped.toArray(new RenderElement[preClipped.size()]);
	}

	public void setVisibilityTwice(RectF viewport1, RectF viewport2) {
//		final long startTime = System.currentTimeMillis();

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

		ArrayList<RenderElement> preClipped = new ArrayList<RenderElement>(100);
		appendDoubleClipping(v1, v2, preClipped, mClippingRoot);
		mElementsToRender = (RenderElement[]) preClipped.toArray(new RenderElement[preClipped.size()]);

//		final long endTime = System.currentTimeMillis();
//		Log.d("aMetro", "clipping time is " + (endTime-startTime) +", clipped " + mElementsToRender.length + "/" + mElements.length );
	}

	public void draw(Canvas canvas) {
//		final long startTime = System.currentTimeMillis();
		canvas.save();
		final RenderElement[] elements = mElementsToRender;
		final int count = elements.length;
		canvas.drawColor(Color.WHITE);
		for (int i = 0; i < count; i++) {
			elements[i].draw(canvas);
		}
		canvas.restore();
//		final long endTime = System.currentTimeMillis();
//		Log.d("aMetro", "drawing time is " + (endTime-startTime) );
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
