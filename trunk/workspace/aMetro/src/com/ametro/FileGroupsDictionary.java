package com.ametro;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

public class FileGroupsDictionary {
	
	private static final long serialVersionUID = 1272092376968306671L;

	private class StringArray extends ArrayList<String>{
		private static final long serialVersionUID = -1990141892280002055L;

	}

	private TreeMap<String, StringArray> groups = new TreeMap<String, StringArray>();
	private TreeMap<String, String> pathes = new TreeMap<String, String>();

	
	public void putFile(String groupName, String label, String path){
		StringArray c = groups.get(groupName);
		if(c==null){
			groups.put(groupName, c = new StringArray());
		}
		c.add(label);
		String pathId = groupName+";"+label;
		pathes.put(pathId, path);
	}

	public String getFile(String groupName, String label){
		String pathId = groupName+";"+label;
		return pathes.get(pathId);
	}

	public String[] getGroups(){
		Set<String> keys = groups.keySet();
		return (String[]) keys.toArray(new String[keys.size()]);
	}

	public String[] getLabels(String groupName){
		StringArray c = groups.get(groupName);
		return (String[]) (c).toArray(new String[c.size()]);
	}

	public String[] getPathes(String groupName, String[] labels){
		String[] vals = new String[labels.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = getFile(groupName, labels[i]);
		}
		return vals;
	}

	//public fillTables() 	

}