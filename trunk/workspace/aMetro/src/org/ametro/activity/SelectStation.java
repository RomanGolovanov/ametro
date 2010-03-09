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
import java.util.Collections;
import java.util.Comparator;

import org.ametro.R;
import org.ametro.adapter.StationListAdapter;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class SelectStation extends ListActivity {

	public final static String STATION_ID = "STATION_ID";

	private final int MAIN_MENU_BY_NAME = 1;
	private final int MAIN_MENU_BY_LINE = 2;
	
	private SubwayMap mMap;
	private ArrayList<SubwayStation> mStations;
	private StationListAdapter mAdapter;
	
	private boolean mSortByName;
	private int mSelection;
	
	private class NameComparator implements Comparator<SubwayStation>
	{
		public int compare(SubwayStation left, SubwayStation right) {
			return left.name.compareTo(right.name);
		}
	}
	
	private class LineComparator implements Comparator<SubwayStation>
	{
		public int compare(SubwayStation left, SubwayStation right) {
			return mMap.lines[left.lineId].name.compareTo(mMap.lines[right.lineId].name);
		}
	}	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MAIN_MENU_BY_NAME, 0, R.string.menu_sort_by_name).setIcon(
				android.R.drawable.ic_menu_sort_alphabetically);
		menu.add(0, MAIN_MENU_BY_LINE, 1, R.string.menu_sort_by_line).setIcon(
				android.R.drawable.ic_menu_sort_by_size);

		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuByName = menu.findItem(MAIN_MENU_BY_NAME);
		MenuItem menuByLine = menu.findItem(MAIN_MENU_BY_LINE);
		menuByLine.setEnabled(mSortByName);
		menuByName.setEnabled(!mSortByName);
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_BY_NAME:
			updateSortOrder(true);
			return true;
		case MAIN_MENU_BY_LINE:
			updateSortOrder(false);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mSelection = -1;
		Intent data = getIntent();
		if(data!=null){
			int id = data.getIntExtra(STATION_ID, -1);
			if(id!=-1){
				mSelection = id;
			}
		}
		
		mMap = BrowseVectorMap.Instance.getSubwayMap();
		mStations = new ArrayList<SubwayStation>( mMap.stations.length );
		for(SubwayStation station : mMap.stations){
			mStations.add(station);
		}
		updateSortOrder(true);
		
		
		
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		SubwayStation station = mAdapter.getStation(position);
		Intent data = new Intent();
		data.putExtra(STATION_ID, station.id);
		setResult(RESULT_OK, data);
		finish();
		super.onListItemClick(l, v, position, id);
	}
	
	private void updateSortOrder(boolean byName){
		mSortByName = byName;
		if(mSortByName){
			Collections.sort(mStations, new NameComparator());
		}else{
			Collections.sort(mStations, new LineComparator());
		}
		mAdapter = new StationListAdapter(this, mStations, mMap);
		this.setListAdapter(mAdapter);
		updateSelection();
	}
	
	private void updateSelection(){
		if(mSelection!=-1){
			int position = mStations.indexOf(mMap.stations[mSelection]);
			setSelection(position);
		}
	}
}
