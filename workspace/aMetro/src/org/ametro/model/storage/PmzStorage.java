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
package org.ametro.model.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ametro.model.LineView;
import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.TransportLine;
import org.ametro.model.TransportMap;
import org.ametro.model.TransportSegment;
import org.ametro.model.TransportStation;
import org.ametro.model.TransportTransfer;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.ext.ModelRect;
import org.ametro.model.ext.ModelSpline;
import org.ametro.model.util.CountryLibrary;
import org.ametro.model.util.IniStreamReader;
import org.ametro.model.util.ModelUtil;
import org.ametro.util.StringUtil;

public class PmzStorage implements IModelStorage {

	private static final String ENCODING = "windows-1251";

	public Model loadModel(String fileName, Locale locale) {
		PmzImporter importer = new PmzImporter(fileName,false);
		try {
			Model model = importer.getModel();
			model.setLocale(locale);
			return model;
		} catch (IOException e) {
			return null;
		}	
	}

	public Model loadModelDescription(String fileName, Locale locale) {
		PmzImporter importer = new PmzImporter(fileName,true);
		try {
			Model model = importer.getModel();
			model.setLocale(locale);
			return model;
		} catch (IOException e) {
			return null;
		}	
	}

	public boolean saveModel(String fileName, Model model) {
		throw new NotImplementedException();
	}

	public void loadModelLocale(String fileName, Model model, Locale locale) {
		throw new NotImplementedException();
	}

	private static class PmzImporter {

		private File mFile;
		private ZipFile mZipFile;
		private Model mModel;
		private boolean mDescriptionOnly;

		private String mCityFile = null;
		private ArrayList<String> mTrpFiles = new ArrayList<String>(); 
		private ArrayList<String> mMapFiles = new ArrayList<String>(); 
		private ArrayList<String> mTxtFiles = new ArrayList<String>();
		private ArrayList<String> mImgFiles = new ArrayList<String>();

		private ArrayList<TransportMap> mTransportMaps = new ArrayList<TransportMap>();

		private ArrayList<TransportLine> mTransportLines = new ArrayList<TransportLine>();
		private ArrayList<TransportStation> mTransportStations = new ArrayList<TransportStation>();
		private ArrayList<TransportSegment> mTransportSegments = new ArrayList<TransportSegment>();
		private ArrayList<TransportTransfer> mTransportTransfers = new ArrayList<TransportTransfer>();

		private ArrayList<MapView> mMapViews = new ArrayList<MapView>();

		private HashMap<String,TransportStation> stationIndex = new HashMap<String,TransportStation>();
		private HashMap<String,TransportLine> lineIndex = new HashMap<String, TransportLine>();
		private HashMap<String,TransportMap> mapIndex = new HashMap<String, TransportMap>();

		private ArrayList<String> mTexts = new ArrayList<String>();

		private int[] getMapsNumbers(String[] maps) {
			ArrayList<Integer> res = new ArrayList<Integer>();
			for(String mapSystemName : maps){
				TransportMap map = mapIndex.get(mapSystemName);
				if(map!=null){
					res.add(map.id);
				}
			}
			return ModelUtil.toIntArray(res);
		}

		private TransportStation getStation(String lineSystemName, String stationSystemName)
		{
			String key = lineSystemName + "\\" + stationSystemName;
			return stationIndex.get(key);
		}

		private void updateStationIndex(TransportLine line){
			final String lineSystemName = line.systemName;
			for(int id : line.stations){
				TransportStation station = mTransportStations.get(id); 
				String key = lineSystemName + "\\" + station.systemName;
				stationIndex.put(key, station);
			}
		}

		private int[] appendTextArray(String[] txt){
			if(txt == null) return null;
			final int len = txt.length;
			int[] r = new int[len];
			int base = mTexts.size();
			for(int i = 0; i < len; i++){
				r[i] = base + i;
				mTexts.add(txt[i]);
			}
			return r;
		}

