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
package org.ametro.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.ametro.MapSettings;
import org.ametro.model.Deserializer;
import org.ametro.model.StationAddon;
import org.ametro.model.SubwayStation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BrowseStation extends Activity {

	private StationAddon mStationAddon;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SubwayStation station = BrowseVectorMap.Instance.getCurrentStation();
		String mapName = MapSettings.getMapName();
		String mapFileName = MapSettings.getMapFileName(mapName);
		
		try {
			mStationAddon = Deserializer.tryDeserializeAddon(station, new FileInputStream(mapFileName));
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "No addon for station", Toast.LENGTH_SHORT).show();
			finish();
			return;
		} catch (IOException e) {
			Toast.makeText(this, "Addon corrupted", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if(mStationAddon==null){
			Toast.makeText(this, "No addon for station", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		ArrayList<String> text = new ArrayList<String>();
		for(StationAddon.Entry entry : mStationAddon.entries){
			String caption = entry.caption;
			StringBuilder sb = new StringBuilder();
			sb.append(caption);
			sb.append("\n");
			for(String line : entry.text){
				sb.append(line);
				sb.append("\n");
			}
			text.add(sb.toString());
		}
		
		ListView list = new ListView(this);
		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, text));

		setContentView(list);
	}
}
