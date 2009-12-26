package com.ametro.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class MapResource implements IResource {

	private class MapParser
	{
		private String section = null;

		public void parseLine(String line)
		{
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
				transportLines.put(section, new TransportLine());
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

			}else{ // Lines names
				TransportLine line = transportLines.get(section);

				if(name.equals("Color")){
					line.linesColor = Integer.parseInt(value,16);
				}else if(name.equals("LabelsColor")){
					line.labelColor = Integer.parseInt(value,16);
				}else if(name.equals("Coordinates")){
					line.coordinates = Helpers.parsePointArray(value);
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
		this.owner = owner;
		this.transportLines = new Hashtable<String, TransportLine>();
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

	public TextResource[] getTextResources() throws IOException {
		if(textResources==null) loadTextResources();
		return textResources;
	}
	public VectorResource[] getVectorResources() throws IOException {
		if(vectorResources==null) loadVectorResources();
		return vectorResources;
	}

	public ImageResource[] getImageResources() throws IOException {
		if(imageResources==null) loadImageResources();
		return imageResources;
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

	public ArrayList<String> getTransportNames() {
		return transportNames;
	}

	public VectorResource getVectorResource() throws IOException{
		return getVectorResources()[0];
	}

	public Hashtable<String, TransportLine> getTransportLines() {
		return transportLines;
	}

	private void loadTextResources() throws IOException {
		textResources = new TextResource[0];
	}

	private void loadVectorResources() throws IOException {
		vectorResources = new VectorResource[1];
		vectorResources[0] = owner.getVectorResource(this.vectorName);
	}

	private void loadImageResources() throws IOException {
		imageResources = new ImageResource[0];
	}

	private FilePackage owner;
	private MapParser parser;

	private int stationDiameter;
	private int linesWidth;
	private boolean upperCase;
	private boolean wordWrap;
	private boolean isVector;
	private String vectorName;
	private String transportName;
	private ArrayList<String> transportNames;

	private TextResource[] textResources;
	private VectorResource[] vectorResources;
	private ImageResource[] imageResources;

	private Hashtable<String, TransportLine> transportLines;

	public int getWidth() {
		return 1050;
	}

	public int getHeight() {
		return 1220;
	}


}