		private int appendLocalizedText(String txt){
			int pos = mTexts.size();
			mTexts.add(txt);
			return pos;
		}

		public PmzImporter(String fileName, boolean descriptionOnly){
			mFile = new File(fileName);
			mModel = null;
			mDescriptionOnly = descriptionOnly;
		}

		public void execute() throws IOException{
			mZipFile = null;
			try{
				mZipFile = new ZipFile(mFile, ZipFile.OPEN_READ);
				mModel = new Model();
				findModelFiles(); // find map files in archive
				importCityFile(); // load data from .cty file - map description
				if(!mDescriptionOnly) { 
					importTrpFiles(); // load data from .trp files 
					importMapFiles(); // load data from .map files
					importTxtFiles(); // load data from .txt files
				}
				makeModel(); // make model from imported data
			}finally{
				if(mZipFile!=null){
					mZipFile.close();
				}
			}
		}

		private void importTxtFiles() {
			// TODO Auto-generated method stub

		}

		private void importMapFiles() throws IOException {
			final Model model = mModel;

			final ArrayList<LineView> lines = new ArrayList<LineView>();
			final ArrayList<StationView> stations = new ArrayList<StationView>();
			final HashMap<Long, ModelSpline> additionalNodes = new HashMap<Long, ModelSpline>();
			final HashMap<Integer,Integer> stationViews = new HashMap<Integer, Integer>();
			final HashMap<Integer,Integer> lineViewIndex = new HashMap<Integer, Integer>(); 

			for(String fileName : mMapFiles){ // for each .map file in catalog
				InputStream stream = mZipFile.getInputStream(mZipFile.getEntry(fileName));
				IniStreamReader ini = new IniStreamReader(new InputStreamReader(stream, ENCODING)); // access as INI file

				MapView view = new MapView();
				view.id = mMapViews.size();
				view.systemName = fileName;
				view.stationDiameter = 11;
				view.lineWidth = 9;
				view.isUpperCase = true;
				view.isWordWrap = true;
				view.isVector = true;
				view.owner = model;
				mMapViews.add(view);

				TransportLine line = null;
				LineView lineView = null;

				lines.clear();
				stations.clear();
				additionalNodes.clear();
				stationViews.clear();
				lineViewIndex.clear();

				ModelPoint[] coords = null;
				ModelRect[] rects = null;
				Integer[] heights = null;

				while(ini.readNext()){ // for each key in file
					final String key = ini.getKey(); // extract current properties
					final String value = ini.getValue();
					final String section = ini.getSection();
					final boolean isSectionChanged = ini.isSectionChanged(); 

					if(lineView!=null && isSectionChanged){
						makeStationViews(line, lineView, stationViews, stations, coords, rects, heights);
						lineView = null;
						line = null;
						coords = null;
						rects = null;
						heights = null;						
					}

					if(section.startsWith("Options")){ // for line sections
						if(key.equalsIgnoreCase("ImageFileName")){ // store line name parameter
							view.backgroundSystemName = value;
						}else if(key.equalsIgnoreCase("StationDiameter")){
							view.stationDiameter = StringUtil.parseInt(value, view.stationDiameter);
						}else if(key.equalsIgnoreCase("LinesWidth")){
							view.lineWidth = StringUtil.parseInt(value, view.lineWidth);
						}else if(key.equalsIgnoreCase("UpperCase")){
							view.isUpperCase = StringUtil.parseBoolean(value, view.isUpperCase);
						}else if(key.equalsIgnoreCase("WordWrap")){
							view.isWordWrap = StringUtil.parseBoolean(value, view.isWordWrap);
						}else if(key.equalsIgnoreCase("IsVector")){
							view.isVector = StringUtil.parseBoolean(value, view.isVector);
						}else if(key.equalsIgnoreCase("Transports")){
							view.transports = getMapsNumbers(StringUtil.parseStringArray(value));
						}else if(key.equalsIgnoreCase("CheckedTransports")){
							view.transportsChecked = getMapsNumbers(StringUtil.parseStringArray(value));
						}			
					}else if(section.equalsIgnoreCase("AdditionalNodes")){
						String[] parts = StringUtil.parseStringArray(value);
						String lineSystemName = parts[0];
						TransportStation from = getStation(lineSystemName, parts[1]);
						TransportStation to = getStation(lineSystemName, parts[2]);
						if(from!=null && to!=null){
							boolean isSpline = false;
							int pos = 3;
							final ArrayList<ModelPoint> points = new ArrayList<ModelPoint>();
							while (pos < parts.length) {
								if (parts[pos].contains("spline")) {
									isSpline = true;
									break;
								} else {
									ModelPoint p = new ModelPoint(StringUtil.parseInt(parts[pos],0), StringUtil.parseInt(parts[pos + 1],0) );
									points.add(p);
									pos += 2;
								}
							}
							final ModelSpline spline = new ModelSpline();
							spline.isSpline = isSpline;
							spline.points = (ModelPoint[]) points.toArray(new ModelPoint[points.size()]);
							long nodeKey = Model.getSegmentKey(from.id, to.id);
							additionalNodes.put(nodeKey, spline);

						}

					}else{
						if(isSectionChanged){
							line = lineIndex.get(section);
							if(line!=null){
								//lineNumbers.add(line.id);
								lineView = new LineView();
								lineView.id = lines.size();
								lineView.lineId = line.id;
								lineView.owner = model;
								lines.add(lineView);
								lineViewIndex.put(line.id, lineView.id);
							}
						}
						if(lineView!=null){					
							if(key.equalsIgnoreCase("Color")){ // store line name parameter
								lineView.lineColor = StringUtil.parseColor(value);
							}else if(key.equalsIgnoreCase("LabelsColor")){
								lineView.labelColor = StringUtil.parseColor(value);
							}else if(key.equalsIgnoreCase("Coordinates")){
								coords = StringUtil.parseModelPointArray(value);
							}else if(key.equalsIgnoreCase("Rects")){
								rects = StringUtil.parseModelRectArray(value);
							}else if(key.equalsIgnoreCase("Heights")){
								heights = StringUtil.parseIntegerArray(value);
							}else if(key.equalsIgnoreCase("Rect")){
								lineView.lineNameRect = StringUtil.parseModelRect(value);
							}			
						}

					}

				}
				// finalize map view
				if(lineView!=null){
					makeStationViews(line, lineView, stationViews, stations, coords, rects, heights);
				}
				view.lines = (LineView[]) lines.toArray(new LineView[lines.size()]);
				view.stations = (StationView[]) stations.toArray(new StationView[lines.size()]);
				view.segments = makeSegmentViews(view, lineViewIndex, stationViews, additionalNodes);
				view.transfers = makeTransferViews(view, stationViews);

				fixViewDimensions(view);
			}
		}

