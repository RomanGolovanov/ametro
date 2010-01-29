package com.ametro.pmz;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class GenericResource implements IResource {

	private class Parser
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
			mKeys.add(section);
			mSections.put(section, new Hashtable<String, String>());
		}		
		private void handleNaveValuePair(String section, String name, String value) {
			mSections.get(section).put(name, value);
		}

	}
	
	@Override
	public void beginInitialize(FilePackage owner) {
		mKeys = new ArrayList<String>();
		mSections = new Hashtable<String, Dictionary<String,String>>();
		mParser = new Parser();
		
	}

	@Override
	public void doneInitialize() {
		mParser = null;
	}

	@Override
	public void parseLine(String line) {
		mParser.parseLine(line);
	}

	public String[] getSections(){
		return (String[]) mKeys.toArray(new String[mKeys.size()]);
	}
	
	public Dictionary<String,String> getSection(String sectionName)
	{
		return mSections.get(sectionName);
	}
	
	public String getValue(String sectionName, String parameter){
		Dictionary<String, String> section = mSections.get(sectionName);
		return section !=null ? section.get(parameter) : null;
	}
	
	private ArrayList<String> mKeys;
	private Dictionary<String, Dictionary<String, String>> mSections;
	private Parser mParser;
	private long mCrc;

	@Override
	public long getCrc() {
		return mCrc;
	}

	@Override
	public void setCrc(long crc) {
		mCrc = crc;
	}
}
