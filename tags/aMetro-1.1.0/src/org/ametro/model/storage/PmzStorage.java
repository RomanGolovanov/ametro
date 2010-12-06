/*
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
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

import static org.ametro.app.Constants.LOCALE_EN;
import static org.ametro.app.Constants.LOCALE_RU;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.directory.CatalogMapSuggestion;
import org.ametro.directory.CityDirectory;
import org.ametro.directory.CityStationDictionary;
import org.ametro.directory.CountryDirectory;
import org.ametro.directory.ImportDirectory;
import org.ametro.directory.ImportMapDirectory;
import org.ametro.directory.ImportTransportDirectory;
import org.ametro.directory.StationDirectory;
import org.ametro.directory.ImportMapDirectory.ImportMapEntity;
import org.ametro.directory.ImportTransportDirectory.TransportMapEntity;
import org.ametro.model.LineView;
import org.ametro.model.MapLayerContainer;
import org.ametro.model.SchemeView;
import org.ametro.model.Model;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.TransportLine;
import org.ametro.model.TransportMap;
import org.ametro.model.TransportSegment;
import org.ametro.model.TransportStation;
import org.ametro.model.TransportStationInfo;
import org.ametro.model.TransportTransfer;
import org.ametro.model.TransportType;
import org.ametro.model.ext.ModelLocation;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.ext.ModelRect;
import org.ametro.model.ext.ModelSpline;
import org.ametro.model.util.IniStreamReader;
import org.ametro.model.util.ModelUtil;
import org.ametro.util.CollectionUtil;
import org.ametro.util.DateUtil;
import org.ametro.util.StringUtil;

public class PmzStorage implements IModelStorage {

	/*package*/ static final int DEFAULT_LINE_BACKGOUND_COLOR = 0xFFFFFFFF; 
	
	/*package*/ static final String DEFAULT_ENCODING = "windows-1251";
	/*package*/ static final String DELAY_DAY_RU = "День";
	/*package*/ static final String DELAY_NIGHT_RU = "Ночь";
	/*package*/ static final String DELAY_RUSH_HOUR_RU = "Час-пик";
	/*package*/ static final String DELAY_DAY_EN = "Day";
	/*package*/ static final String DELAY_NIGHT_EN = "Night";
	/*package*/ static final String DELAY_RUSH_HOUR_EN = "Rush Hour";

	public Model loadModel(String fileName, Locale locale) throws IOException {
		PmzImporter importer = new PmzImporter(fileName,false);
		try {
			Model model = importer.getModel();
			model.setLocale(locale);
			return model;
		} catch (IOException e) {
			return null;
		}	
	}

	public Model loadModelDescription(String fileName, Locale locale) throws IOException {
		PmzImporter importer = new PmzImporter(fileName,true);
		try {
			Model model = importer.getModel();
			model.setLocale(locale);
			return model;
		} catch (IOException e) {
			return null;
		}	
	}

	public void saveModel(String fileName, Model model) throws IOException {
		throw new NotImplementedException();
	}

	public String[] loadModelLocale(String fileName, Model model, int localeId) throws IOException {
		throw new NotImplementedException();
	}

	public SchemeView loadModelView(String fileName, Model model, String name) throws IOException {
		throw new NotImplementedException();
	}

	private static class PmzImporter {

		private File mFile;
		private ZipFile mZipFile;
		private Model mModel;
		private boolean mDescriptionOnly;

		private long mTimestamp;
		
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

		private ArrayList<SchemeView> mMapViews = new ArrayList<SchemeView>();
		private ArrayList<String> mMapViewSystemNames = new ArrayList<String>();

		private ArrayList<MapLayerContainer> mMapLayers = new ArrayList<MapLayerContainer>();
		private ArrayList<String> mMapLayerNames = new ArrayList<String>();

		private HashMap<String,TransportStation> mTransportStationIndex = new HashMap<String,TransportStation>();
		private HashMap<String,TransportLine> mTransportLineIndex = new HashMap<String, TransportLine>();
		private HashMap<String,TransportMap> mTransportMapIndex = new HashMap<String, TransportMap>();

		private ArrayList<String> mTextsOriginal = new ArrayList<String>();
		private ArrayList<String> mTextsTranslit = new ArrayList<String>();

		private HashMap<Integer, StationInfo> mStationInfo = new HashMap<Integer, StationInfo>();
		
		private StationDirectory mStationDirectory;
		private CityStationDictionary mCityStationDictionary;
		private ImportMapDirectory mImportMapDirectory;
		private ImportTransportDirectory mImportTransportDirectory;
		private ImportDirectory mImportDirectory;

		private ArrayList<String> mDelayNames = new ArrayList<String>();
		private TreeMap<String,Integer> mDelayIndexes = new TreeMap<String, Integer>();

		private String mCharset;
		
		private int[] getMapsNumbers(String[] maps) {
			ArrayList<Integer> res = new ArrayList<Integer>();
			for(String mapSystemName : maps){
				TransportMap map = mTransportMapIndex.get(mapSystemName);
				if(map!=null){
					res.add(map.id);
				}
			}
			return CollectionUtil.toArray(res);
		}

		private TransportStation getStation(String lineSystemName, String stationSystemName)
		{
			String key = lineSystemName + "\\" + stationSystemName;
			return mTransportStationIndex.get(key);
		}

		private void updateStationIndex(TransportLine line){
			final String lineSystemName = line.systemName;
			for(int id : line.stations){
				TransportStation station = mTransportStations.get(id); 
				String key = lineSystemName + "\\" + station.systemName;
				mTransportStationIndex.put(key, station);
			}
		}

		private int[] appendTextArray(String[] texts){
			if(texts == null) return null;
			final int len = texts.length;
			int[] r = new int[len];
			int base = mTextsOriginal.size();
			for(int i = 0; i < len; i++){
				r[i] = base + i;
				String txt = texts[i];
				mTextsOriginal.add(txt);
				mTextsTranslit.add(StringUtil.toTranslit(txt));
			}
			return r;
		}

		private int[] appendTextArray(String[] en, String[] ru){
			if(en==null || ru==null || en.length!=ru.length) return null;
			final int len = en.length;
			int[] r = new int[len];
			int base = mTextsOriginal.size();
			for(int i = 0; i < len; i++){
				r[i] = base + i;
				mTextsOriginal.add(ru[i]);
				mTextsTranslit.add(en[i]);
			}
			return r;
		}		
		private int appendLocalizedText(String txt){
			int pos = mTextsOriginal.size();
			mTextsOriginal.add(txt);
			mTextsTranslit.add(StringUtil.toTranslit(txt));
			return pos;
		}

		private int appendLocalizedText(String en, String ru) {
			int pos = mTextsOriginal.size();
			mTextsTranslit.add(en);
			mTextsOriginal.add(ru);
			return pos;
		}
		
		public PmzImporter(String fileName, boolean descriptionOnly){
			mFile = new File(fileName);
			mModel = null;
			mDescriptionOnly = descriptionOnly;
			mStationDirectory = ApplicationEx.getInstance().getStationDirectory();
			mCityStationDictionary = mStationDirectory.get(mFile);
			mImportMapDirectory = ApplicationEx.getInstance().getImportMapDirectory();
			mImportTransportDirectory = ApplicationEx.getInstance().getImportTransportDirectory();
			mImportDirectory = ApplicationEx.getInstance().getImportDirectory();
		}

		public void execute() throws IOException{
			mZipFile = null;
			try{
				mZipFile = new ZipFile(mFile, ZipFile.OPEN_READ);
				mModel = new Model();

				String fileName = mFile.getName();
				String mapName = fileName.substring(0, fileName.indexOf('.'));
				ImportDirectory.Entity importInfo = mImportDirectory.get(mapName);
				if(importInfo!=null && importInfo.getCharSet()!=null){
					mCharset = importInfo.getCharSet();
				}else{
					mCharset = DEFAULT_ENCODING; 
				}
				
				findModelFiles(); // find map files in archive
				importCityFile(); // load data from .cty file - map description
				if(!mDescriptionOnly) { 
					importTrpFiles(); // load data from .trp files 
					importMapFiles(); // load data from .map files
					//importTxtFiles(); // load data from .txt files
				}
				postProcessModel(); // make model from imported data
			}finally{
				if(mZipFile!=null){
					mZipFile.close();
				}
			}
		}

		@SuppressWarnings("unused")
		private void importTxtFiles() throws IOException {
			Collections.sort(mTxtFiles);

			final Model model = mModel;

			for(String fileName : mTxtFiles){
				InputStream stream = mZipFile.getInputStream(mZipFile.getEntry(fileName));
				IniStreamReader ini = new IniStreamReader(new InputStreamReader(stream, mCharset)); // access as INI file

				boolean addToStationInfo = false;
				String caption = null;
				String prefix = null;
				//String menuName = null;

				while(ini.readNext()){ 
					final String key = ini.getKey(); 
					final String value = ini.getValue();
					final String section = ini.getSection();

					if(section!=null){
						if(section.startsWith("Options")){ 
							if(key.equalsIgnoreCase("AddToInfo")){ 
								addToStationInfo = value.equalsIgnoreCase("1");
							}else if(key.equalsIgnoreCase("CityName")){
								// skip
							}else if(key.equalsIgnoreCase("MenuName")){
								//menuName = (value);
							}else if(key.equalsIgnoreCase("MenuImage")){

							}else if(key.equalsIgnoreCase("Caption")){
								caption = (value);
							}else if(key.equalsIgnoreCase("StringToAdd")){
								String txt = value.trim();
								if(txt.length() > 0){
									if(txt.startsWith("'") && txt.endsWith("'")){
										txt = txt.substring(1, txt.length()-1 ).trim();
									}
									prefix = (txt);
								}
							}			
						}else{
							if(addToStationInfo){
								makeStationInfo(caption, prefix, key, value, section);
							}
						}
					}
				}
			}

			TransportStationInfo[] lst = new TransportStationInfo[mTransportStations.size()];
			for(TransportStation station : mTransportStations){
				StationInfo src = mStationInfo.get(station.id);
				if(src!=null){
					TransportStationInfo info = new TransportStationInfo();
					String[] captions = (String[]) src.captions.toArray(new String[src.captions.size()]);
					info.captions = appendTextArray(captions);
					int len = captions.length;
					int[][] lines = new int[len][];
					for(int i = 0; i<len; i++){
						String caption = captions[i];
						ArrayList<String> textLines = src.data.get(caption);
						if(textLines!=null){
							String[] textLinesArray = (String[]) textLines.toArray(new String[textLines.size()]);
							lines[i] = appendTextArray(textLinesArray);
						}else{
							lines[i] = null;
						}
					}
					info.lines = lines;
					lst[station.id] = info;
				}
			}
			model.stationInfos = lst;

		}

		private void makeStationInfo(String caption, String prefix,
				final String key, final String value, final String section) {
			TransportStation station = getStation(section, key);
			if(station!=null){
				StationInfo info = mStationInfo.get(station.id);
				if(info==null){
					info = new StationInfo();
					mStationInfo.put(station.id, info);
				}
				ArrayList<String> lines = info.data.get(caption);
				if(lines == null){
					lines = new ArrayList<String>();
					info.captions.add(caption);
					info.data.put(caption, lines);
				}
				String[] textLines = StringUtil.fastSplit( value.replace("\\n",";"), ';' );
				for(String textLine : textLines){
					lines.add(prefix + textLine);
				}
			}
		}

		private void importMapFiles() throws IOException {
			final Model model = mModel;

			final ArrayList<LineView> lines = new ArrayList<LineView>();
			final ArrayList<StationView> stations = new ArrayList<StationView>();
			final HashMap<Long, ModelSpline> additionalNodes = new HashMap<Long, ModelSpline>();
			final HashMap<Integer,Integer> stationViews = new HashMap<Integer, Integer>();
			final HashMap<Integer,Integer> lineViewIndex = new HashMap<Integer, Integer>();

			final HashMap<String, LineView> viewsDefaults = new HashMap<String, LineView>();

			for(String fileName : mMapFiles){ // for each .map file in catalog
				InputStream stream = mZipFile.getInputStream(mZipFile.getEntry(fileName));
				IniStreamReader ini = new IniStreamReader(new InputStreamReader(stream, mCharset)); // access as INI file

				SchemeView view = new SchemeView();
				view.id = mMapViews.size();
				view.systemName = fileName;
				view.stationDiameter = 11;
				view.lineWidth = 9;
				view.isUpperCase = true;
				view.isWordWrap = true;
				view.isVector = true;
				view.owner = model;
				mMapViews.add(view);
				mMapViewSystemNames.add(view.systemName);

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
						makeAdditionalNodes(additionalNodes, stationViews, value);
					}else{
						if(isSectionChanged){
							line = mTransportLineIndex.get(section);
							if(line!=null){
								lineView = new LineView();

								lineView.labelBackgroundColor = DEFAULT_LINE_BACKGOUND_COLOR;
								
								LineView def = viewsDefaults.get(section);
								if(def == null){
									viewsDefaults.put(section, lineView);
								}else{
									lineView.labelColor = def.labelColor;
									lineView.lineColor = def.lineColor;
									lineView.labelBackgroundColor = def.labelBackgroundColor;
								}
								lineView.id = lines.size();
								lineView.lineWidth = view.lineWidth;
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
								rects = StringUtil.parsePmzModelRectArray(value);
							}else if(key.equalsIgnoreCase("Heights")){
								heights = StringUtil.parseIntegerArray(value);
							}else if(key.equalsIgnoreCase("Rect")){
								lineView.lineNameRect = StringUtil.parseModelRect(value);
							}else if(key.equalsIgnoreCase("Width")){
								lineView.lineWidth = StringUtil.parseInt(value, view.lineWidth);
							}else if(key.equalsIgnoreCase("LabelsBColor")){
								lineView.labelBackgroundColor = StringUtil.parseColor(value, DEFAULT_LINE_BACKGOUND_COLOR);
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

				ImportMapEntity entity = mImportMapDirectory.get(mFile.getName(), view.systemName);
				if(entity!=null){
					view.transportTypes = entity.getTransportType();
					view.name = appendLocalizedText(entity.getName(LOCALE_EN), entity.getName(LOCALE_RU));
					view.isMain = entity.isMain();
				}else{
					view.transportTypes = 0;
					view.name = appendLocalizedText(view.systemName);
					view.isMain = false;
				}
				
				fixViewDimensions(view);
			}

		}

		private void makeAdditionalNodes(
				final HashMap<Long, ModelSpline> additionalNodes,
				final HashMap<Integer, Integer> stationViews, final String value) {
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
						ModelPoint p = new ModelPoint(StringUtil.parseInt(parts[pos].trim(),0), StringUtil.parseInt(parts[pos + 1].trim(),0) );
						points.add(p);
						pos += 2;
					}
				}
				final ModelSpline spline = new ModelSpline();
				spline.isSpline = isSpline;
				spline.points = (ModelPoint[]) points.toArray(new ModelPoint[points.size()]);
				additionalNodes.put(Model.getSegmentKey(from.id, to.id), spline);
			}
		}


		private SegmentView[] makeSegmentViews(SchemeView view, HashMap<Integer,Integer> lineViewIndex, HashMap<Integer,Integer> stationViewIndex, HashMap<Long,ModelSpline> additionalNodes) {
			final Model model = mModel;
			final ArrayList<SegmentView> segments = new ArrayList<SegmentView>();
			int base = 0;
			for(TransportSegment segment: mTransportSegments){
				Integer fromId = stationViewIndex.get(segment.stationFromId);
				Integer toId = stationViewIndex.get(segment.stationToId);
				boolean visibleStations = fromId!=null && toId!=null;
				if(!visibleStations || ((segment.flags & TransportSegment.TYPE_INVISIBLE) != 0)){
					continue;
				}
				final ModelSpline spline = additionalNodes.get( Model.getSegmentKey(segment.stationFromId, segment.stationToId) );
				if(spline!=null && spline.isZero()){
					continue;
				}else{
					final ModelSpline opposite = additionalNodes.get( Model.getSegmentKey(segment.stationToId, segment.stationFromId) );
					if(opposite!=null && opposite.isZero()){
						continue;
					}
				}

				final SegmentView segmentView = new SegmentView();
				segmentView.id = base++;
				segmentView.lineViewId = lineViewIndex.get(segment.lineId);
				segmentView.segmentId = segment.id;
				segmentView.stationViewFromId = fromId;
				segmentView.stationViewToId = toId;

				segmentView.owner = model;


				if(spline!=null){
					segmentView.spline = spline;
				}else{
					segmentView.spline = null;
				}
				segments.add(segmentView);
			}
			return (SegmentView[]) segments.toArray(new SegmentView[segments.size()]);
		}

		private void fixViewDimensions(SchemeView view) {
			ModelRect mapRect = ModelUtil.getDimensions(view.stations);

			int xmin = mapRect.left;
			int ymin = mapRect.top;
			int xmax = mapRect.right;
			int ymax = mapRect.bottom;

			int dx = 80 - xmin;
			int dy = 80 - ymin;

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
			view.width = xmax - xmin + 160;
			view.height = ymax - ymin + 160;
		}

		private TransferView[] makeTransferViews(SchemeView view, HashMap<Integer,Integer> stationViewIndex) {
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

		private void makeStationViews(TransportLine line, LineView lineView, HashMap<Integer,Integer> stationViewIndex, ArrayList<StationView> stationViews, ModelPoint[] coords, ModelRect[] rects, Integer[] heights) {
			final int stationsCount = line.stations.length;
			final int pointsCount = coords!=null ? coords.length : 0;
			final int rectsCount = rects!=null ? rects.length : 0;
			final int heightsCount = heights!=null ? heights.length : 0;
			final int[] stations = line.stations;
			int base = stationViews.size();

			for(int i = 0; i < stationsCount && i < pointsCount; i++){
				if(ModelPoint.isNullOrZero( coords[i] )) { 
					continue; // skip station with ZERO coordinates!
				}
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
				IniStreamReader ini = new IniStreamReader(new InputStreamReader(stream, mCharset)); // access as INI file

				TransportMap map = new TransportMap(); // create new transport map
				map.id = mTransportMaps.size();
				map.owner = mModel;
				mTransportMaps.add(map);
				map.systemName = fileName; // setup transport map internal name
				mTransportMapIndex.put(map.systemName, map);

				TransportLine line = null; // create loop variables
				String stationList = null; // storing driving data
				String aliasesList = null;
				String drivingList = null; // for single line
				String lineName = null;
				ArrayList<Integer> delays = null;

				while(ini.readNext()){ // for each key in file
					final String key = ini.getKey(); // extract current properties
					final String value = ini.getValue();
					final String section = ini.getSection();
					final boolean isSectionChanged = ini.isSectionChanged(); 

					if(line!=null && isSectionChanged){ // if end of line
						line.name  = appendLocalizedText(lineName);
						makeLineObjects(line, stationList, drivingList, aliasesList, delays); // make station and segments
						line = null; // clear loop variables
						stationList = null;
						drivingList = null;
						aliasesList = null;
						lineName = null;
						delays = null;
					}
					if(section.startsWith("Line")){ // for line sections
						if(isSectionChanged){
							line = new TransportLine(); // at start - create new line object
							line.id = mTransportLines.size();
							line.mapId = map.id;
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
							aliasesList = value;
						}else if(key.startsWith("Delay")){
							String delayName = key.substring(5);
							if(DELAY_DAY_EN.equalsIgnoreCase(delayName)){
								delayName = DELAY_DAY_RU;
							}else if(DELAY_NIGHT_EN.equalsIgnoreCase(delayName)){
								delayName = DELAY_NIGHT_RU;
							}else if(DELAY_RUSH_HOUR_EN.equalsIgnoreCase(delayName)){
								delayName = DELAY_RUSH_HOUR_RU;
							}
							if(!mDelayIndexes.containsKey(delayName)){
								mDelayIndexes.put(delayName, mDelayNames.size());
								mDelayNames.add(delayName);
							}
							Integer delayValue = StringUtil.parseDelay(value);
							if(delays == null){
								delays = new ArrayList<Integer>();
							}
							int index = mDelayIndexes.get(delayName);
							CollectionUtil.ensureSize(delays,index+1);
							delays.set(index,delayValue);
						}
					}else if(section.equalsIgnoreCase("Transfers")){
						makeTransfer(value);
					}else if (section.equalsIgnoreCase("AdditionalInfo")){
						// do nothing at this time
					}else if (section.equalsIgnoreCase("Options")){
						if(key.equalsIgnoreCase("Type")){
							map.typeName = TransportType.getTransportTypeResource(value);
							map.transportTypes = TransportType.getTransportTypeId(value);
						}
					}
				}
				TransportMapEntity entity = mImportTransportDirectory.get(mFile.getName(), map.systemName);
				if(entity!=null){
					int transportType = entity.getTransportType();
					map.transportTypes = transportType;
					map.typeName = TransportType.getTransportTypeResource(transportType);
					map.name = appendLocalizedText(entity.getName(LOCALE_EN), entity.getName(LOCALE_RU));
				}else{
					if(map.typeName == 0){
							map.typeName = TransportType.UNKNOWN_RESOURCE_INDEX;
							map.transportTypes = TransportType.UNKNOWN_ID;
					}					
					map.name = appendLocalizedText(map.systemName);
				}
				if(line!=null){ // if end of line 
					makeLineObjects(line, stationList, drivingList, aliasesList, delays); // make station and segments
				}
			}
		}
		
		private void importCityFile() throws IOException {
			String cityNameRus = null;
			String country = null;
			String cityNameEng = null;
			final ArrayList<String> authors = new ArrayList<String>();
			final ArrayList<String> comments = new ArrayList<String>();

			ZipEntry cityEntry = mZipFile.getEntry(mCityFile);

			long gpsTimestamp = mStationDirectory.getTimestamp(mFile);
			mTimestamp =  Math.max(Math.max( DateUtil.toUTC( cityEntry.getTime() ), Constants.MODEL_IMPORT_TIMESTAMP), gpsTimestamp);
			
			InputStream stream = mZipFile.getInputStream(cityEntry);
			final IniStreamReader ini = new IniStreamReader(new InputStreamReader(stream, DEFAULT_ENCODING));
			while(ini.readNext()){
				final String key = ini.getKey();
				final String value = ini.getValue();
				if(key.equalsIgnoreCase("RusName")){
					cityNameRus = value;
				}else if(key.equalsIgnoreCase("CityName")){
					cityNameEng = value;
				}else if(key.equalsIgnoreCase("Country")){
					country = value;
				}else if(key.equalsIgnoreCase("MapAuthors")){
					authors.add(value);
				}else if(key.equalsIgnoreCase("Comment")){
					comments.add(value);
				}else if(key.equalsIgnoreCase("DelayNames")){
					mDelayNames.addAll(StringUtil.fastSplitToList(value,','));
				}
			}

			final CityStationDictionary lib = mCityStationDictionary;
			if(lib!=null){
				ArrayList<String> gpsComments = lib.getComments();
				if(gpsComments!=null && gpsComments.size()>0){
					authors.add("");
					authors.addAll(gpsComments);
				}
			}
			
			final Model model = mModel;
			model.systemName = mFile.getName().toLowerCase() + ".ametro";
			model.cityName = appendLocalizedText(cityNameRus!=null ? cityNameRus : cityNameEng);
			model.countryName = appendLocalizedText(country);
			model.authors = appendTextArray((String[]) authors.toArray(new String[authors.size()]));
			model.comments = appendTextArray((String[]) comments.toArray(new String[comments.size()]));
			model.textLengthDescription = mTextsOriginal.size();
		}


		private String translateDelay(String delayName) {
			if(DELAY_DAY_RU.equalsIgnoreCase(delayName)){
				return DELAY_DAY_EN;
			}else if(DELAY_NIGHT_RU.equalsIgnoreCase(delayName)){
				return DELAY_NIGHT_EN;
			}else if(DELAY_RUSH_HOUR_RU.equalsIgnoreCase(delayName)){
				return DELAY_RUSH_HOUR_EN;
			}
			return StringUtil.toTranslit(delayName);
		}

		private void postProcessModel() {
			final Model model = mModel;
			// fill model fields
			model.maps = (TransportMap[]) mTransportMaps.toArray(new TransportMap[mTransportMaps.size()]);
			model.lines = (TransportLine[]) mTransportLines.toArray(new TransportLine[mTransportLines.size()]);
			model.stations = (TransportStation[]) mTransportStations.toArray(new TransportStation[mTransportStations.size()]);
			model.segments = (TransportSegment[]) mTransportSegments.toArray(new TransportSegment[mTransportSegments.size()]);
			model.transfers = (TransportTransfer[]) mTransportTransfers.toArray(new TransportTransfer[mTransportTransfers.size()]);

			model.views = (SchemeView[]) mMapViews.toArray(new SchemeView[mMapViews.size()]);
			model.viewSystemNames = (String[]) mMapViewSystemNames.toArray(new String[mMapViewSystemNames.size()]);

			model.layers = (MapLayerContainer[]) mMapLayers.toArray(new MapLayerContainer[mMapLayers.size()]);
			model.layerNames = (String[]) mMapLayerNames.toArray(new String[mMapLayerNames.size()]);

			model.systemName = mFile.getName().toLowerCase();

			model.fileSystemName = mFile.getAbsolutePath();

			String[] russianDelays = (String[]) mDelayNames.toArray(new String[mDelayNames.size()]);
			String[] englishDelays = new String[russianDelays.length];
			int len = russianDelays.length;
			for(int i=0;i<len;i++){
				englishDelays[i] = translateDelay(russianDelays[i]);
			}
			model.delays = appendTextArray(englishDelays, russianDelays);

			makeTransportTypes(model);
			makeGlobalization(model);
			makeModelViewArrays(model);

			model.timestamp =  mTimestamp;
			
			if(!mDescriptionOnly){
				final CityStationDictionary lib = mCityStationDictionary;
				if(lib!=null){
					
					for(TransportStation station : model.stations){
						String lineSystemName = model.lines[station.lineId].systemName;
						String stationSystemName = station.systemName;
						ModelLocation l = lib.getStationLocation(lineSystemName, stationSystemName);
						if(l!=null){
							station.location = l;
						}
					}
				}
			}
		}

		private void makeTransportTypes(final Model model) {
			long transports = 0;
			final HashMap<TransportMap,Long> transportIndex = new HashMap<TransportMap, Long>();
			for(TransportMap map : model.maps){
				long typeId = map.transportTypes;
				transports |= typeId;
				transportIndex.put(map, typeId);
				
			}
			model.transportTypes = transports;
			for(SchemeView view : model.views){
				if(view.transportTypes == 0){
					view.transportTypes = extractViewTransportType(model, transportIndex, view);
				}
			}
		}

		private void makeModelViewArrays(final Model model) {
			final int len = model.views.length;
			model.viewNames = new int[len];
			model.viewIsMain = new boolean[len];
			model.viewTransportTypes = new long[len];
			for(int i=0; i<len; i++){
				final SchemeView view = model.views[i];
				model.viewNames[i] = view.name;
				model.viewIsMain[i] = view.isMain;
				model.viewTransportTypes[i] = view.transportTypes;
			}
		}

		private long extractViewTransportType(final Model model, final HashMap<TransportMap, Long> transportIndex, SchemeView view){
			if(view.transports!=null){
				long transports = 0;
				for(int mapId : view.transports){
					final TransportMap map = model.maps[mapId];
					final Long transportType = transportIndex.get(map);
					if(map!=null){
						transports |= transportType;
					}
				}
				return transports;
			}else{
				return TransportType.UNKNOWN_ID;
			}			
		}
		
		private void makeGlobalization(final Model model) {
			// prepare 
			final ArrayList<String> localeList = new ArrayList<String>();
			final ArrayList<String[]> textList = new ArrayList<String[]>();
			final String[] originalTexts = mTextsOriginal.toArray(new String[mTextsOriginal.size()]);
			final String[] translitTexts = mTextsTranslit.toArray(new String[mTextsTranslit.size()]);

			// locate country info
			final String countryName = originalTexts[mModel.countryName];
			final String cityName = originalTexts[mModel.cityName];
			
			CatalogMapSuggestion suggestion = CatalogMapSuggestion.create(ApplicationEx.getInstance(), mFile, cityName, countryName );
			CityDirectory.Entity cityEntity = suggestion.getCityEntity();
			CountryDirectory.Entity countryEntity = suggestion.getCountryEntity();
			
			localeList.add(LOCALE_EN);
			textList.add(translitTexts);
			localeList.add(LOCALE_RU);
			textList.add(originalTexts);
			
			// setup model
			model.location = cityEntity!=null ? cityEntity.getLocation() : null;
			model.locales = (String[]) localeList.toArray(new String[localeList.size()]);
			model.localeTexts = (String[][]) textList.toArray(new String[textList.size()][]);
			model.localeCurrent = model.locales[0];
			model.texts = model.localeTexts[0];
			model.textLength = model.localeTexts[0].length;
			model.countryIso = countryEntity!=null ? countryEntity.getISO2() : null; 
			
			int len = model.locales.length;
			for(int i=0;i<len;i++){
				final String code = model.locales[i];
				if(cityEntity!=null){
					model.localeTexts[i][model.cityName] = cityEntity.getName(code);
				}
				if(countryEntity!=null){
					model.localeTexts[i][model.countryName] = countryEntity.getName(code);
				}
			}
		}

		private void makeLineObjects(TransportLine line, String stationList, String drivingList, String aliasesList, ArrayList<Integer> delays) {

			final int mapId = line.mapId;
			
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
				station.mapId = mapId;
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
					segment.mapId = mapId;
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
			mTransportLineIndex.put(line.systemName, line);
			
			if(delays!=null){
				int size = mDelayNames.size();
				CollectionUtil.ensureSize(delays,size);
				line.delays = (Integer[]) delays.toArray(new Integer[size]);
			}
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
				transfer.mapFromId = from.mapId;
				transfer.lineFromId = from.lineId;
				transfer.stationFromId = from.id;
				transfer.mapToId = to.mapId;
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

	private static class StationInfo
	{
		public ArrayList<String> captions = new ArrayList<String>();
		public HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();
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