		private void fixViewDimensions(MapView view) {
			ModelRect mapRect = ModelUtil.getDimensions(view.stations);

			int xmin = mapRect.left;
			int ymin = mapRect.top;
			int xmax = mapRect.right;
			int ymax = mapRect.bottom;

			int dx = 50 - xmin;
			int dy = 50 - ymin;

			for (StationView station : view.stations) {
				ModelPoint p = station.stationPoint;
				if (p != null && !p.isZero() ) {
					p.offset(dx, dy);
				}
				ModelRect r = station.stationNameRect;
				if (r != null && !r.isZero() ) {
					r.offset(dx, dy);
				}
			}
			for (SegmentView segment : view.segments) {
				ModelSpline spline = segment.spline;
				if (spline != null) {
					ModelPoint[] points = spline.points;
					for (ModelPoint point : points) {
						point.offset(dx, dy);
					}
				}

			}
			view.width = xmax - xmin + 100;
			view.height = ymax - ymin + 100;
		}

		private TransferView[] makeTransferViews(MapView view, HashMap<Integer,Integer> stationViewIndex) {
			final Model model = mModel;
			final ArrayList<TransferView> transfers = new ArrayList<TransferView>();
			int base = 0;
			for(TransportTransfer transfer: mTransportTransfers){
				Integer fromId = stationViewIndex.get(transfer.stationFromId);
				Integer toId = stationViewIndex.get(transfer.stationToId);
				if(fromId!=null && toId!=null){
					final TransferView v = new TransferView();
					v.id = base++;
					v.transferId = transfer.id;
					v.stationViewFromId = fromId;
					v.stationViewToId = toId;
					v.owner = model;
					transfers.add(v);
				}
			}
			return (TransferView[]) transfers.toArray(new TransferView[transfers.size()]);	
		}

