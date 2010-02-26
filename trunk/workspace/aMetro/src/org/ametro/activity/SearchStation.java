package org.ametro.activity;

import java.util.ArrayList;

import org.ametro.MapSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

public class SearchStation extends Activity {

	private View mSearchControls;
	private AutoCompleteTextView mSearchEdit;
	private ImageButton mSearchButton;

	private SubwayMap mSubwayMap;

	public boolean onSearchRequested() {
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSubwayMap = MapSettings.getModel();
		if (mSubwayMap == null) {
			finish();
			return;
		}
		setContentView(R.layout.search_station);

		mSearchEdit = (AutoCompleteTextView) findViewById(R.id.search_station_edit);
		mSearchEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String text = parent.getItemAtPosition(position).toString();
				Intent data = new Intent();
				data.setData(MapUri.createSearch(text));
				setResult(RESULT_OK, data);
				finish();
			}
			
		});

		mSearchButton = (ImageButton) findViewById(R.id.search_station_button);
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSearchEdit.setText("");
			}
		});
		
		mSearchControls = (View) findViewById(R.id.search_station_controls);
		mSearchControls.setVisibility(View.INVISIBLE);

	}
	
	protected void onStart() {
		TranslateAnimation anim = new TranslateAnimation(0, 0, -mSearchControls.getHeight(), 0);
		anim.setDuration(1000);
		mSearchControls.startAnimation(anim);
		mSearchControls.setVisibility(View.VISIBLE);
		
		ArrayList<String> stationList = new ArrayList<String>();
		for (SubwayStation station : mSubwayMap.stations) {
			stationList.add(station.name + " (" + mSubwayMap.getLine(station.lineId).name + ")");
		}
		mSearchEdit.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, stationList));
		
		super.onStart();
	}
}
