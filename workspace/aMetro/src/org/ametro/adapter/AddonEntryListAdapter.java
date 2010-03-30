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
package org.ametro.adapter;

import org.ametro.R;
import org.ametro.model.StationAddon;
import org.ametro.util.StringUtil;

import android.app.Activity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AddonEntryListAdapter extends BaseAdapter {


	private static class ListItemWrapper
	{
		public final TextView Caption;
		public final TextView Text;
		
		public ListItemWrapper(View view) {
			Caption = (TextView)view.findViewById(R.id.addon_entry_list_item_caption);
			Text = (TextView)view.findViewById(R.id.addon_entry_list_item_text);
			view.setTag(this);
		}
	}	


	public AddonEntryListAdapter(Activity activity, StationAddon addon){
		mContextActivity = activity;
		mAddon = addon;
	}
	
	protected final Activity mContextActivity;
	protected Integer mTextColor;
	protected StationAddon mAddon;

	public int getCount() {
		return mAddon.entries.length;
	}

	public void setTextColor(Integer color){
		mTextColor = color;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ListItemWrapper wrapper = null;
		
		if(convertView==null){
			view = mContextActivity.getLayoutInflater().inflate(R.layout.addon_entry_list_item, null);
			wrapper = new ListItemWrapper(view);
			if(mTextColor!=null){
				wrapper.Caption.setTextColor(mTextColor);
				//wrapper.Line.setTextColor(mTextColor);
			}
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}

		final StationAddon.Entry entry = mAddon.entries[position];
		wrapper.Caption.setText(entry.caption);

		Spanned text = Html.fromHtml(StringUtil.join(entry.text, "<br/>").replace("\\n", "<br/>"));
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
		wrapper.Text.setText(spannable);
		
		return view;		
	}

	public Object getItem(int position) {
		return mAddon.entries[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
}