		private SegmentView[] makeSegmentViews(MapView view, HashMap<Integer,Integer> lineViewIndex, HashMap<Integer,Integer> stationViewIndex, HashMap<Long,ModelSpline> additionalNodes) {
			final Model model = mModel;
			final ArrayList<SegmentView> segments = new ArrayList<SegmentView>();
			int base = 0;
			for(TransportSegment segment: mTransportSegments){
				Integer fromId = stationViewIndex.get(segment.stationFromId);
				Integer toId = stationViewIndex.get(segment.stationToId);
				boolean visibleStations = fromId!=null && toId!=null;

				if(visibleStations && ( (segment.flags & TransportSegment.TYPE_INVISIBLE) == 0)){
					final SegmentView segmentView = new SegmentView();
					segmentView.id = base++;
					segmentView.lineViewId = lineViewIndex.get(segment.lineId);
					segmentView.segmentId = segment.id;
					segmentView.stationViewFromId = fromId;
					segmentView.stationViewToId = toId;

					segmentView.owner = model;

					long nodeKey = Model.getSegmentKey(segment.stationFromId, segment.stationToId);
					ModelSpline spline = additionalNodes.get(nodeKey);

					if(spline!=null){
						segmentView.spline = spline;
					}else{

					}
					segmentView.spline = null;
					segments.add(segmentView);
				}
			}
			return (SegmentView[]) segments.toArray(new SegmentView[segments.size()]);
		}


		private void makeStationViews(TransportLine line, LineView lineView, HashMap<Integer,Integer> stationViewIndex, ArrayList<StationView> stationViews, ModelPoint[] coords, ModelRect[] rects, Integer[] heights) {
			final int stationsCount = line.stations.length;
			final int pointsCount = coords!=null ? coords.length : 0;
			final int rectsCount = rects!=null ? rects.length : 0;
			final int heightsCount = heights!=null ? heights.length : 0;
			final int[] stations = line.stations;
			int base = stationViews.size();

			for(int i = 0; i < stationsCount && i < pointsCount; i++){
				final StationView v = new StationView();
				v.id = base++;
				v.owner = mModel;
				v.lineViewId = lineView.id;
				v.stationId = stations[i];
				v.stationPoint = coords[i];
				v.stationNameRect = (i < rectsCount) ? rects[i] : null;
				v.stationHeight = (i < heightsCount) ? heights[i] : null;
				stationViews.add(v);
				stationViewIndex.put(v.stationId, v.id);
			}


		}

