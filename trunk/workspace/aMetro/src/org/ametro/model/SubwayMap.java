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

import android.graphics.Point;

import java.util.HashMap;

public class SubwayMap {

	public static final int VERSION = 1;

	public int id;

	public long timestamp;
	public long crc;

	public String mapName;

	public String cityName;
	public String countryName;

	public int width;
	public int height;

	public int stationDiameter;
	public int linesWidth;
	public boolean wordWrap;
	public boolean upperCase;

	public long sourceVersion;

	public SubwayLine[] lines;
	public SubwayStation[] stations;
	public SubwaySegment[] segments;
	public HashMap<Integer, SubwaySegment[]> segmentsByStationId;
	public SubwayTransfer[] transfers;
	public HashMap<Integer, Point[]> pointsBySegmentId;

	private HashMap<Long, SubwaySegment> segmentsIndexed;
	private HashMap<Integer, SubwayLine> linesIndexed;

	public SubwayMap() {
	}

	public SubwayMap(int newId, String newMapName) {
		id = newId;
		mapName = newMapName;
	}

	public Point[] getSegmentsNodes(int segmentId) {
		if (pointsBySegmentId != null) {
			return pointsBySegmentId.get(segmentId);
		} else {
			return null;
		}
	}

	public boolean hasConnections(SubwayStation station) {
		if (station != null) {
			SubwaySegment[] stationSegments = segmentsByStationId
					.get(station.id);
			if (stationSegments != null) {
				for (SubwaySegment segment : stationSegments) {
					Double delay = segment.delay;
					if (delay != null && delay != 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public SubwaySegment getSegment(SubwayStation from, SubwayStation to) {
		if (to != null && from != null) {
			if (segmentsIndexed == null) {
				segmentsIndexed = new HashMap<Long, SubwaySegment>();
				for (SubwaySegment segment : segments) {
					long segFromId = segment.fromStationId;
					long segToId = segment.toStationId;
					segmentsIndexed.put((segFromId << 32) + segToId, segment);
				}
			}
			long fromId = from.id;
			long toId = to.id;
			return segmentsIndexed.get( (fromId << 32) + toId );
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o != null && getClass() == o.getClass()
				&& id == ((SubwayMap) o).id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public SubwayLine getLine(int lineId) {
		if(linesIndexed == null){
			linesIndexed = new HashMap<Integer, SubwayLine>();
			for(SubwayLine line : lines){
				linesIndexed.put(line.id, line);
			}
		}
		return linesIndexed.get(lineId);
	}

}
