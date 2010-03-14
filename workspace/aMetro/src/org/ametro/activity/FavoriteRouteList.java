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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FavoriteRouteList extends Activity implements OnClickListener, OnItemClickListener {

	private Point[] mRoutes;
	private SubwayMap mSubwayMap;
	
	private ListView mList;
	private View mDeletePanel;
	
	private Button mDelete;
	private Button mCancel;
	
	private RouteFavoriteListAdapter mAdapter;
	
	private static final int CONTEXT_MENU_SELECT = 0;
	private static final int CONTEXT_MENU_REMOVE = 1;
	
	private static final int MENU_REMOVE = 0;
	
	private boolean mDeletePanelVisible;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.favorite_route_list);
		
		mList = (ListView)findViewById(R.id.favorite_route_list_main);
		mDeletePanel = (View)findViewById(R.id.favorite_route_list_delete_panel);
		mDelete = (Button)findViewById(R.id.favorite_route_list_delete);
		mCancel = (Button)findViewById(R.id.favorite_route_list_cancel);
		
		mDelete.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mList.setOnItemClickListener(this);
		
		onBindData();
		hideDeletePanel();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mDeletePanelVisible){
				hideDeletePanel();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_REMOVE, 0, R.string.menu_remove).setIcon(android.R.drawable.ic_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case MENU_REMOVE:
				showDeletePanel();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(ContextMenu.NONE, CONTEXT_MENU_SELECT, CONTEXT_MENU_SELECT, R.string.menu_select);
		menu.add(ContextMenu.NONE, CONTEXT_MENU_REMOVE, CONTEXT_MENU_REMOVE, R.string.menu_remove);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case CONTEXT_MENU_SELECT:
			onSelect((int)mList.getSelectedItemId());
			return true;
		case CONTEXT_MENU_REMOVE:
			onRemove((int)mList.getSelectedItemId());
			return true;
		}
		return super.onContextItemSelected(item);
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
		mAdapter = new RouteFavoriteListAdapter(this, mRoutes, mSubwayMap);
		mList.setAdapter(mAdapter);
	}


	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if(!mDeletePanelVisible){
			onSelect(position);
		}else{
			mAdapter.toggleCheckbox(position);
		}
	}

	private void showDeletePanel(){
		mDeletePanel.setVisibility(View.VISIBLE);
		mAdapter.setCheckboxesVisible(true);
		mDeletePanelVisible = true;
		unregisterForContextMenu(mList);
	}
	
	private void hideDeletePanel(){
		mDeletePanel.setVisibility(View.GONE);
		mAdapter.setCheckboxesVisible(false);
		mDeletePanelVisible = false;
		registerForContextMenu(mList);
	}

	public void onClick(View v) {
		if(v == mCancel){
			hideDeletePanel();
		}
		if(v == mDelete){
			hideDeletePanel();
			// TODO: remove selected items!
			onBindData();
		}
	}
	
}