		private void importTrpFiles() throws IOException {
			for(String fileName : mTrpFiles){ // for each .trp file in catalog
				InputStream stream = mZipFile.getInputStream(mZipFile.getEntry(fileName));
				IniStreamReader ini = new IniStreamReader(new InputStreamReader(stream, ENCODING)); // access as INI file

				TransportMap map = new TransportMap(); // create new transport map
				map.id = mTransportMaps.size();
				map.owner = mModel;
				mTransportMaps.add(map);
				map.systemName = fileName; // setup transport map internal name
				mapIndex.put(map.systemName, map);

				TransportLine line = null; // create loop variables
				String stationList = null; // storing driving data
				String aliasesList = null;
				String drivingList = null; // for single line
				String lineName = null;

				while(ini.readNext()){ // for each key in file
					final String key = ini.getKey(); // extract current properties
					final String value = ini.getValue();
					final String section = ini.getSection();
					final boolean isSectionChanged = ini.isSectionChanged(); 

					if(line!=null && isSectionChanged){ // if end of line
						line.name  = appendLocalizedText(lineName);
						makeLineObjects(line, stationList, drivingList, aliasesList); // make station and segments
						line = null; // clear loop variables
						stationList = null;
						drivingList = null;
						aliasesList = null;
						lineName = null;
					}
					if(section.startsWith("Line")){ // for line sections
						if(isSectionChanged){
							line = new TransportLine(); // at start - create new line object
							line.id = mTransportLines.size();
							line.owner = mModel;
							mTransportLines.add(line);
						}
						if(key.equalsIgnoreCase("Name")){ // store line name parameter
							if(lineName==null){
								lineName = value;
							}
							line.systemName = value;
						}else if(key.equalsIgnoreCase("LineMap")){
							line.lineMapName = value;
						}else if(key.equalsIgnoreCase("Stations")){
							stationList = value;
						}else if(key.equalsIgnoreCase("Driving")){
							drivingList = value;
						}else if(key.equalsIgnoreCase("Delays")){
							line.delays = StringUtil.parseDelayArray(value);
						}else if(key.equalsIgnoreCase("Alias")){
							lineName = value;
						}else if(key.equalsIgnoreCase("Aliases")){
							lineName = value;
						}			

					}else if(section.equalsIgnoreCase("Transfers")){
						makeTransfer(value);
					}else if (section.equalsIgnoreCase("AdditionalInfo")){
						// do nothing at this time
					}else if (section.equalsIgnoreCase("Options")){
						if(key.equalsIgnoreCase("Type")){
							map.typeName = appendLocalizedText(value);
						}
					}
				}
				if(line!=null){ // if end of line 
					makeLineObjects(line, stationList, drivingList, aliasesList); // make station and segments
				}

			}
		}


		private void importCityFile() throws IOException {
			String city = null;
			String country = null;
			final ArrayList<String> authors = new ArrayList<String>();
			final ArrayList<String> comments = new ArrayList<String>();
			String[] delays = null;

			InputStream stream = mZipFile.getInputStream(mZipFile.getEntry(mCityFile));
			final IniStreamReader ini = new IniStreamReader(new InputStreamReader(stream, ENCODING));
			while(ini.readNext()){
				final String key = ini.getKey();
				final String value = ini.getValue();
				if(key.equalsIgnoreCase("RusName")){
					city = value;
				}else if(key.equalsIgnoreCase("Country")){
					country = value;
				}else if(key.equalsIgnoreCase("MapAuthors")){
					authors.add(value);
				}else if(key.equalsIgnoreCase("Comment")){
					comments.add(value);
				}else if(key.equalsIgnoreCase("DelayNames")){
					delays = value.split(",");
				}
			}
			final Model m = mModel;
			m.cityName = appendLocalizedText(city);
			m.countryName = appendLocalizedText(country);
			m.authors = appendTextArray((String[]) authors.toArray(new String[authors.size()]));
			m.comments = appendTextArray((String[]) comments.toArray(new String[comments.size()]));
			m.delays = appendTextArray(delays);
		}


