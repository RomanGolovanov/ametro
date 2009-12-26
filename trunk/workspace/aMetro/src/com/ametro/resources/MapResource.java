package com.ametro.resources;

import java.util.ArrayList;
import java.util.Hashtable;

public class MapResource implements IResource {

	private class MapParser
	{
		private String section = null;

		public void parseLine(String line)
		{
			if(line.startsWith(";")) return;
			if(line.startsWith("[") && line.endsWith("]")){
				section = line.substring(1,line.length()-1);
				handleSection(section);
			}else if (line.contains("=")){
				String[] parts = line.split("=");
				if(parts.length==2){
					String name = parts[0].trim();
					String value = parts.length > 1 ? parts[1].trim() : "";
					handleNaveValuePair(section, name, value);
				}
			}
		}

		private void handleSection(String section) {
			if(section.equals("Options")){
				// do nothing ^__^
			}else if(section.equals("AdditionalNodes")){
				// also
			}else{ // Lines names
				// add line
				mapLines.put(section, new MapLine());
			}
		}		
		private void handleNaveValuePair(String section, String name, String value) {
			if(section.equals("Options")){
				if(name.equals("ImageFileName")){
					vectorName = value;
				}else if(name.equals("StationDiameter")){
					stationDiameter = Integer.parseInt(value);
				}else if(name.equals("LinesWidth")){
					linesWidth = Integer.parseInt(value);
				}else if(name.equals("UpperCase")){
					upperCase = Boolean.parseBoolean(value);
				}else if(name.equals("WordWrap")){
					wordWrap = Boolean.parseBoolean(value);
				}else if(name.equals("IsVector")){
					isVector = Boolean.parseBoolean(value);
				}else if(name.equals("Transports")){
					transportNames = Helpers.parseStringArray(value);
				}else if(name.equals("CheckedTransports")){
					transportName = value;
				}
			}else if(section.equals("AdditionalNodes")){
				String[] parts = Helpers.parseStringArray(value);
				MapAddiditionalLine line = new MapAddiditionalLine();
				line.mName = name;
				line.mLineName = parts[0];
				line.mFromStationName = parts[1];
				line.mToStationName = parts[2];
				line.mPoint = Helpers.parsePoint(parts[3] + "," + parts[4]);
				line.mIsSpline = parts.length > 5 && parts[5].contains("spline");
				addiditionalLines.add(line);
			}else{ // Lines names
				MapLine line = mapLines.get(section);

				if(name.equals("Color")){
					line.linesColor = Integer.parseInt(value,16);
				}else if(name.equals("LabelsColor")){
					line.labelColor = Integer.parseInt(value,16);
				}else if(name.equals("Coordinates")){
					line.coordinates = Helpers.parsePointArray(value);
					if(line.coordinates!=null){
						stationCount+= line.coordinates.length - 1;
					}
				}else if(name.equals("Rects")){
					line.rectangles = Helpers.parseRectangleArray(value);
				}else if(name.equals("Heights")){
					line.heights = Helpers.parseDoubleArray(value);
				}else if(name.equals("Rect")){
					line.rectangle = Helpers.parseRectangle(value);
				}
			}
		}

	}

	public void beginInitialize(FilePackage owner) {
		this.mapLines = new Hashtable<String, MapLine>();
		this.addiditionalLines = new ArrayList<MapAddiditionalLine>();
		parser = new MapParser();
	}

	public void doneInitialize() {
		parser = null;
	}

	public void parseLine(String line) {
		parser.parseLine(line.trim());
	}

	public MapResource(){

	}

	public ArrayList<MapAddiditionalLine> getAddiditionalLines() {
		return addiditionalLines;
	}
	
	public int getStationDiameter() {
		return stationDiameter;
	}

	public int getLinesWidth() {
		return linesWidth;
	}

	public boolean isUpperCase() {
		return upperCase;
	}

	public boolean isWordWrap() {
		return wordWrap;
	}

	public boolean isVector() {
		return isVector;
	}

	public String getVectorName() {
		return vectorName;
	}

	public String getTransportName() {
		return transportName;
	}

	public String[] getTransportNames() {
		return transportNames;
	}
	
	public Hashtable<String, MapLine> getMapLines() {
		return mapLines;
	}

	public int getStationCount(){
		return stationCount;
	}
	
	public int getAddiditionalStationCount(){
		return addiditionalStationCount;
	}

	private MapParser parser;

	private int stationCount = 0;
	private int addiditionalStationCount = 0;
	
	private int stationDiameter;
	private int linesWidth;
	private boolean upperCase;
	private boolean wordWrap;
	private boolean isVector;
	
	private String vectorName;
	private String transportName;
	
	private String[] transportNames;
	private Hashtable<String, MapLine> mapLines;
	private ArrayList<MapAddiditionalLine> addiditionalLines;

}
