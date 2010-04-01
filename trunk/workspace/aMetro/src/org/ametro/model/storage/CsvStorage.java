package org.ametro.model.storage;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ametro.Constants;
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
import org.ametro.util.csv.CsvWriter;

import android.util.Log;

public class CsvStorage implements IModelStorage {

	private static final String ENCODING = "UTF-8";

	private static final String MAIN_ENTRY_NAME = "index.csv";
	private static final String LOCALE_ENTRY_NAME = "locales\\%s.txt";

	private static final String TRANSPORT_MAPS_ENTRY_NAME = "transport\\maps.csv";
	private static final String TRANSPORT_STATIONS_ENTRY_NAME = "transport\\stations.csv";
	private static final String TRANSPORT_SEGMENTS_ENTRY_NAME = "transport\\segments.csv";
	private static final String TRANSPORT_LINES_ENTRY_NAME = "transport\\lines.csv";
	private static final String TRANSPORT_TRANSFERS_ENTRY_NAME = "transport\\transfers.csv";
	
	private static final String MAP_ENTRY_NAME = "maps\\%s.csv";

	public Model loadModel(String fileName, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	public Model loadModelDescription(String fileName, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	public void loadModelLocale(String fileName, Model model, Locale locale) {
		// TODO Auto-generated method stub
	}

	public boolean saveModel(String fileName, Model model) {
		try {
			long startTime = System.currentTimeMillis();
			final ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName),8196));
			final CsvWriter csvWriter = new CsvWriter(new BufferedWriter( new OutputStreamWriter(zipOut, ENCODING)));
			serializeModel(model, zipOut, csvWriter);
			zipOut.close();
			if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.INFO)) {
				Log.i(Constants.LOG_TAG_MAIN, "Model saving time is "
						+ (System.currentTimeMillis() - startTime) + "ms");
			}

		} catch (Throwable e) {
			if (Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)) {
				Log.e(Constants.LOG_TAG_MAIN, "Model saving error", e);
			}
			return false;

		}
		return true;
	}

	private void serializeModel(final Model model, final ZipOutputStream zip, final CsvWriter writer) throws IOException {
		ZipEntry zipEntry = new ZipEntry(MAIN_ENTRY_NAME);
		zip.putNextEntry(zipEntry);
		writer.newRecord();
		writer.writeInt(Model.VERSION);
		writer.writeString(model.systemName);
		writer.writeLong(model.timestamp);
		writer.writeInt(model.countryName);
		writer.writeInt(model.cityName);
		writer.writeModelLocation(model.location);
		writer.writeStringArray(model.locales);
		writer.writeString(model.localeCurrent);
		writer.writeInt(model.textLength);
		writer.newRecord();
		writer.writeIntArray(model.authors);
		writer.newRecord();
		writer.writeIntArray(model.comments);
		writer.newRecord();
		writer.writeIntArray(model.delays);
		writer.newRecord();
		writer.flush();
		zip.closeEntry();
		
		serializeTransportMaps(model, zip, writer);
		serializeTransportLines(model, zip, writer);
		serializeTransportStations(model, zip, writer);
		serializeTransportSegments(model, zip, writer);
		serializeTransportTransfers(model, zip, writer);

		serializeMaps(model, zip, writer);
		
		serializeTexts(model, zip);
		
	}

	private void serializeMaps(Model model, ZipOutputStream zip, CsvWriter writer) throws IOException {
		for(MapView obj : model.views){
			String entryName = String.format(MAP_ENTRY_NAME, obj.systemName);
			ZipEntry zipEntry = new ZipEntry(entryName);
			zip.putNextEntry(zipEntry);

			writer.writeInt(obj.id);
			writer.writeString(obj.systemName);
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
			writer.newRecord();

			writer.writeInt(obj.lines.length);
			writer.writeInt(obj.stations.length);
			writer.writeInt(obj.segments.length);
			writer.writeInt(obj.transfers.length);
			writer.newRecord();
			
			for(LineView v : obj.lines){
				writer.writeInt(v.id);
				writer.writeInt(v.lineId);
				writer.writeInt(v.lineColor);
				writer.writeInt(v.labelColor);
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
			writer.writeString(obj.systemName);
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
			writer.writeInt(obj.lineFromId);
			writer.writeInt(obj.stationFromId);
			writer.writeInt(obj.lineToId);
			writer.writeInt(obj.stationToId);
			writer.writeInteger(obj.delay);
			writer.writeInt(obj.flags);
			writer.newRecord();
		}
		writer.flush();
		zip.closeEntry();
	}		
	
	private void serializeTexts(final Model model, final ZipOutputStream zip) throws IOException {
		final int len = model.locales.length;
		final int textsLen = model.textLength;
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(zip));
		for(int i = 0; i < len; i++){
			String entryName = String.format(LOCALE_ENTRY_NAME, model.locales[i]);
			ZipEntry zipEntry = new ZipEntry(entryName);
			zip.putNextEntry(zipEntry);
			
			final String[] texts = model.localeTexts[i]; 
			for(int j = 0; j < textsLen; j++){
				writer.println( texts[j] );
			}
			writer.flush();
			zip.closeEntry();
		}
	}
}