		private void makeModel() {
			final Model model = mModel;
			// fill model fields
			model.maps = (TransportMap[]) mTransportMaps.toArray(new TransportMap[mTransportMaps.size()]);
			model.lines = (TransportLine[]) mTransportLines.toArray(new TransportLine[mTransportLines.size()]);
			model.stations = (TransportStation[]) mTransportStations.toArray(new TransportStation[mTransportStations.size()]);
			model.segments = (TransportSegment[]) mTransportSegments.toArray(new TransportSegment[mTransportSegments.size()]);
			model.transfers = (TransportTransfer[]) mTransportTransfers.toArray(new TransportTransfer[mTransportTransfers.size()]);
			model.views = (MapView[]) mMapViews.toArray(new MapView[mMapViews.size()]);

			model.systemName = mFile.getName();

			model.fileSystemName = mFile.getAbsolutePath();
			model.timestamp = mFile.lastModified();

			makeGlobalization();
		}

		private void makeGlobalization() {
			// prepare 
			final ArrayList<String> localeList = new ArrayList<String>();
			final ArrayList<String[]> textList = new ArrayList<String[]>();
			final String[] originalTexts = mTexts.toArray(new String[mTexts.size()]);
			final String originalLocale = determineLocale(originalTexts);

			// locate country info
			final String country = originalTexts[mModel.countryName];
			final String city = originalTexts[mModel.cityName];
			CountryLibrary.CountryLibraryRecord info = CountryLibrary.search(country,city);
			mModel.location = info!=null ? info.Location : null;

			// make localization
			if(originalLocale.equals(Model.LOCALE_RU)){
				localeList.add(originalLocale);
				textList.add(originalTexts);
				localeList.add(Model.LOCALE_EN);
				textList.add(makeTransliteText(info,originalTexts, true));
			}
			if(originalLocale.equals(Model.LOCALE_EN)){
				// localize description fields
				localeList.add(originalLocale);
				textList.add(makeTransliteText(info,originalTexts, false));
				localeList.add(Model.LOCALE_RU);
				textList.add(originalTexts);

			}


			// setup model
			final Model model = mModel;
			model.locales = (String[]) localeList.toArray(new String[localeList.size()]);
			model.localeTexts = (String[][]) textList.toArray(new String[textList.size()][]);
			model.localeCurrent = model.locales[0];
			model.texts = model.localeTexts[0];
			model.textLength = model.localeTexts[0].length;


		}

		private String[] makeTransliteText(CountryLibrary.CountryLibraryRecord info, final String[] originalTexts, boolean transliterate) { 
			final int len = originalTexts.length;
			final String[] translitTexts = new String[len];
			if(info!=null){
				translitTexts[mModel.countryName] = info.CountryNameEn;
				translitTexts[mModel.cityName] = info.CityNameEn;
			}
			for(int i=0; i<len; i++){
				if(translitTexts[i] == null){
					translitTexts[i] = transliterate ?  StringUtil.toTranslit(originalTexts[i]) : originalTexts[i];
				}
			}
			return translitTexts;
		}

		private String determineLocale(String[] originalTexts) {
			int low = 0;
			int high = 0;
			for(String txt : originalTexts){
				final int len = txt.length();
				for(int i = 0; i<len; i++){
					char ch = txt.charAt(i);
					if( ch >= 128 ){
						high++;
					}else{
						low++;
					}
				}
			}
			String originalLocale = low>high ? Model.LOCALE_EN : Model.LOCALE_RU;
			return originalLocale;
		}

