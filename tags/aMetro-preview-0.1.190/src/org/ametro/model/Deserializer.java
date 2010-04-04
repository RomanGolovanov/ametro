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

import static org.ametro.Constants.LOG_TAG_MAIN;
import static org.ametro.model.Serializer.ADDONS_DIR_ENTRY_NAME;
import static org.ametro.model.Serializer.CITY_ENTRY_NAME;
import static org.ametro.model.Serializer.ENCODING;
import static org.ametro.model.Serializer.LINES_ENTRY_NAME;
import static org.ametro.model.Serializer.MAP_ENTRY_NAME;
import static org.ametro.model.Serializer.SEGMENTS_ENTRY_NAME;
import static org.ametro.model.Serializer.SEGMENTS_POINTS_ENTRY_NAME;
import static org.ametro.model.Serializer.STATIONS_ENTRY_NAME;
import static org.ametro.model.Serializer.TRANSFERS_ENTRY_NAME;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ametro.Constants;
import org.ametro.util.StringUtil;
import org.ametro.util.csv.CsvReader;

import android.graphics.Point;
import android.util.Log;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 11.02.2010
 *         Time: 0:06:46
 */
public class Deserializer {

	private static HashSet<String> mMainCityEntries = new HashSet<String>();
	
	static {
		mMainCityEntries.add(CITY_ENTRY_NAME);
		mMainCityEntries.add(MAP_ENTRY_NAME);
		mMainCityEntries.add(LINES_ENTRY_NAME);
		mMainCityEntries.add(STATIONS_ENTRY_NAME);
		mMainCityEntries.add(SEGMENTS_ENTRY_NAME);
		mMainCityEntries.add(TRANSFERS_ENTRY_NAME);
		mMainCityEntries.add(SEGMENTS_POINTS_ENTRY_NAME);		
	}
	
	public static City deserialize(InputStream in) throws IOException {
		return deserialize(in, false);
	}

	public static City deserializeDescription(InputStream in) throws IOException {
		return deserialize(in, true);
	}

	public static City deserialize(InputStream in, boolean descriptionOnly) throws IOException {
		long startTime = System.currentTimeMillis();
		//Debug.startMethodTracing("ametro");
		ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(in, Constants.DEFAULT_BUFFER_SIZE));


		City city = null;
		SubwayMap subwayMap = null;
		SubwayLine[] lines = null;
		SubwayStation[] stations = null;
		SubwaySegment[] segments = null;
		SubwayTransfer[] transfers = null;
		HashMap<Integer, Point[]> pointsBySegmentId = null;

		ZipEntry zipEntry;
		while( (zipEntry = zipIn.getNextEntry()) != null) {
			final String name = zipEntry.getName();
			if(mMainCityEntries.contains(name)){
			CsvReader csvReader = new CsvReader(new BufferedReader(new InputStreamReader(zipIn,ENCODING)));
			if (csvReader.next()) {
				int version = csvReader.getInt(1);
				if (CITY_ENTRY_NAME.equals(name)) {
					city = deserializeCity(csvReader, version);
					if (descriptionOnly) {
						zipIn.closeEntry();
						zipIn.close();
						return city;
					}
				} else if (MAP_ENTRY_NAME.equals(name)) {
					subwayMap = deserializeMap(csvReader, version);
				} else if (LINES_ENTRY_NAME.equals(name)) {
					lines = deserializeLines(csvReader, version);
				} else if (STATIONS_ENTRY_NAME.equals(name)) {
					stations = deserializeStations(csvReader, version);
				} else if (SEGMENTS_ENTRY_NAME.equals(name)) {
					segments = deserializeSegments(csvReader, version);
				} else if (TRANSFERS_ENTRY_NAME.equals(name)) {
					transfers = deserializeTransfers(csvReader, version);
				} else if (SEGMENTS_POINTS_ENTRY_NAME.equals(name)) {
					pointsBySegmentId = deserializeSegmentsPoints(csvReader, version);
				}
			}
			}
			zipIn.closeEntry();
		}

		zipIn.close();

