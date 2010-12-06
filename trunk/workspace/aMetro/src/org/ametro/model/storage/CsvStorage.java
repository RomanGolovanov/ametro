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
package org.ametro.model.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.ametro.app.Constants;
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
import org.ametro.util.FileUtil;
import org.ametro.util.csv.CsvReader;
import org.ametro.util.csv.CsvWriter;

public class CsvStorage implements IModelStorage {

	public String[] loadModelLocale(String fileName, Model model, int localeId) throws IOException {
		final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(fileName), BUFFER_SIZE));
		final String localeEntryName = String.format(LOCALE_ENTRY_NAME, model.locales[localeId]);
		ZipEntry zipEntry;
		while( (zipEntry = zip.getNextEntry()) != null) { 
			final String name = zipEntry.getName();
			if(localeEntryName.equals(name)){
				String[] texts  = deserializeLocaleTable(zip, model, false);
				model.localeTexts[localeId] = texts;
				return texts;
			}
		}
		return null;
	}

	public SchemeView loadModelView(String fileName, Model model, String viewName) throws IOException {
		final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(fileName), BUFFER_SIZE));
		final String viewEntryName = String.format(MAP_ENTRY_NAME, viewName);
		final int id = model.getViewId(viewName);
		ZipEntry zipEntry;
		while( (zipEntry = zip.getNextEntry()) != null) { 
			final String name = zipEntry.getName();
			if(viewEntryName.equals(name)){
				SchemeView view = deserializeMapView(zip, model);
				model.views[id] = view;
				return view;
			}

		}
		return null;
	}

	public Model loadModel(String fileName, Locale locale) throws IOException{
		return loadModel(fileName, locale, false);
	}

	public Model loadModelDescription(String fileName, Locale locale) throws IOException{
		return loadModel(fileName, locale, true);
	}

	private Model loadModel(String fileName, Locale locale, boolean descriptionOnly) throws IOException
	{
		final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(fileName), BUFFER_SIZE));

		boolean isIndexLoaded = false;
		final Model model = new Model();

		HashMap<String,SchemeView> views = new HashMap<String, SchemeView>();
		HashMap<String,String[]> locales = new HashMap<String, String[]>();
		HashMap<String,MapLayerContainer> layers = new HashMap<String, MapLayerContainer>();

		boolean defaultViewLoaded = false;
		String defaultLocaleName = null;

		ZipEntry zipEntry;
		while( (zipEntry = zip.getNextEntry()) != null) { 
			final String name = zipEntry.getName();
			if(mMainEntries.contains(name)){
				if (MAIN_ENTRY_NAME.equals(name)) {
					deserializeModel(zip, model);
					model.fileSystemName = fileName;
					defaultLocaleName = model.getLocaleName(locale);
					isIndexLoaded = true;
				} else if (!descriptionOnly) {
					if ( TRANSPORT_MAPS_ENTRY_NAME.equals(name)) {
						deserializeMap(zip, model);
					} else if ( TRANSPORT_LINES_ENTRY_NAME.equals(name)) {
						deserializeLines(zip, model);
					} else if ( TRANSPORT_STATIONS_ENTRY_NAME.equals(name)) {
						deserializeStations(zip, model);
					} else if ( TRANSPORT_SEGMENTS_ENTRY_NAME.equals(name)) {
						deserializeSegments(zip, model);
					} else if ( TRANSPORT_TRANSFERS_ENTRY_NAME.equals(name)) {
						deserializeTransfers(zip, model);
					}
				}
			}else if(!descriptionOnly && name.startsWith("maps\\")){
				String viewName = FileUtil.getFileName(name);
				if(!defaultViewLoaded){
					SchemeView view = deserializeMapView(zip,model);
					views.put(viewName, view);
					defaultViewLoaded = true;
				}else{
					views.put(viewName, null);
				}
			}else if(name.startsWith("locales\\")){
				String localeName = FileUtil.getFileName(name);
				if(descriptionOnly || localeName.equals(defaultLocaleName)){
					String localeTable[] = deserializeLocaleTable(zip, model, descriptionOnly );
					locales.put(localeName, localeTable);
				}else{
					locales.put(localeName, null);
				}
			}else if(!descriptionOnly && name.startsWith("layers\\")){
			}
			zip.closeEntry();
		}
		zip.close();

		if(isIndexLoaded){

			int len = model.locales.length;
			model.localeTexts = new String[len][];
			for(int i = 0; i < len; i++){
				String localeName = model.locales[i];
				model.localeTexts[i] = locales.get(localeName);

				if(locale!=null && localeName.equals(defaultLocaleName)){
					model.texts = model.localeTexts[i];
					model.localeCurrent = localeName;
				}else if(localeName.equals(model.localeCurrent)){
					model.texts = model.localeTexts[i];
				}
			}

			len = model.viewSystemNames.length;
			model.views = new SchemeView[len];
			for(int i = 0; i < len; i++){
				String viewName = model.viewSystemNames[i];
				model.views[i] = views.get(viewName);
			}

			len = layers.size();
			model.layers = new MapLayerContainer[len];
			for(int i = 0; i < len; i++){
				String layerName = model.layerNames[i];
				model.layers[i] = layers.get(layerName);
			}

		}
		//Debug.stopMethodTracing();
		return model;
	}

	public void saveModel(String fileName, Model model) throws IOException {
		final ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName),8196));
		final CsvWriter csvWriter = new CsvWriter(new BufferedWriter( new OutputStreamWriter(zipOut, ENCODING)));
		serializeModel(model, zipOut, csvWriter);
		zipOut.close();
	}

	private SchemeView deserializeMapView(ZipInputStream zip, Model model) throws IOException {
		final CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(zip, ENCODING), BUFFER_SIZE ));
		final SchemeView view = new SchemeView();
		view.owner = model;

		reader.next();
		view.id = reader.readInt();
		view.systemName = reader.readString();
		view.name = reader.readInt();
		view.isMain =reader.readBoolean();
		view.transportTypes = reader.readLong();
		view.width = reader.readInt();
		view.height = reader.readInt();
		view.stationDiameter = reader.readInt();
		view.lineWidth = reader.readInt();
		view.backgroundSystemName = reader.readString();
		view.isVector = reader.readBoolean();
		view.isWordWrap = reader.readBoolean();
		view.isUpperCase = reader.readBoolean();
		view.transports = reader.readIntArray();
		view.transportsChecked = reader.readIntArray();
		int linesLength = reader.readInt();
		int stationsLength = reader.readInt();
		int segmentsLength = reader.readInt();
		int transfersLength = reader.readInt();

		view.lines = new LineView[linesLength];
		view.stations = new StationView[stationsLength];
		view.segments = new SegmentView[segmentsLength];
		view.transfers = new TransferView[transfersLength];

		for(int i = 0; i < linesLength; i++){
			reader.next();
			LineView obj = new LineView();
			obj.id = reader.readInt();
			obj.lineId = reader.readInt();
			obj.lineWidth = reader.readInt();
			obj.lineColor = reader.readInt();
			obj.labelColor = reader.readInt();
			obj.labelBackgroundColor = reader.readInt();
			obj.lineNameRect = reader.readModelRect();
			obj.owner = model;
			view.lines[i] = obj;
		}
		for(int i = 0; i < stationsLength; i++){
			reader.next();
			StationView obj = new StationView();
			obj.id = reader.readInt();
			obj.stationId = reader.readInt();
			obj.lineViewId = reader.readInt();
			obj.stationPoint = reader.readModelPoint();
			obj.stationNameRect = reader.readModelRect();
			obj.stationHeight = reader.readInteger();
			obj.owner = model;
			view.stations[i] = obj;
		}
		for(int i = 0; i < segmentsLength; i++){
			reader.next();
			SegmentView obj = new SegmentView();
			obj.id = reader.readInt();
			obj.lineViewId = reader.readInt();
			obj.segmentId = reader.readInt();
			obj.stationViewFromId = reader.readInt();
			obj.stationViewToId = reader.readInt();
			obj.spline = reader.readModelSpline();
			obj.owner = model;
			view.segments[i] = obj;
		}
		for(int i = 0; i < transfersLength; i++){
			reader.next();
			TransferView obj = new TransferView();
			obj.id = reader.readInt();
			obj.transferId = reader.readInt();
			obj.stationViewFromId = reader.readInt();
			obj.stationViewToId = reader.readInt();
			obj.owner = model;
			view.transfers[i] = obj;
		}

		return view;
	}

	private String[] deserializeLocaleTable(ZipInputStream zip, Model model, boolean descriptionOnly) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(zip, ENCODING), BUFFER_SIZE);
		final int len = descriptionOnly ? model.textLengthDescription : model.textLength;
		String[] table = new String[len];
		for(int i = 0; i<len; i++){
			table[i] = reader.readLine();
		}
		return table;
	}

	private void deserializeTransfers(ZipInputStream zip, final Model model) throws IOException {
		final CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(zip, ENCODING), BUFFER_SIZE )); 
		ArrayList<TransportTransfer> lst = new ArrayList<TransportTransfer>();
		while(reader.next()){
			TransportTransfer obj = new TransportTransfer();
			obj.id = reader.readInt();
			obj.mapFromId = reader.readInt();
			obj.lineFromId = reader.readInt();
			obj.stationFromId = reader.readInt();
			obj.mapToId = reader.readInt();
			obj.lineToId = reader.readInt();
			obj.stationToId = reader.readInt();
			obj.delay = reader.readInteger();
			obj.flags = reader.readInt();
			obj.owner = model;
			lst.add(obj);
		}
		model.transfers = (TransportTransfer[]) lst.toArray(new TransportTransfer[lst.size()]);
	}

	private void deserializeSegments(ZipInputStream zip, final Model model) throws IOException {
		final CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(zip, ENCODING), BUFFER_SIZE )); 
		ArrayList<TransportSegment> lst = new ArrayList<TransportSegment>();
		while(reader.next()){
			TransportSegment obj = new TransportSegment();
			obj.id = reader.readInt();
			obj.mapId = reader.readInt();
			obj.lineId = reader.readInt();
			obj.stationFromId = reader.readInt();
			obj.stationToId = reader.readInt();
			obj.delay = reader.readInteger();
			obj.flags = reader.readInt();
			obj.owner = model;
			lst.add(obj);
		}
		model.segments = (TransportSegment[]) lst.toArray(new TransportSegment[lst.size()]);
	}

	private void deserializeStations(ZipInputStream zip, final Model model) throws IOException {
		final CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(zip, ENCODING), BUFFER_SIZE )); 
		ArrayList<TransportStation> lst = new ArrayList<TransportStation>();
		ArrayList<TransportStationInfo> infos = new ArrayList<TransportStationInfo>();
		while(reader.next()){
			TransportStation obj = new TransportStation();
			obj.id = reader.readInt();
			obj.mapId = reader.readInt();
			obj.lineId = reader.readInt();
			obj.name = reader.readInt();
			obj.systemName = reader.readString();
			obj.location = reader.readModelLocation();
			
			obj.owner = model;
			lst.add(obj);
		}
		model.stations = (TransportStation[]) lst.toArray(new TransportStation[lst.size()]);
		model.stationInfos = (TransportStationInfo[]) infos.toArray(new TransportStationInfo[infos.size()]);
	}

	private void deserializeLines(ZipInputStream zip, final Model model) throws IOException {
		final CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(zip, ENCODING), BUFFER_SIZE )); 
		ArrayList<TransportLine> lst = new ArrayList<TransportLine>();
		while(reader.next()){
			TransportLine obj = new TransportLine();
			obj.id = reader.readInt();
			obj.mapId = reader.readInt();
			obj.name = reader.readInt();
			obj.systemName = reader.readString();
			obj.lineMapName = reader.readString();
			obj.stations = reader.readIntArray();
			obj.delays = reader.readIntegerArray();
			obj.owner = model;
			lst.add(obj);
		}
		model.lines = (TransportLine[]) lst.toArray(new TransportLine[lst.size()]);	
	}

	private void deserializeMap(ZipInputStream zip, final Model model) throws IOException {
		final CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(zip, ENCODING), BUFFER_SIZE )); 
		ArrayList<TransportMap> lst = new ArrayList<TransportMap>();
		while(reader.next()){
			TransportMap obj = new TransportMap();
			obj.id = reader.readInt();
			obj.transportTypes = reader.readInt();
			obj.systemName = reader.readString();
			obj.name = reader.readInt();
			obj.typeName = reader.readInt();
			obj.owner = model;
			lst.add(obj);
		}
		model.maps = (TransportMap[]) lst.toArray(new TransportMap[lst.size()]);
	}


	private void deserializeModel(ZipInputStream zip, final Model model) throws IOException {
		final CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(zip, ENCODING), BUFFER_SIZE )); 
		reader.next();

		int version = reader.readInt();

		if(version!=Constants.MODEL_VERSION){
			throw new IOException("Unsupported version");
		}
		model.systemName = reader.readString();
		model.timestamp = reader.readLong();
		model.countryIso = reader.readString();
		model.countryName = reader.readInt();
		model.cityName = reader.readInt();
		model.transportTypes = reader.readLong();
		model.location = reader.readModelLocation();
		model.locales = reader.readStringArray();
		model.localeCurrent = reader.readString();
		model.textLength = reader.readInt();
		model.textLengthDescription = reader.readInt();

		model.authors = reader.readIntArray();
		model.comments = reader.readIntArray();
		model.delays = reader.readIntArray();
		model.viewSystemNames = reader.readStringArray();
		model.viewNames = reader.readIntArray();
		model.viewTransportTypes = reader.readLongArray();
		model.viewIsMain = reader.readBoolArray();
		
		model.layerNames = reader.readStringArray();
	}

	private void serializeModel(final Model model, final ZipOutputStream zip, final CsvWriter writer) throws IOException {
		ZipEntry zipEntry = new ZipEntry(MAIN_ENTRY_NAME);
		zip.putNextEntry(zipEntry);
		writer.newRecord();
		writer.writeInt(Constants.MODEL_VERSION);
		writer.writeString(model.systemName);
		writer.writeLong(model.timestamp);
		writer.writeString(model.countryIso);
		writer.writeInt(model.countryName);
		writer.writeInt(model.cityName);
		writer.writeLong(model.transportTypes);
		writer.writeModelLocation(model.location);
		writer.writeStringArray(model.locales);
		writer.writeString(model.localeCurrent);
		writer.writeInt(model.textLength);
		writer.writeInt(model.textLengthDescription);
		writer.writeIntArray(model.authors);
		writer.writeIntArray(model.comments);
		writer.writeIntArray(model.delays);
		
		writer.writeStringArray(model.viewSystemNames);
		writer.writeIntArray(model.viewNames);
		writer.writeLongArray(model.viewTransportTypes);
		writer.writeBoolArray(model.viewIsMain);
		
		writer.writeStringArray(model.layerNames);
		writer.newRecord();

		writer.flush();
		zip.closeEntry();

		serializeTransportMaps(model, zip, writer);
		serializeTransportLines(model, zip, writer);
		serializeTransportStations(model, zip, writer);
		serializeTransportSegments(model, zip, writer);
		serializeTransportTransfers(model, zip, writer);

		serializeMaps(model, zip, writer);
		serializeLayers(model, zip, writer);
		serializeLocaleTable(model, zip);

	}

	private void serializeLayers(Model model, ZipOutputStream zip, CsvWriter writer) throws IOException {
		for(MapLayerContainer obj : model.layers){
			String entryName = String.format(LAYER_ENTRY_NAME, obj.id);
			ZipEntry zipEntry = new ZipEntry(entryName);
			zip.putNextEntry(zipEntry);
			//writer.writeInt(obj.id);
			writer.flush();
			zip.closeEntry();			
		}
	}

	private void serializeMaps(Model model, ZipOutputStream zip, CsvWriter writer) throws IOException {
		for(SchemeView obj : model.views){
			String entryName = String.format(MAP_ENTRY_NAME, obj.systemName);
			ZipEntry zipEntry = new ZipEntry(entryName);
			zip.putNextEntry(zipEntry);

			writer.writeInt(obj.id);
			writer.writeString(obj.systemName);
			writer.writeInt(obj.name);
			writer.writeBoolean(obj.isMain);
			writer.writeLong(obj.transportTypes);
			writer.writeInt(obj.width);
			writer.writeInt(obj.height);
			writer.writeInt(obj.stationDiameter);
			writer.writeInt(obj.lineWidth);
			writer.writeString(obj.backgroundSystemName);
			writer.writeBoolean(obj.isVector);
			writer.writeBoolean(obj.isWordWrap);
			writer.writeBoolean(obj.isUpperCase);
			writer.writeIntArray(obj.transports);
			writer.writeIntArray(obj.transportsChecked);
			writer.writeInt(obj.lines.length);
			writer.writeInt(obj.stations.length);
			writer.writeInt(obj.segments.length);
			writer.writeInt(obj.transfers.length);
			writer.newRecord();

			for(LineView v : obj.lines){
				writer.writeInt(v.id);
				writer.writeInt(v.lineId);
				writer.writeInt(v.lineWidth);
				writer.writeInt(v.lineColor);
				writer.writeInt(v.labelColor);
				writer.writeInt(v.labelBackgroundColor);
				writer.writeModelRect(v.lineNameRect);
				writer.newRecord();
			}
			for(StationView v : obj.stations){
				writer.writeInt(v.id);
				writer.writeInt(v.stationId);
				writer.writeInt(v.lineViewId);
				writer.writeModelPoint(v.stationPoint);
				writer.writeModelRect(v.stationNameRect);
				writer.writeInteger(v.stationHeight);
				writer.newRecord();
			}
			for(SegmentView v : obj.segments){
				writer.writeInt(v.id);
				writer.writeInt(v.lineViewId);
				writer.writeInt(v.segmentId);
				writer.writeInt(v.stationViewFromId);
				writer.writeInt(v.stationViewToId);
				writer.writeModelSpline(v.spline);
				writer.newRecord();
			}
			for(TransferView v : obj.transfers){
				writer.writeInt(v.id);
				writer.writeInt(v.transferId);
				writer.writeInt(v.stationViewFromId);
				writer.writeInt(v.stationViewToId);
				writer.newRecord();
			}
			writer.flush();
			zip.closeEntry();			


		}
	}

	private void serializeTransportMaps(final Model model, final ZipOutputStream zip, final CsvWriter writer) throws IOException {
		ZipEntry zipEntry = new ZipEntry(TRANSPORT_MAPS_ENTRY_NAME);
		zip.putNextEntry(zipEntry);
		for(TransportMap obj : model.maps){
			writer.writeInt(obj.id);
			writer.writeInt(obj.transportTypes);
			writer.writeString(obj.systemName);
			writer.writeInt(obj.name);
			writer.writeInt(obj.typeName);
			writer.newRecord();
		}
		writer.flush();
		zip.closeEntry();
	}

	private void serializeTransportLines(final Model model, final ZipOutputStream zip, final CsvWriter writer) throws IOException {
		ZipEntry zipEntry = new ZipEntry(TRANSPORT_LINES_ENTRY_NAME);
		zip.putNextEntry(zipEntry);
		for(TransportLine obj : model.lines){
			writer.writeInt(obj.id);
			writer.writeInt(obj.mapId);
			writer.writeInt(obj.name);
			writer.writeString(obj.systemName);
			writer.writeString(obj.lineMapName);
			writer.writeIntArray(obj.stations);
			writer.writeIntegerArray(obj.delays);
			writer.newRecord();
		}
		writer.flush();
		zip.closeEntry();
	}

	private void serializeTransportStations(final Model model, final ZipOutputStream zip, final CsvWriter writer) throws IOException {
		ZipEntry zipEntry = new ZipEntry(TRANSPORT_STATIONS_ENTRY_NAME);
		zip.putNextEntry(zipEntry);
		for(TransportStation obj : model.stations){
			writer.writeInt(obj.id);
			writer.writeInt(obj.mapId);
			writer.writeInt(obj.lineId);
			writer.writeInt(obj.name);
			writer.writeString(obj.systemName);
			writer.writeModelLocation(obj.location);
			writer.newRecord();
		}
		writer.flush();
		zip.closeEntry();
	}	

	private void serializeTransportSegments(final Model model, final ZipOutputStream zip, final CsvWriter writer) throws IOException {
		ZipEntry zipEntry = new ZipEntry(TRANSPORT_SEGMENTS_ENTRY_NAME);
		zip.putNextEntry(zipEntry);
		for(TransportSegment obj : model.segments){
			writer.writeInt(obj.id);
			writer.writeInt(obj.mapId);
			writer.writeInt(obj.lineId);
			writer.writeInt(obj.stationFromId);
			writer.writeInt(obj.stationToId);
			writer.writeInteger(obj.delay);
			writer.writeInt(obj.flags);
			writer.newRecord();
		}
		writer.flush();
		zip.closeEntry();
	}	

	private void serializeTransportTransfers(final Model model, final ZipOutputStream zip, final CsvWriter writer) throws IOException {
		ZipEntry zipEntry = new ZipEntry(TRANSPORT_TRANSFERS_ENTRY_NAME);
		zip.putNextEntry(zipEntry);
		for(TransportTransfer obj : model.transfers){
			writer.writeInt(obj.id);
			writer.writeInt(obj.mapFromId);
			writer.writeInt(obj.lineFromId);
			writer.writeInt(obj.stationFromId);
			writer.writeInt(obj.mapToId);
			writer.writeInt(obj.lineToId);
			writer.writeInt(obj.stationToId);
			writer.writeInteger(obj.delay);
			writer.writeInt(obj.flags);
			writer.newRecord();
		}
		writer.flush();
		zip.closeEntry();
	}		

	private void serializeLocaleTable(final Model model, final ZipOutputStream zip) throws IOException {
		final int len = model.locales.length;
		final int textsLen = model.textLength;
		for(int i = 0; i < len; i++){
			String entryName = String.format(LOCALE_ENTRY_NAME, model.locales[i]);
			ZipEntry zipEntry = new ZipEntry(entryName);
			zip.putNextEntry(zipEntry);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, ENCODING), BUFFER_SIZE);

			final String[] texts = model.localeTexts[i]; 
			for(int j = 0; j < textsLen; j++){
				writer.write( texts[j] );
				writer.newLine();
			}
			writer.flush();
			zip.closeEntry();
		}
	}

	private static HashSet<String> mMainEntries = new HashSet<String>();

	private static final String ENCODING = "utf-8";
	private static final int BUFFER_SIZE = 8196;

	private static final String MAIN_ENTRY_NAME = "index.csv";

	private static final String LOCALE_ENTRY_NAME = "locales\\%s.txt";
	private static final String LAYER_ENTRY_NAME = "layers\\%s.csv";
	private static final String MAP_ENTRY_NAME = "maps\\%s.csv";

	private static final String TRANSPORT_MAPS_ENTRY_NAME = "transport\\maps.csv";
	private static final String TRANSPORT_STATIONS_ENTRY_NAME = "transport\\stations.csv";
	private static final String TRANSPORT_SEGMENTS_ENTRY_NAME = "transport\\segments.csv";
	private static final String TRANSPORT_LINES_ENTRY_NAME = "transport\\lines.csv";
	private static final String TRANSPORT_TRANSFERS_ENTRY_NAME = "transport\\transfers.csv";

	static {
		mMainEntries.add(MAIN_ENTRY_NAME);
		mMainEntries.add(TRANSPORT_MAPS_ENTRY_NAME);
		mMainEntries.add(TRANSPORT_STATIONS_ENTRY_NAME);
		mMainEntries.add(TRANSPORT_SEGMENTS_ENTRY_NAME);
		mMainEntries.add(TRANSPORT_LINES_ENTRY_NAME);
		mMainEntries.add(TRANSPORT_TRANSFERS_ENTRY_NAME);
	}

}