		private void makeLineObjects(TransportLine line, String stationList, String drivingList, String aliasesList) {

			ArrayList<String> stations = new ArrayList<String>();
			HashSet<SegmentInfo> segments = new HashSet<SegmentInfo>();
			makeDrivingGraph(stationList, drivingList, stations, segments);

			final HashMap<String, String> aliases = makeAliasDictionary(aliasesList);		

			// create stations
			final HashMap<String, Integer> localStationIndex = new HashMap<String, Integer>();
			final int[] stationNumbers = new int[stations.size()]; 
			int index = mTransportStations.size();
			int number = 0;
			for(String stationSystemName : stations){
				final String alias = aliases.get(stationSystemName);
				final TransportStation station = new TransportStation();
				final int id = index++;
				station.id = id;
				station.lineId = line.id;
				station.systemName = stationSystemName;
				station.name = appendLocalizedText( alias!=null ? alias : stationSystemName );

				station.owner = mModel;

				mTransportStations.add(station);
				localStationIndex.put(stationSystemName, id);
				stationNumbers[number] = id;
				number++;
			}
			line.stations = stationNumbers;

			index = mTransportSegments.size();
			for(SegmentInfo si : segments){
				final Integer fromId = localStationIndex.get(si.from);
				final Integer toId = localStationIndex.get(si.to);
				final Integer delay = si.delay;

				if(fromId != null && toId != null){
					final TransportSegment segment = new TransportSegment();
					segment.id = index++;
					segment.stationFromId = fromId;
					segment.stationToId = toId;
					segment.delay = delay;
					segment.lineId = line.id;
					segment.flags = 0;
					segment.owner = mModel;

					mTransportSegments.add(segment);
				}else{
					System.out.println(si);
				}
			}

			updateStationIndex(line);
			lineIndex.put(line.systemName, line);
		}

		private HashMap<String, String> makeAliasDictionary(String aliasesList) {
			// fill aliases table
			final HashMap<String,String> aliases = new HashMap<String, String>();
			if(aliasesList!=null){
				// build aliases table
				String[] aliasesTable = StringUtil.parseStringArray(aliasesList);
				// append new station names
				final int len = ( aliasesTable.length / 2 );
				for(int i = 0; i < len; i++){
					final int idx = i * 2;
					final String name = aliasesTable[idx];
					final String displayName = aliasesTable[idx+1];
					aliases.put(name, displayName);
				}
			}
			return aliases;
		}

		private void makeDrivingGraph(String stationList, String drivingList, 
				ArrayList<String> stations, HashSet<SegmentInfo> segments) {

			ModelUtil.StationsString tStations = new ModelUtil.StationsString(stationList);
			ModelUtil.DelaysString tDelays = new ModelUtil.DelaysString(drivingList);

			String toStation;
			Integer toDelay;

			String fromStation = null;
			Integer fromDelay = null;

			String thisStation = tStations.next();
			stations.add(thisStation);

			do {
				if ("(".equals(tStations.getNextDelimeter())) {
					int idx = 0;
					Integer[] delays = tDelays.nextBracket();
					while (tStations.hasNext() && !")".equals(tStations.getNextDelimeter())) {
						boolean isForwardDirection = true;
						String bracketedStationName = tStations.next();
						if (bracketedStationName.startsWith("-")) {
							bracketedStationName = bracketedStationName.substring(1);
							isForwardDirection = !isForwardDirection;
						}

						if (bracketedStationName != null && bracketedStationName.length() > 0) {
							String bracketedStation = bracketedStationName;
							if (isForwardDirection) {
								segments.add(new SegmentInfo(thisStation, bracketedStation, delays.length <= idx ? null : delays[idx]));

							} else {
								segments.add(new SegmentInfo(bracketedStation, thisStation, delays.length <= idx ? null : delays[idx]));
							}
						}
						idx++;
					}

					fromStation = thisStation;

					fromDelay = null;
					toDelay = null;

					if (!tStations.hasNext()) {
						break;
					}

					thisStation = tStations.next();
					stations.add(thisStation);

				} else {

					toStation = tStations.next();
					stations.add(toStation);

					if (tDelays.beginBracket()) {
						Integer[] delays = tDelays.nextBracket();
						toDelay = delays[0];
						fromDelay = delays[1];
					} else {
						toDelay = tDelays.next();
					}

					SegmentInfo this2from = new SegmentInfo(thisStation, fromStation, fromDelay);
					SegmentInfo this2to = new SegmentInfo(thisStation, toStation, toDelay);

					if (fromStation != null && !segments.contains(this2from) ) {
						if (fromDelay == null) {
							final SegmentInfo opposite = new SegmentInfo(fromStation, thisStation, null);
							for(SegmentInfo si : segments){
								if(opposite.equals(si)){
									this2from.delay = si.delay;
								}
							}
						} 
						segments.add(this2from);
					}

					if (toStation != null && !segments.contains(this2to)) {
						segments.add(this2to);
					}

					fromStation = thisStation;

					fromDelay = toDelay;
					toDelay = null;

					thisStation = toStation;
					toStation = null;

					if(!tStations.hasNext()){
						this2from = new SegmentInfo(thisStation, fromStation, fromDelay);

						if (fromStation != null && !segments.contains(this2from) ) {
							if (fromDelay == null) {
								final SegmentInfo opposite = new SegmentInfo(fromStation, thisStation, null);
								for(SegmentInfo si : segments){
									if(opposite.equals(si)){
										this2from.delay = si.delay;
									}
								}
							}
							segments.add(this2from);
						}					

					}
				}

			} while (tStations.hasNext());
		}

