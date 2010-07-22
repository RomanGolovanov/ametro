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

import org.ametro.ApplicationEx;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.tasks.BaseTask;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TaskFailedList extends ListActivity {

	CatalogStorage mStorage;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mStorage = ((ApplicationEx)getApplicationContext()).getCatalogStorage();
		setListAdapter(new FailedTaskListAdapter(this, mStorage.takeFailedTaskList()));
	}
	
	private static class FailedTaskListAdapter extends BaseAdapter{

		ArrayList<BaseTask> mData;
		Context mContext;
		
		public FailedTaskListAdapter(Context context, ArrayList<BaseTask> data) {
			mContext = context;
			mData = data;
		}

		public int getCount() {
			return mData.size();
		}

		public Object getItem(int position) {
			return mData.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup viewGroup) {
			TextView v = new TextView(mContext);
			//v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 64));
			
			BaseTask task = mData.get(position);
			v.setText(task.toString());
			
			return v;
		}
		
	}
}
