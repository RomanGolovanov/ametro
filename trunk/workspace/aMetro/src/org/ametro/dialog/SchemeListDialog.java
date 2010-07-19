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
package org.ametro.dialog;

import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.model.TransportType;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class SchemeListDialog implements OnClickListener {

	private AlertDialog mDialog;
	private Context mContext;
	
	private String[] mTransportLineNames;
	
	private String[] mTexts;
	private ArrayList<ArrayList<ListItem>> mChildren;
	private String[] mSystemNames;

	private static class ListItem
	{
		public String Name;
		public String SystemName;
		public long Type;
		public ArrayList<ListItem> Children;
	}
	
	private ArrayList<ListItem> parseModel(final Model model){
		HashMap<Long,ListItem> index = new HashMap<Long,ListItem>();
		ArrayList<ListItem> list = new ArrayList<ListItem>();
		
		final int len = model.views.length;
		for(int i=0;i<len;i++){
			boolean isMain = model.viewIsMain[i];
			String systemName = model.viewSystemNames[i];
			String name = Model.getLocalizedString(model, model.viewNames[i] );
			long type = model.viewTransportTypes[i];
			if(isMain){
				ListItem item = index.get(type);
				if(item == null){
					item = new ListItem();
					index.put(type, item);
				}
				item.Name = name;
				item.SystemName = systemName;
				item.Type = type;
				item.Children = null;
				
				list.add(item);
			}else{
				ListItem parent = index.get(type);
				if(parent == null){
					parent = new ListItem();
					index.put(type, parent);
				}
				if(parent.Children == null){
					parent.Children = new ArrayList<ListItem>();
				}
				ListItem child = new ListItem();
				child.Name = name;
				child.SystemName = systemName;
				child.Type = type;
				child.Children = null;
				parent.Children.add(child);
			}
		}	
		if(list.size()==0){
			for(int i=0;i<len;i++){
				String systemName = model.viewSystemNames[i];
				String name = Model.getLocalizedString(model, model.viewNames[i] );
				long type = model.viewTransportTypes[i];
				ListItem item = new ListItem();
				item.Name = name;
				item.SystemName = systemName;
				item.Type = type;
				item.Children = null;
				
				list.add(item);
				
			}
		}
		return list;
	}
	
	private void prepareData(ArrayList<ListItem> data){
		final ArrayList<String> names = new ArrayList<String>();
		final ArrayList<String> systemNames = new ArrayList<String>();
		final ArrayList<ArrayList<ListItem>> children = new ArrayList<ArrayList<ListItem>>();
		for(ListItem item : data){
			names.add(item.Name);
			systemNames.add(item.SystemName);
			children.add(null);
			if(item.Children!=null){
				names.add( mTransportLineNames[TransportType.getTransportTypeResource((int)item.Type)] );
				systemNames.add(null);
				children.add(item.Children);
			}
		}
		mSystemNames = (String[]) systemNames.toArray(new String[systemNames.size()]);
		mTexts = (String[]) names.toArray(new String[names.size()]);
		mChildren = children;
	}
	
	public SchemeListDialog(final Context context, Model model, MapView current) {
		mContext = context;
		mTransportLineNames = mContext.getResources().getStringArray(R.array.transport_type_lines);
		createDialog( parseModel(model));
	}

	private void createDialog(ArrayList<ListItem> data) {
		prepareData(data);
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.msg_select_scheme);
		builder.setCancelable(true);
		builder.setIcon(android.R.drawable.ic_dialog_map);
		builder.setItems(mTexts, this);
		mDialog = builder.create();
	}

	public void onMapViewSelected(String mapViewSystemName){
		mDialog.dismiss();
	}

	public void onClick(DialogInterface dialog, int which) {
		ArrayList<ListItem> children = mChildren.get(which);
		if(children!=null){
			mDialog.dismiss();
			createDialog(children);
			mDialog.show();
		}else{
			onMapViewSelected(mSystemNames[which]);
		}
	}

	public void show() {
		mDialog.show();
	}
	
}
