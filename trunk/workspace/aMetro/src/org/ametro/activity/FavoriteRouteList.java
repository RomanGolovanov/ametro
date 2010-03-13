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

import static org.ametro.Constants.STATION_FROM_ID;
import static org.ametro.Constants.STATION_TO_ID;

import org.ametro.R;
import org.ametro.adapter.RouteFavoriteListAdapter;
import org.ametro.model.SubwayMap;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

public class FavoriteRouteList extends ListActivity {

	private Point[] mRoutes;
	private SubwayMap mSubwayMap;
	
	private static final int CONTEXT_MENU_SELECT = 0;
	private static final int CONTEXT_MENU_REMOVE = 1;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onBindData();
		registerForContextMenu(getListView());
	}


	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(ContextMenu.NONE, CONTEXT_MENU_SELECT, CONTEXT_MENU_SELECT, R.string.menu_select);
		menu.add(ContextMenu.NONE, CONTEXT_MENU_REMOVE, CONTEXT_MENU_REMOVE, R.string.menu_remove);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case CONTEXT_MENU_SELECT:
			onSelect((int)getSelectedItemId());
			return true;
		case CONTEXT_MENU_REMOVE:
			onRemove((int)getSelectedItemId());
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		onSelect(position);
		super.onListItemClick(l, v, position, id);
	}

	private void onSelect(int position) {
		Intent data = new Intent();
		Point r = mRoutes[position];
		data.putExtra(STATION_FROM_ID, r.x);
		data.putExtra(STATION_TO_ID, r.y);
		setResult(RESULT_OK, data);
		finish();
	}
	
	private void onRemove(int position){
		Point p = mRoutes[position];
		BrowseVectorMap.Instance.removeFavoriteRoute(p.x, p.y);
		onBindData();
	}
	
	private void onBindData() {
		mRoutes = BrowseVectorMap.Instance.getFavoriteRoutes();
		if(mRoutes==null || mRoutes.length == 0){
			Toast.makeText(this, R.string.msg_no_favorites, Toast.LENGTH_SHORT).show();
			finish();
		}
		mSubwayMap = BrowseVectorMap.Instance.getSubwayMap();
		final RouteFavoriteListAdapter adapter = new RouteFavoriteListAdapter(this, mRoutes, mSubwayMap);
		setListAdapter(adapter);
	}
		
}
