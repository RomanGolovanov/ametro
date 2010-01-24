package com.ametro.model;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.ametro.libs.DelaysString;
import com.ametro.libs.StationsString;
import com.ametro.resources.FilePackage;
import com.ametro.resources.GenericResource;
import com.ametro.resources.MapAddiditionalLine;
import com.ametro.resources.MapLine;
import com.ametro.resources.MapResource;
import com.ametro.resources.TransportLine;
import com.ametro.resources.TransportResource;
import com.ametro.resources.TransportTransfer;

public class TransportMapBuilder {

	public static TransportMap Create(String libraryPath, String packageName, String mapName) throws IOException
	{
		FilePackage pkg = new FilePackage(libraryPath +"/"+ packageName+".pmz" );

		Date startTimestamp = new Date();

		GenericResource info = pkg.getCityGenericResource();
		MapResource map = pkg.getMapResource(mapName+".map" );
		TransportResource trp = pkg.getTransportResource(map.getTransportName() != null ? map.getTransportName() : mapName+".trp");

		int size = map.getStationCount() + map.getAddiditionalStationCount();

		TransportMap model = new TransportMap(packageName, size);
		model.setCountryName(info.getValue("Options", "Country"));
		model.setCityName(info.getValue("Options", "Name"));
		model.setLinesWidth(map.getLinesWidth());
		model.setStationDiameter(map.getStationDiameter());
		model.setWordWrap(map.isWordWrap());
		model.setUpperCase(map.isUpperCase());

		Hashtable<String, MapLine> mapLines = map.getMapLines();
		Hashtable<String, TransportLine> transportLines = trp.getLines();

		Iterator<TransportLine> lines = transportLines.values().iterator();
		while(lines.hasNext()){
			TransportLine tl = lines.next();
			MapLine ml = mapLines.get(tl.mName);
			if(ml == null && tl.mName!=null) {
				continue;
				//MapResource lineMap = pkg.getMapResource(tl.mMapName);
				//ml = lineMap.getMapLines().get(tl.mName); 
			}
			fillMapLines( model, tl, ml);
		}

		Iterator<TransportTransfer> transfers = trp.getTransfers().iterator();
		while(transfers.hasNext()){
			TransportTransfer t = transfers.next();
			fillTransfer(model, t);

		}


		Iterator<MapAddiditionalLine> additionalLines = map.getAddiditionalLines().iterator();
		while(additionalLines.hasNext()){
			MapAddiditionalLine al = additionalLines.next();
			fillAdditionalLines(model, al);
		}

		calculateDimensions(model);

		Log.d("aMetro", String.format("Overall data parsing is %sms", Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
		return model;
	}

	private static void calculateDimensions(TransportMap model){
		Rect bounds = new Rect(0,0,0,0);
		Enumeration<Line> lines = model.getLines();
		while(lines.hasMoreElements()){
			Line line = lines.nextElement();
			Enumeration<Station> stations = line.getStations();
			while(stations.hasMoreElements()){
				Station station = stations.nextElement();
				Point p = station.getPoint();
				if(p!=null){
					bounds.union(p.x, p.y);
				}
				Rect r = station.getRect();
				if(r!=null){
					bounds.union(r);
				}				
			}
		}
		model.setDimension(bounds.width()+100, bounds.height()+100);
	}

	private static void fillTransfer(TransportMap model, TransportTransfer t) {
		Station from = model.getStation(t.mStartLine, t.mStartStation);
		Station to = model.getStation(t.mEndLine, t.mEndStation);
		int flags = 0;
		if(t.mStatus!=null && t.mStatus.contains("invisible")){
			flags = Transfer.INVISIBLE;
		}
		if(from!=null && to!=null){
			model.addTransfer(from,to,t.mDelay, flags);
		}
	}

	private static void fillAdditionalLines(TransportMap model, MapAddiditionalLine al) {
		Line line = model.getLine(al.mLineName);
		Station from = model.getStation(al.mLineName, al.mFromStationName);
		Station to = model.getStation(al.mLineName, al.mToStationName);
		if(from!=null && to!=null){
			Segment segment = line.getSegment(from,to);
			if(segment!=null){
				Point[] points = al.mPoints;
				segment.setAdditionalNodes(points);
				if(al.mIsSpline){
					segment.setFlags(Segment.SPLINE);
				}
			}
		}
	}

	private static void fillMapLines(TransportMap model, TransportLine tl, MapLine ml) throws IOException {
		String lineName = tl.mName;
		Line line = model.addLine(lineName,(ml.linesColor | 0xFF000000));
		if(ml.coordinates!=null){

			DelaysString tDelays = new DelaysString(tl.mDrivingDelaysText);
			StationsString tStations = new StationsString(tl.mStationText);
			Point[] points = ml.coordinates;
			Rect[] rects = ml.rectangles;
			final int rectCount  = rects!=null ? rects.length : 0;

			int stationIndex = 0;

			Station toStation = null;
			Double toDelay = null;

			Station fromStation = null;
			Double fromDelay = null;

			Station thisStation = line.invalidateStation(
					tStations.next(), 
					rectCount > stationIndex ? rects[stationIndex] : null, 
							points[stationIndex]);

			do{

				if("(".equals(tStations.getNextDelimeter())){
					int idx = 0;
					Double[] delays = tDelays.nextBracket(); 
					while(tStations.hasNext() && !")".equals(tStations.getNextDelimeter())){
						boolean isForwardDirection = true;
						String bracketedStationName = tStations.next();
						if(bracketedStationName.startsWith("-")){
							bracketedStationName = bracketedStationName.substring(1);
							isForwardDirection = !isForwardDirection;
						}

						if(bracketedStationName!=null && bracketedStationName.length() > 0 ){
							Station bracketedStation = line.invalidateStation(bracketedStationName);
							if(isForwardDirection){
								line.addSegment(thisStation, bracketedStation, delays[idx]);
							}else{
								line.addSegment(bracketedStation, thisStation, delays[idx]);
							}
						}
						idx++;
					}

					fromStation = thisStation;

					fromDelay = null;
					toDelay = null;

					if(!tStations.hasNext()){
						break;
					}

					stationIndex++;
					thisStation = line.invalidateStation(
							tStations.next(), 
							rectCount > stationIndex ? rects[stationIndex] : null, 
									points[stationIndex]);

				}else{

					stationIndex++;
					toStation = line.invalidateStation(tStations.next(), 
							rectCount > stationIndex ? rects[stationIndex] : null, 
									points[stationIndex]);

					if(tDelays.beginBracket()){
						Double[] delays = tDelays.nextBracket();
						toDelay = delays[0];
						fromDelay = delays[1];
					}else{
						toDelay = tDelays.next();
					}

					if(fromStation!=null && line.getSegment(thisStation, fromStation)==null)
					{
						if(fromDelay == null){
							Segment opposite = line.getSegment(fromStation, thisStation);
							fromDelay = opposite!=null ? opposite.getDelay() : null;
						}
						line.addSegment(thisStation, fromStation, fromDelay);
					}
					if(toStation!=null && line.getSegment(thisStation, toStation)==null)
					{
						line.addSegment(thisStation, toStation, toDelay);
					}


					fromStation = thisStation;

					fromDelay = toDelay;
					toDelay = null;

					thisStation = toStation;
				}

			}while(tStations.hasNext());
		}
	}

}
