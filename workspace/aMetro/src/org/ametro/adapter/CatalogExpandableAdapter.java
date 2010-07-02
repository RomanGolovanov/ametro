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
 */package org.ametro.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ametro.GlobalSettings;
import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.CatalogMapPair.CatalogMapDifferenceCityNameComparator;
import org.ametro.model.TransportType;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CatalogExpandableAdapter extends BaseExpandableListAdapter {

	protected Catalog mLocal;
	protected Catalog mRemote;
	protected int mMode;
	
	protected String mLanguageCode;
	protected Context mContext;
    protected LayoutInflater mInflater;
	protected List<CatalogMapPair> mData;
	
	protected String[] mCountries;
    protected CatalogMapPair[][] mRefs;

    protected String[] mStates;
    protected int[] mStateColors;
    
    protected ICatalogStateProvider mStatusProvider;
    
    protected HashMap<Integer,Drawable> mTransportTypes;

    public CatalogMapPair getData(int groupId, int childId) {
        return mRefs[groupId][childId];
    }

    public String getLanguage(){
    	return mLanguageCode;
    }
    
    public void setLanguage(String languageCode)
    {
    	mLanguageCode = languageCode;
    }
    
	public void updateData(Catalog local, Catalog remote)
	{
		mLocal = local;
		mRemote = remote;
		mData = CatalogMapPair.diff(local, remote, mMode);
		mLanguageCode = GlobalSettings.getLanguage(mContext); 
		bindData(mLanguageCode);
		notifyDataSetChanged();
	}
    
    public CatalogExpandableAdapter(Context context, Catalog local, Catalog remote, int mode, int colorsArray, ICatalogStateProvider statusProvider) {
        mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mStates = context.getResources().getStringArray(R.array.catalog_map_states);
		mStateColors = context.getResources().getIntArray(colorsArray);
		mStatusProvider = statusProvider;
		
		mLocal = local;
		mRemote = remote;
		mMode = mode;
		mData = CatalogMapPair.diff(local, remote, mode);
		
		mLanguageCode = GlobalSettings.getLanguage(mContext); 
		bindTransportTypes();
		
        bindData(mLanguageCode);
		
    }

    public Object getChild(int groupPosition, int childPosition) {
        return mRefs[groupPosition][childPosition];
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return mRefs[groupPosition].length;
    }

    public TextView getGenericView() {
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(lp);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setPadding(36, 0, 0, 0);
        return textView;
    }

	public static class ViewHolder {
		TextView mText;
		TextView mStatus;
		LinearLayout mImageContainer;
	}    
    
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

    	ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.catalog_list_item, null);
			holder = new ViewHolder();
			holder.mText = (TextView) convertView.findViewById(R.id.text);
			holder.mStatus = (TextView) convertView.findViewById(R.id.state);
			holder.mImageContainer = (LinearLayout) convertView.findViewById(R.id.icons);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final String code = mLanguageCode;
		final CatalogMapPair ref = mRefs[groupPosition][childPosition];
		final int state = mStatusProvider.getCatalogState(ref.getLocal(), ref.getRemote());
		holder.mText.setText(ref.getCity(code));
		holder.mStatus.setText(mStates[state]);
		holder.mStatus.setTextColor(mStateColors[state]);
		
		final LinearLayout ll = holder.mImageContainer;
		ll.removeAllViews();
		long transports = ref.getTransports();
		int transportId = 1;
		while(transports>0){
			if((transports % 2)>0){
				ImageView img = new ImageView(mContext);
				img.setImageDrawable(mTransportTypes.get(transportId));
				ll.addView( img );
			}
			transports = transports >> 1;
			transportId = transportId << 1;
		}
		
		return convertView;
		
    }

    public Object getGroup(int groupPosition) {
        return mCountries[groupPosition];
    }

    public int getGroupCount() {
        return mCountries.length;
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView textView = getGenericView();
        textView.setText(getGroup(groupPosition).toString());
        return textView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }
    
    protected void bindTransportTypes(){
		mTransportTypes = new HashMap<Integer, Drawable>();
		final Resources res = mContext.getResources();
		mTransportTypes.put( TransportType.UNKNOWN_ID , res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.UNKNOWN_ID))  );
		mTransportTypes.put( TransportType.METRO_ID , res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.METRO_ID))  );
		mTransportTypes.put( TransportType.TRAM_ID , res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.TRAM_ID))  );
		mTransportTypes.put( TransportType.BUS_ID , res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.BUS_ID))  );
		mTransportTypes.put( TransportType.TRAIN_ID , res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.TRAIN_ID))  );
		mTransportTypes.put( TransportType.WATER_BUS_ID , res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.WATER_BUS_ID))  );
		mTransportTypes.put( TransportType.TROLLEYBUS_ID , res.getDrawable(GlobalSettings.getTransportTypeWhiteIconId(TransportType.TROLLEYBUS_ID))  );
    }
    
    protected void bindData(String code) {
        TreeSet<String> countries = new TreeSet<String>();
        TreeMap<String, ArrayList<CatalogMapPair>> index = new TreeMap<String, ArrayList<CatalogMapPair>>();
        CatalogMapDifferenceCityNameComparator comparator = new CatalogMapDifferenceCityNameComparator(code);

        for(CatalogMapPair diff : mData){
        	final String country = diff.getCountry(code);
        	countries.add(country);
        	ArrayList<CatalogMapPair> cities = index.get(country);
        	if(cities == null){
        		cities = new ArrayList<CatalogMapPair>();
        		index.put(country,cities);
        	}
        	cities.add(diff); 
        }
        mCountries = (String[]) countries.toArray(new String[countries.size()]);
        mRefs = new CatalogMapPair[mCountries.length][];
        for(int i=0; i<mCountries.length;i++){
        	String country = mCountries[i];
        	ArrayList<CatalogMapPair> diffSet = index.get(country);
			if(diffSet!=null){        	
	        	int len = diffSet.size();
	        	mRefs[i] = (CatalogMapPair[]) diffSet.toArray(new CatalogMapPair[len]);
	        	Arrays.sort(mRefs[i], comparator);
			}
        }
	}
}
