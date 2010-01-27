package com.ametro.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Color;
import android.util.Log;

import com.ametro.MapSettings;

import com.ametro.libs.Helpers;
import com.ametro.libs.DelaysString;
import com.ametro.libs.StationsString;
import com.ametro.pmz.FilePackage;
import com.ametro.pmz.GenericResource;
import com.ametro.pmz.MapResource;
import com.ametro.pmz.TransportResource;
import com.ametro.pmz.MapResource.MapAddiditionalLine;
import com.ametro.pmz.MapResource.MapLine;
import com.ametro.pmz.TransportResource.TransportLine;
import com.ametro.pmz.TransportResource.TransportTransfer;

public class MapBuilder {

	public static Model loadModel(String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream strm = null;
		try{
			strm = new ObjectInputStream( new FileInputStream(fileName) );
			return (Model)strm.readObject();
		} finally{
			if(strm!=null){
				strm.close();
			}
		}
	}

	public static void saveModel(Model model) throws IOException {
		String fileName = MapSettings.getMapFileName(model.getMapName());
		ObjectOutputStream strm = new ObjectOutputStream(new FileOutputStream(fileName));
		strm.writeObject(model);
		strm.flush();
		strm.close();
	}


	public static Model ImportPmz(String libraryPath, String packageName, String mapName) throws IOException
	{
		FilePackage pkg = new FilePackage(libraryPath +"/"+ packageName+".pmz" );

		Date startTimestamp = new Date();

		GenericResource info = pkg.getCityGenericResource();
		MapResource map = pkg.getMapResource(mapName+".map" );
		TransportResource trp = pkg.getTransportResource(map.getTransportName() != null ? map.getTransportName() : mapName+".trp");

		int size = map.getStationCount() + map.getAddiditionalStationCount();

		Model model = new Model(packageName, size);
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
			MapResource.MapLine ml = mapLines.get(tl.mName);
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

	private static void calculateDimensions(Model model){
		android.graphics.Rect bounds = new android.graphics.Rect(0,0,0,0);
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
					bounds.union(r.left, r.top, r.right, r.bottom);
				}				
			}
		}
		model.setDimension(bounds.width()+100, bounds.height()+100);
	}

	private static void fillTransfer(Model model, TransportTransfer t) {
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

	private static void fillAdditionalLines(Model model, MapAddiditionalLine al) {
		Line line = model.getLine(al.mLineName);
		Station from = model.getStation(al.mLineName, al.mFromStationName);
		Station to = model.getStation(al.mLineName, al.mToStationName);
		if(from!=null && to!=null){
			Segment segment = line.getSegment(from,to);
			if(segment!=null && segment.getAdditionalNodes() == null){
				Point[] points = Helpers.convertPoints(al.mPoints);
				segment.setAdditionalNodes(points);
				if(al.mIsSpline){
					segment.addFlag(Segment.SPLINE);
				}
			}
		}
	}

	private static final Point zeroPoint = new Point(0,0);
	private static final Rect zeroRect = new Rect(0,0,0,0);

	private static Point getPoint(Point[] array, int index){
		if(array == null) return null;
		return index >= array.length ? null : ( !zeroPoint.equals(array[index]) ? array[index] : null); 
	}

	private static Rect getRect(Rect[] array, int index){
		if(array == null) return null;
		return index >= array.length ? null : ( !zeroRect.equals(array[index]) ? array[index] : null); 
	}



	private static void fillMapLines(Model model, TransportLine tl, MapResource.MapLine ml) throws IOException {
		String lineName = tl.mName;
		int lineColor = ml.linesColor | 0xFF000000;
		int labelColor = ml.labelColor > 0 ? ml.labelColor | 0xFF000000 : lineColor;
		int labelBgColor = ml.backgroundColor;
		if(labelBgColor != -1){
			labelBgColor = labelBgColor == 0 ? Color.WHITE : labelBgColor | 0xFF000000;
		}else{
			labelBgColor = 0;
		}

		Line line = model.addLine(lineName,lineColor, labelColor, labelBgColor);
		if(ml.coordinates!=null){

			DelaysString tDelays = new DelaysString(tl.mDrivingDelaysText);
			StationsString tStations = new StationsString(tl.mStationText);
			Point[] points = Helpers.convertPoints(ml.coordinates);
			Rect[] rects = Helpers.convertRects(ml.rectangles);

			int stationIndex = 0;

			Station toStation = null;
			Double toDelay = null;

			Station fromStation = null; 
			Double fromDelay = null;

			Station thisStation = line.invalidateStation(
					tStations.next(), 
					getRect(rects, stationIndex), 
					getPoint(points,stationIndex));

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
								line.addSegment(thisStation, bracketedStation, delays.length <= idx ? null : delays[idx] );
							}else{
								line.addSegment(bracketedStation, thisStation, delays.length <= idx ? null : delays[idx] );
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
							getRect(rects, stationIndex), 
							getPoint(points,stationIndex));

				}else{

					stationIndex++;
					toStation = line.invalidateStation(tStations.next(), 
							getRect(rects, stationIndex), 
							getPoint(points,stationIndex));

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
