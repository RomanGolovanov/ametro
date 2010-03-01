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

import java.util.ArrayList;
import java.util.List;

import org.ametro.MapSettings;
import org.ametro.R;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class CreateRoute extends Activity implements OnClickListener{

	private AutoCompleteTextView mFromText;
	private AutoCompleteTextView mToText;
	
	private Button mSwapButton;
	private Button mCreateButton;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_route);

		mSwapButton = (Button)findViewById(R.id.create_route_swap_button);
		mCreateButton = (Button)findViewById(R.id.create_route_create_button);
		
		mSwapButton.setOnClickListener(this);
		mCreateButton.setOnClickListener(this);
		
		mFromText = (AutoCompleteTextView) findViewById(R.id.create_route_from_text);
		mToText = (AutoCompleteTextView) findViewById(R.id.create_route_to_text);

		List<String> stations = new ArrayList<String>();
		SubwayMap map = MapSettings.getModel();
		SubwayStation[] data = map.stations;
		for(SubwayStation station : data)
		{
			stations.add(station.name + " (" + map.getLine(station.lineId).name + ")");
		}

		ArrayAdapter<String> stationNameAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, stations);
		
		mFromText.setAdapter(stationNameAdapter);
		mToText.setAdapter(stationNameAdapter);

	}

	public void onClick(View v) {
		if(v == mSwapButton){
			Editable swap = mFromText.getText();
			mFromText.setText(mToText.getText());
			mToText.setText(swap);
			mSwapButton.requestFocus();
		}
		if(v == mCreateButton){
			finish();
		}
		
	}
	
}
