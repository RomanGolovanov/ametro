package com.ametro.model;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.ametro.libs.Helpers;
import com.ametro.libs.TokenizedString;
import com.ametro.resources.FilePackage;
import com.ametro.resources.GenericResource;
import com.ametro.resources.MapAddiditionalLine;
import com.ametro.resources.MapLine;
import com.ametro.resources.MapResource;
import com.ametro.resources.TransportLine;
import com.ametro.resources.TransportResource;
import com.ametro.resources.TransportTransfer;
import com.ametro.resources.VectorResource;

public class ModelBuilder {

	public static Model Create(String libraryPath, String packageName, String mapName) throws IOException
	{
		FilePackage pkg = new FilePackage(libraryPath +"/"+ packageName+".pmz" );
		
		Date startTimestamp = new Date();
		
		GenericResource info = pkg.getCityGenericResource();
		MapResource map = pkg.getMapResource(mapName+".map" );
		VectorResource vec = pkg.getVectorResource(map.getVectorName());
		TransportResource trp = pkg.getTransportResource(map.getTransportName() != null ? map.getTransportName() : mapName+".trp");

		int size = map.getStationCount() + map.getAddiditionalStationCount();

		Model model = new Model(packageName, size);
		model.setCountryName(info.getValue("Options", "Country"));
		model.setCityName(info.getValue("Options", "Name"));
		model.setDimensions(vec.getWidth(),vec.getHeight());
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
				MapResource lineMap = pkg.getMapResource(tl.mMapName);
				ml = lineMap.getMapLines().get(tl.mName); 
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
		
		Log.d("aMetro", String.format("Overall data parsing is %sms", Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
		return model;
	}

	private static void fillTransfer(Model model, TransportTransfer t) {
		int fromStationId = model.getStationId(t.mStartLine, t.mStartStation);
		int toStationId = model.getStationId(t.mEndLine, t.mEndStation);
		int flags = 0;
		if(t.mStatus!=null && t.mStatus.contains("invisible")){
			flags = Model.EDGE_FLAG_INVISIBLE;
		}
		model.addTransferEdge(fromStationId, toStationId, t.mDelay, flags);
	}

	private static void fillAdditionalLines(Model model, MapAddiditionalLine al) {
		//int lineId = model.getLineId(al.mLineName);
		int fromId = model.getStationId(al.mLineName, al.mFromStationName);
		int toId = model.getStationId(al.mLineName, al.mToStationName);
		Point point = al.mPoint;
		if(al.mIsSpline){
			model.addSplineToEdge(fromId, toId, point);
		}else{
			model.addAdditionToEdge(fromId, toId, point);
		}
	}

	private static void fillMapLines(Model model, TransportLine tl, MapLine ml) throws IOException {
		String lineName = tl.mName;
		int lineId = model.addLine(lineName, (ml.linesColor | 0xFF000000));
		Log.v("aMetro", String.format("Loading transport line '%s'", lineName));
		if(ml.coordinates!=null){

			TokenizedString tDelays = new TokenizedString(tl.mDrivingDelaysText, ",()");
			TokenizedString tStations = new TokenizedString(tl.mStationText, ",()");
			Point[] points = ml.coordinates;
			Rect[] rects = ml.rectangles;
			final int rectCount  = rects!=null ? rects.length : 0;

			int mapStationIndex = 0;
			String fromStation = tStations.next();
			int fromStationId = model.addStation(lineId, lineName, fromStation, 
					rectCount > mapStationIndex ? rects[mapStationIndex] : null, 
					points[mapStationIndex]);
			boolean previousBrackedOpened = false;
			while(tStations.hasNext()){
				boolean bracketOpened = tStations.isBracketOpened();
				
				if(!bracketOpened && previousBrackedOpened){
					mapStationIndex++;
					fromStation = tStations.next();
					fromStationId = model.addStation(lineId, lineName, fromStation, 
							rectCount > mapStationIndex ? rects[mapStationIndex] : null, 
							points[mapStationIndex]);
					previousBrackedOpened = false;
					continue;
				}

				previousBrackedOpened = bracketOpened;
				String toStation = tStations.next();
				int toStationId;

				if(tStations.isBracketOpened()){
					toStationId = model.addStation(lineId, lineName, toStation);
				}else{
					mapStationIndex++;
					toStationId = model.addStation(lineId, lineName, toStation, 
							rectCount > mapStationIndex ? rects[mapStationIndex] : null, 
							points[mapStationIndex]);
				}
				
				if(!model.isExistEdge(fromStationId,toStationId)){
					Double delay = Helpers.parseNullableDouble( tDelays.next() );
					model.addLineEdge(fromStationId, toStationId, delay, lineId);
				}

				if(!tStations.isBracketOpened()){
					fromStation = toStation;
					fromStationId = toStationId;
				}
			}
		}
	}

}
