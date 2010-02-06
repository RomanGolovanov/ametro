package org.ametro.pmz;

import java.util.ArrayList;
import java.util.Hashtable;

import org.ametro.libs.Helpers;


public class TransportResource implements IResource {

	public static class TransportLine {
		public String mName;
		public String mMapName;
		public String mStationText;
		public String mDrivingDelaysText;
		public String mTimeDelaysText;
	}
	
	public static class TransportTransfer {

		public String mName;
		public String mStartLine;
		public String mStartStation;
		public String mEndLine;
		public String mEndStation;
		
		public Double mDelay;
		public String mStatus;
	}
	
	private class TransportParser
	{
		private String mSection = null;
		private TransportLine mLine = null;

		public void parseLine(String line)
		{
			if(line.startsWith(";")) return;
			if(line.startsWith("[") && line.endsWith("]")){
				mSection = line.substring(1,line.length()-1);
				handleSection(mSection);
			}else if (line.contains("=")){
				String[] parts = line.split("=");
				if(parts.length==2){
					String name = parts[0].trim();
					String value = parts.length > 1 ? parts[1].trim() : "";
					handleNaveValuePair(mSection, name, value);
				}
			}
		}

		private void handleSection(String section) {
			if(section.equals("Options")){
				// do nothing ^__^
			}else if(section.equals("Transfers")){
			}else{ // Lines names
				// add line
				mLine = new TransportLine();
			}
		}		
		private void handleNaveValuePair(String section, String name, String value) {
			if(section.equals("Options")){
				if(name.equals("Type")){
					mType = value;
				}
			}else if(section.equals("Transfers")){
				String[] parts = Helpers.splitCSV( value );
				TransportTransfer transfer = new TransportTransfer();
				transfer.mStartLine = parts[0].trim();
				transfer.mStartStation = parts[1].trim();
				transfer.mEndLine = parts[2].trim();
				transfer.mEndStation = parts[3].trim();
				transfer.mDelay = parts.length>4 && parts[4].length()>0 ? Double.parseDouble(parts[4]) : null;
				transfer.mStatus = parts.length>5 ? parts[5] : null;
				mTransfers.add(transfer);
			}else{ // Lines names
				if(name.equals("Name")){
					mLine.mName = value;
					mLines.put(mLine.mName, mLine); 
				}else if(name.equals("LineMap")){
					mLine.mMapName = value;
				}else if(name.equals("Stations")){
					mLine.mStationText = value;
				}else if(name.equals("Driving")){
					mLine.mDrivingDelaysText = value;
				}else if(name.equals("Delays")){
					mLine.mTimeDelaysText = value;
				}
			}
		}

	}

	public void beginInitialize(FilePackage owner) {
		//this.owner = owner;
		this.mLines = new Hashtable<String, TransportLine>();
		this.mTransfers = new ArrayList<TransportTransfer>();
		mParser = new TransportParser();
	}

	public void doneInitialize() {
		mParser = null;
	}

	public void parseLine(String line) {
		mParser.parseLine(line.trim());
	}

	public TransportResource(){

	}

	public String getType(){
		return mType;
	}
	
	public Hashtable<String, TransportLine> getLines(){
		return mLines;
	}
	
	public ArrayList<TransportTransfer> getTransfers(){
		return mTransfers;
	}
	
	//private FilePackage owner;
	private TransportParser mParser;

	private String mType;
	
	private Hashtable<String, TransportLine> mLines;
	private ArrayList<TransportTransfer> mTransfers;
	private long mCrc;

	public long getCrc() {
		return mCrc;
	}

	public void setCrc(long crc) {
		mCrc = crc;
	}	

	
}
