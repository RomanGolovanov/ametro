package org.ametro.libs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.util.Log;

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
	
	public int getGroupCount(){
		return groups.keySet().size();
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

	public static void write(FileGroupsDictionary data, String fileName) {
		ObjectOutputStream strm = null;
		try{
			strm = new ObjectOutputStream(new FileOutputStream(fileName));
			strm.writeObject(data);
			strm.flush();
		}catch(Exception ex){
			Log.e("aMetro", "Failed write map cache", ex);
		}finally{
			if(strm!=null){
				try {
					strm.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static FileGroupsDictionary read(String fileName)
	{
		File file = new File(fileName);
		if(!file.exists()) return null;
		ObjectInputStream strm = null;
		try{
			try {
				strm = new ObjectInputStream(new FileInputStream(file));
				FileGroupsDictionary map = (FileGroupsDictionary) strm.readObject();
				Log.i("aMetro", "Loaded map cache");
				return map;
			} catch (Exception ex) {
				Log.i("aMetro", "Cannot load map cache");
				return null;
			}
		} finally{
			if(strm!=null){
				try {
					strm.close();
				} catch (IOException e) {
				}
			}
		}

	}


}