		if (city != null && subwayMap != null) {
			city.subwayMap = subwayMap;
			subwayMap.lines = lines;
			subwayMap.stations = stations;
			subwayMap.segments = segments;
			subwayMap.transfers = transfers;
			subwayMap.pointsBySegmentId = pointsBySegmentId;
			if (segments != null) {
				HashMap<Integer, ArrayList<SubwaySegment>> segmentsByStationId =
					new HashMap<Integer, ArrayList<SubwaySegment>>();
				for (SubwaySegment segment : segments) {
					int stationId = segment.fromStationId;
					ArrayList<SubwaySegment> stationSegments = segmentsByStationId.get(stationId);
					if (stationSegments == null) {
						stationSegments = new ArrayList<SubwaySegment>();
						segmentsByStationId.put(stationId, stationSegments);
					}
					stationSegments.add(segment);

					stationId = segment.toStationId;
					stationSegments = segmentsByStationId.get(stationId);
					if (stationSegments == null) {
						stationSegments = new ArrayList<SubwaySegment>();
						segmentsByStationId.put(stationId, stationSegments);
					}
					stationSegments.add(segment);
				}
				HashMap<Integer, SubwaySegment[]> result =
					new HashMap<Integer, SubwaySegment[]>();
					for (Integer stationId : segmentsByStationId.keySet()) {
						ArrayList<SubwaySegment> stationSegments = segmentsByStationId.get(stationId);
						result.put(stationId, stationSegments.toArray(new SubwaySegment[stationSegments.size()]));
					}
					subwayMap.segmentsByStationId = result;
			}
		}

