package com.ametro.libs;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FileGroupsDictionary implements Serializable{
	
	private static final long serialVersionUID = -4607416163824014049L;

	private class StringArray extends TreeSet<String> implements Serializable {

		private static final long serialVersionUID = 59732808049508726L;

	}

	private TreeMap<String, StringArray> groups = new TreeMap<String, StringArray>();
	private TreeMap<String, String> pathes = new TreeMap<String, String>();
	private long timestamp;
	
	public TreeMap<String, String> getPathes() {
		return pathes;
	}

	public void setPathes(TreeMap<String, String> pathes) {
		this.pathes = pathes;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setGroups(TreeMap<String, StringArray> groups) {
		this.groups = groups;
	}

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