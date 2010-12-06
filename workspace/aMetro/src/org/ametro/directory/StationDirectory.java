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
package org.ametro.directory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.app.Constants;
import org.ametro.directory.CityStationDictionary.Entity;
import org.ametro.model.ext.ModelLocation;
import org.ametro.util.DateUtil;
import org.ametro.util.csv.CsvReader;

import android.content.Context;

public  class StationDirectory
{
	public StationDirectory(Context context){
		mContext = context;
	}
	
	public long getTimestamp(File pmzFile){
		File gpsFile = new File(pmzFile.getAbsolutePath().replace(".pmz", ".gps"));
		if(gpsFile.exists()){
			return DateUtil.toUTC(gpsFile.lastModified());
		}else{
			return Constants.MODEL_IMPORT_TIMESTAMP;
		}
	}
	
	public CityStationDictionary get(File pmzFile){
		try {
			InputStream strm;
			File gpsFile = new File(pmzFile.getAbsolutePath().replace(".pmz", ".gps"));
			ArrayList<String> comments = null;
			if(gpsFile.exists()){
				strm = new FileInputStream(gpsFile);
			}else if (mContext!=null){
				String assetName = pmzFile.getName().replace(".pmz", ".gps").toLowerCase();
				strm = mContext.getAssets().open(assetName);
			}else{
				return null;
			}
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),';');
			HashMap<String, HashMap<String,Entity>> index = new HashMap<String, HashMap<String,Entity>>();
			while(reader.next()){
				if(reader.isComment()){
					String comment = reader.getEntireRecord();
					if(comment.startsWith("#")){
						if(comments==null){
							comments = new ArrayList<String>();
						}
						comments.add(comment.substring(1));
					}
				}else{
					Entity r = new Entity(reader.getString(1),reader.getString(2), reader.getCount()>=5 ? new ModelLocation(reader.getFloat(3), reader.getFloat(4) ) : null );
					HashMap<String, Entity> city2rec = index.get(r.getLineSystemName());
					if(city2rec==null){
						city2rec = new HashMap<String, Entity>();
						index.put(r.getLineSystemName(), city2rec);
					}
					city2rec.put(r.getStationSystemName(), r);
				}
			}
			return new CityStationDictionary(index, comments);
		} catch (Throwable e) {
		}
		return null;
	}
	
	private Context mContext;
}