		if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
			Log.i(LOG_TAG_MAIN, "City loading time is " + (System.currentTimeMillis() - startTime) + "ms");
		}

		localize(city);

		//Debug.stopMethodTracing();
		return city;
	}

	public static StationAddon tryDeserializeAddon(SubwayStation station, InputStream in) throws IOException {

		long startTime = System.currentTimeMillis();
		ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(in, Constants.DEFAULT_BUFFER_SIZE));

		StationAddon addon = null;
		final String addonName = ADDONS_DIR_ENTRY_NAME + station.id + ".csv"; 

		ZipEntry zipEntry;
		while( (zipEntry = zipIn.getNextEntry()) != null) {
			String name = zipEntry.getName();
			if (addonName.equals(name)) {
				CsvReader csvReader = new CsvReader(new BufferedReader(new InputStreamReader(zipIn,ENCODING)));
				if (csvReader.next()) {
					int version = csvReader.getInt(1);
					addon = deserializeStationAddon(csvReader, version);
					zipIn.closeEntry();
					break;
				} 
			}
			zipIn.closeEntry();
		}
		zipIn.close();

		if (Log.isLoggable(LOG_TAG_MAIN, Log.INFO)) {
			Log.i(LOG_TAG_MAIN, "Station addon loading time is " + (System.currentTimeMillis() - startTime) + "ms");
		}

		return addon;
	}

	private static void localize(City city) {
		Locale locale = Locale.getDefault();
		if(locale.equals(Locale.ENGLISH)){

			city.cityName = StringUtil.toTranslit(city.cityName);
			city.countryName = StringUtil.toTranslit(city.countryName);

			SubwayMap map = city.subwayMap;
			map.cityName = StringUtil.toTranslit(map.cityName);
			map.countryName = StringUtil.toTranslit(map.countryName);

			for(SubwayLine line : map.lines){
				line.name = StringUtil.toTranslit(line.name);
			}

			for(SubwayStation station : map.stations){
				station.name = StringUtil.toTranslit(station.name);
			}
		}
	}

	private static StationAddon deserializeStationAddon(CsvReader csvReader, int version) throws IOException {
		StationAddon addon = new StationAddon();
		if (csvReader.next()) {
			switch (version) {
			case StationAddon.VERSION:
				addon.stationId =  csvReader.readInt();
				ArrayList<StationAddon.Entry> entries = new ArrayList<StationAddon.Entry>();
				while(csvReader.next()){
					StationAddon.Entry entry = new StationAddon.Entry();
					csvReader.readString(); // skip first column;
					entry.id = csvReader.readInt();
					entry.caption = csvReader.readString();
					int lineCount = csvReader.readInt();
					entry.text = new String[lineCount];
					for(int i = 0; i < lineCount && csvReader.next(); i++){
						entry.text[i] = csvReader.readString();
					}
					entries.add(entry);
				}
				addon.entries = (StationAddon.Entry[]) entries.toArray(new StationAddon.Entry[entries.size()]);
				break;
			default:
				throw new IllegalStateException("Cant parse file: unsupported version");
			}
		}
		return addon;
	}



	private static City deserializeCity(CsvReader csvReader, int version) throws IOException {
		City city = new City();
		if (csvReader.next()) {
			switch (version) {
			case City.VERSION:
				city.countryName = csvReader.readString();
				city.cityName = csvReader.readString();
				city.width = csvReader.readInt();
				city.height = csvReader.readInt();
				city.timestamp = csvReader.readLong();
				city.sourceVersion = csvReader.readLong();
				break;
			default:
				throw new IllegalStateException("Cant parse file: unsupported version");
			}
		}
		return city;
	}

	private static SubwayMap deserializeMap(CsvReader csvReader, int version) throws IOException {
		SubwayMap subwayMap = new SubwayMap();
		if (csvReader.next()) {
			switch (version) {
			case SubwayMap.VERSION:
				subwayMap.id = csvReader.readInt();
				subwayMap.mapName = csvReader.readString();
				subwayMap.cityName = csvReader.readString();
				subwayMap.countryName = csvReader.readString();
				subwayMap.width = csvReader.readInt();
				subwayMap.height = csvReader.readInt();
				subwayMap.stationDiameter = csvReader.readInt();
				subwayMap.linesWidth = csvReader.readInt();
				subwayMap.wordWrap = csvReader.readBoolean();
				subwayMap.upperCase = csvReader.readBoolean();
				break;
			default:
				throw new IllegalStateException("Cant parse file: unsupported version");
			}
		}
		return subwayMap;
	}

	private static SubwayLine[] deserializeLines(CsvReader csvReader, int version) throws IOException {
		ArrayList<SubwayLine> lines = new ArrayList<SubwayLine>();
		switch (version) {
		case SubwayLine.VERSION:
			while (csvReader.next()) {
				SubwayLine line = new SubwayLine();
				line.id = csvReader.readInt();
				line.name = csvReader.readString();
				line.color = csvReader.readInt();
				line.labelColor = csvReader.readInt();
				line.labelBgColor = csvReader.readInt();
				lines.add(line);
			}
			break;
		default:
			throw new IllegalStateException("Cant parse file: unsupported version");
		}
		SubwayLine[] result = new SubwayLine[lines.size()];
		for (SubwayLine line : lines) {
			result[line.id] = line;
		}
		return result;
	}

	private static SubwayStation[] deserializeStations(CsvReader csvReader, int version) throws IOException {
		ArrayList<SubwayStation> stations = new ArrayList<SubwayStation>();
		switch (version) {
		case SubwayStation.VERSION:
			while (csvReader.next()) {
				SubwayStation station = new SubwayStation();
				station.id = csvReader.readInt();
				station.name = csvReader.readString();
				station.rect = csvReader.readRect();
				station.point = csvReader.readPoint();
				station.lineId = csvReader.readInt();
				stations.add(station);
			}
			break;
		default:
			throw new IllegalStateException("Cant parse file: unsupported version");
		}
		SubwayStation[] result = new SubwayStation[stations.size()];
		for (SubwayStation station : stations) {
			result[station.id] = station;
		}
		return result;
	}

	private static SubwaySegment[] deserializeSegments(CsvReader csvReader, int version) throws IOException {
		ArrayList<SubwaySegment> segments = new ArrayList<SubwaySegment>();
		switch (version) {
		case SubwaySegment.VERSION:
			while (csvReader.next()) {
				SubwaySegment segment = new SubwaySegment();
				segment.id = csvReader.readInt();
				segment.delay = csvReader.readNullableDouble();
				segment.fromStationId = csvReader.readInt();
				segment.toStationId = csvReader.readInt();
				segment.flags = csvReader.readInt();
				segments.add(segment);
			}
			break;
		default:
			throw new IllegalStateException("Cant parse file: unsupported version");
		}
		SubwaySegment[] result = new SubwaySegment[segments.size()];
		for (SubwaySegment segment : segments) {
			result[segment.id] = segment;
		}
		return result;
	}

	private static SubwayTransfer[] deserializeTransfers(CsvReader csvReader, int version) throws IOException {
		ArrayList<SubwayTransfer> transfers = new ArrayList<SubwayTransfer>();
		switch (version) {
		case SubwayTransfer.VERSION:
			while (csvReader.next()) {
				SubwayTransfer transfer = new SubwayTransfer();
				transfer.id = csvReader.readInt();
				transfer.delay = csvReader.readNullableDouble();
				transfer.fromStationId = csvReader.readInt();
				transfer.toStationId = csvReader.readInt();
				transfer.flags = csvReader.readInt();
				transfers.add(transfer);
			}
			break;
		default:
			throw new IllegalStateException("Cant parse file: unsupported version");
		}
		SubwayTransfer[] result = new SubwayTransfer[transfers.size()];
		for (SubwayTransfer transfer : transfers) {
			result[transfer.id] = transfer;
		}
		return result;
	}

	private static HashMap<Integer, Point[]> deserializeSegmentsPoints(CsvReader csvReader, int version) throws IOException {
		HashMap<Integer, Point[]> pointsBySegmentId = new HashMap<Integer, Point[]>();
		switch (version) {
		case SubwaySegment.VERSION:
			while (csvReader.next()) {
				int segmentId = csvReader.readInt();
				Point[] points = csvReader.readPointArray();
				pointsBySegmentId.put(segmentId, points);
			}
			break;
		default:
			throw new IllegalStateException("Cant parse file: unsupported version");
		}
		return pointsBySegmentId;
	}

}
