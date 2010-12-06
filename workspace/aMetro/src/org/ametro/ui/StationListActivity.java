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

package org.ametro.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.model.SchemeView;
import org.ametro.model.StationView;
import org.ametro.ui.adapters.StationListAdapter;
import org.ametro.util.StringUtil;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class StationListActivity extends ListActivity {

	private final int MAIN_MENU_BY_NAME = 1;
	private final int MAIN_MENU_BY_LINE = 2;
	
	private SchemeView mMap;
	private ArrayList<StationView> mStations;
	private StationListAdapter mAdapter;
	
	private static boolean mSortByName;
	private int mSelection;
	
	static{
		mSortByName = true;
	}
	
	private class NameComparator implements Comparator<StationView>
	{
		public int compare(StationView left, StationView right) {
			return StringUtil.COLLATOR.compare(left.getName(),right.getName());
		}
	}
	
	private class LineComparator implements Comparator<StationView>
	{
		public int compare(StationView left, StationView right) {
			int lineCompare = StringUtil.COLLATOR.compare( mMap.lines[left.lineViewId].getName(), mMap.lines[right.lineViewId].getName());
			return (lineCompare!=0) ? lineCompare : StringUtil.COLLATOR.compare(left.getName(),right.getName()); 
		}
	}	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MAIN_MENU_BY_NAME, 0, R.string.menu_sort_by_name).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		menu.add(0, MAIN_MENU_BY_LINE, 1, R.string.menu_sort_by_line).setIcon(android.R.drawable.ic_menu_sort_by_size);
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
			int id = data.getIntExtra(Constants.STATION_ID, -1);
			if(id!=-1){
				mSelection = id;
			}
		}
		mMap = MapViewActivity.Instance.getMapView();
		mStations = mMap.getStationList(false);
		updateSortOrder(mSortByName);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		StationView station = mAdapter.getStation(position);
		Intent data = new Intent();
		data.putExtra(Constants.STATION_ID, station.id);
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