		private void makeTransfer(final String value) {
			String[] parts = StringUtil.parseStringArray(value);
			String startLine = parts[0].trim();
			String startStation = parts[1].trim();
			String endLine = parts[2].trim();
			String endStation = parts[3].trim();

			TransportStation from = getStation(startLine, startStation);
			TransportStation to = getStation(endLine, endStation);

			if(from!=null && to!=null){
				TransportTransfer transfer = new TransportTransfer();
				transfer.lineFromId = from.lineId;
				transfer.stationFromId = from.id;
				transfer.lineToId = to.lineId;
				transfer.stationToId = to.id; 
				transfer.delay = parts.length > 4 && parts[4].length() > 0 ? StringUtil.parseNullableDelay(parts[4]) : null;
				transfer.flags = parts.length > 5 && parts[5].indexOf(TransportTransfer.INVISIBLE)!=-1 ? TransportTransfer.TYPE_INVISIBLE : 0;
				transfer.id = mTransportTransfers.size();
				transfer.owner = mModel;
				mTransportTransfers.add(transfer);
			}
		}

		private void findModelFiles() {
			final ArrayList<String> trpFiles = mTrpFiles; 
			final ArrayList<String> mapFiles = mMapFiles;
			final ArrayList<String> txtFiles = mTxtFiles;
			final ArrayList<String> imgFiles = mImgFiles;

			Enumeration<? extends ZipEntry> entries = mZipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) continue;
				final String name = entry.getName();
				if(name.endsWith(".map")){
					if(!name.equalsIgnoreCase("metro.map")){
						mapFiles.add(name);	                	
					}else{
						mapFiles.add(0,name);
					}
				}else if(name.endsWith(".trp")){
					if(!name.equalsIgnoreCase("metro.trp")){
						trpFiles.add(name);	                	
					}else{
						trpFiles.add(0,name);
					}
				}else if(name.endsWith(".txt")){
					txtFiles.add(name);	                	

				}else if(name.endsWith(".vec") || name.endsWith(".gif")|| name.endsWith(".png")|| name.endsWith(".bmp")){
					imgFiles.add(name);
				}else if(name.endsWith(".cty")){
					mCityFile = name;
					if(mDescriptionOnly) return;
				}
			}
		}

		public Model getModel() throws IOException{
			if(mModel==null){
				execute();
			}
			return mModel;
		}

	}

	private static class SegmentInfo
	{
		public String from;
		public String to;
		public Integer delay;

		public SegmentInfo(String from, String to, Integer delay){
			this.from = from;
			this.to = to;
			this.delay = delay;
		}

		public boolean equals(Object obj) {
			SegmentInfo o = (SegmentInfo)obj;
			return from.equals(o.from) && to.equals(o.to);
		}

		public int hashCode() {
			return from.hashCode() + to.hashCode();
		}

		public String toString() {
			return "[FROM:" + from + ";TO:" + to + ";DELAY:" + delay + "]";
		}

	}


}

