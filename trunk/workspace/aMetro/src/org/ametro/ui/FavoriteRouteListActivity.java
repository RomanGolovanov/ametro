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

import static org.ametro.app.Constants.STATION_FROM_ID;
import static org.ametro.app.Constants.STATION_TO_ID;

import org.ametro.R;
import org.ametro.model.SchemeView;
import org.ametro.ui.adapters.FavoriteRoutesListAdapter;

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
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FavoriteRouteListActivity extends Activity implements OnClickListener, OnItemClickListener {

	private Point[] mRoutes;
	private SchemeView mMapView;
	
	private ListView mList;
	private View mDeletePanel;
	
	private Button mDelete;
	private Button mCancel;
	
	private FavoriteRoutesListAdapter mAdapter;
	
	private static final int CONTEXT_MENU_SELECT = 0;
	private static final int CONTEXT_MENU_REMOVE = 1;
	
	private static final int MENU_REMOVE = 0;
	
	private TranslateAnimation mPanelAnimation;
	
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
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_REMOVE).setVisible(!mDeletePanelVisible);
		return super.onPrepareOptionsMenu(menu);
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
		MapViewActivity.Instance.removeFavoriteRoute(p.x, p.y);
		onBindData();
	}
	
	private void onBindData() {
		mRoutes = MapViewActivity.Instance.getFavoriteRoutes();
		if(mRoutes==null || mRoutes.length == 0){
			Toast.makeText(this, R.string.msg_no_favorites, Toast.LENGTH_SHORT).show();
			finish();
		}
		mMapView = MapViewActivity.Instance.getMapView();
		mAdapter = new FavoriteRoutesListAdapter(this, mRoutes, mMapView);
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
		mPanelAnimation = new TranslateAnimation(0, 0, mDeletePanel.getHeight(),0);
		mPanelAnimation.setDuration(350);
		mDeletePanel.startAnimation(mPanelAnimation);
		
		mDeletePanel.setVisibility(View.VISIBLE);
		mAdapter.setCheckboxesVisible(true);
		mDeletePanelVisible = true;
		unregisterForContextMenu(mList);
	}
	
	private void hideDeletePanel(){
		mPanelAnimation = new TranslateAnimation(0, 0, 0, mDeletePanel.getHeight());
		mPanelAnimation.setDuration(350);
		mDeletePanel.startAnimation(mPanelAnimation);

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
			final boolean[] checked = mAdapter.getChecked();
			final int len = mRoutes.length;
			for(int i = 0; i < len; i++){
				if(checked[i]){
					final Point r = mRoutes[i];
					MapViewActivity.Instance.removeFavoriteRoute(r.x, r.y);
				}
			}
			onBindData();
		}
	}
	
}
