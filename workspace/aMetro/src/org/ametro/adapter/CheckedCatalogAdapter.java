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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.CatalogMapPair.CatalogMapPairCityComparator;
import org.ametro.catalog.CatalogMapPair.CatalogMapPairCountryComparator;
import org.ametro.model.TransportType;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckedCatalogAdapter extends BaseAdapter implements Filterable { 

	public static final int SORT_MODE_CITY = 1;
	public static final int SORT_MODE_COUNTRY = 2;
	
	protected Context mContext;
    protected LayoutInflater mInflater;
    
	protected ArrayList<CatalogMapPair> mObjects;
	protected ArrayList<CatalogMapPair> mOriginalValues;
	
	protected int mItemId;
	
	protected int mMode;
	protected int mSortMode;
	protected String mLanguageCode;

	protected HashMap<String,Drawable> mIcons;

    protected String[] mStates;
    protected int[] mStateColors;
    
    protected ICatalogStateProvider mStatusProvider;
    
    protected HashMap<Integer,Drawable> mTransportTypes;
    
    protected CatalogFilter mFilter;
    protected Object mLock = new Object();
    protected String mSearchPrefix;
    protected Drawable mNoCountryIcon;
	
    protected HashSet<String> mCheckedItems;

    public CatalogMapPair getData(int itemId) {
        return mObjects.get(itemId);
    }

    public String getLanguage(){
    	return mLanguageCode;
    }
    
    public void setLanguage(String languageCode)
    {
    	mLanguageCode = languageCode;
    }
    
	public void updateLanguage() {
		bindData();
		notifyDataSetChanged();
	}    
    
	public void updateData(Catalog local, Catalog remote)
	{
        synchronized (mLock) {
        	mOriginalValues = CatalogMapPair.diff(local, remote, mMode);
            if (mSearchPrefix == null || mSearchPrefix.length() == 0) {
            	mObjects = new ArrayList<CatalogMapPair>(mOriginalValues);
            } else {
                mObjects = getFilteredData(mSearchPrefix);
            }
        }
		bindData();
		notifyDataSetChanged();
	}
    
	public void updateSort(int sortMode){
		mSortMode = sortMode;
		bindData();
		notifyDataSetChanged();
	}
	
    public CheckedCatalogAdapter(Context context, Catalog local, Catalog remote, int mode, int colorsArray, ICatalogStateProvider statusProvider, int sortMode) {
    	mItemId = R.layout.catalog_list_item_check;
        mContext = context;
        mNoCountryIcon = context.getResources().getDrawable(R.drawable.no_country);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mStates = context.getResources().getStringArray(R.array.catalog_map_states);
		mStateColors = context.getResources().getIntArray(colorsArray);
		mStatusProvider = statusProvider;
		mMode = mode;
		mSortMode = sortMode;
		mCheckedItems = new HashSet<String>();
		
    	mObjects = CatalogMapPair.diff(local, remote, mode);
        bindData();
		bindTransportTypes();
    }

	public static class ViewHolder {
		CheckedTextView mCity;
		TextView mCountry;
		TextView mCountryISO;
		TextView mStatus;
		ImageView mIsoIcon;
		LinearLayout mImageContainer;
	}    

    public boolean hasStableIds() {
        return false;
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
    
    protected void bindData() {
    	final String code= GlobalSettings.getLanguage(mContext); 
    	mLanguageCode = code;
    	Comparator<CatalogMapPair> comparator;
    	if(mSortMode == SORT_MODE_CITY){
    		comparator  = new CatalogMapPairCityComparator(code);
    	}else{
    		comparator  = new CatalogMapPairCountryComparator(code);
    	}
        mIcons = new HashMap<String, Drawable>();
        Collections.sort(mObjects, comparator);
	}

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CatalogFilter();
        }
        return mFilter;
	}
	
    private class CatalogFilter extends Filter {

    	protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
        	mSearchPrefix = prefix.toString();
            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<CatalogMapPair>(mObjects);
                }
            }
            if (prefix == null || prefix.length() == 0) {
                synchronized (mLock) {
                    ArrayList<CatalogMapPair> list = new ArrayList<CatalogMapPair>(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                final ArrayList<CatalogMapPair> newValues = getFilteredData(prefix);
                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mObjects = (ArrayList<CatalogMapPair>) results.values;
        	bindData();
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
	
	/*package*/ ArrayList<CatalogMapPair> getFilteredData(CharSequence prefix) {
		String prefixString = prefix.toString().toLowerCase();

		final ArrayList<CatalogMapPair> values = mOriginalValues;
		final int count = values.size();
		final String code = mLanguageCode;
		final ArrayList<CatalogMapPair> newValues = new ArrayList<CatalogMapPair>(count);

		for (int i = 0; i < count; i++) {
		    final CatalogMapPair value = values.get(i);
		    final String cityName = value.getCity(code).toString().toLowerCase();
		    final String countryName = value.getCountry(code).toString().toLowerCase();

		    // First match against the whole, non-splitted value
		    if (cityName.startsWith(prefixString) || countryName.startsWith(prefixString)) {
		        newValues.add(value);
		    } else {
		    	boolean added = false;
		        final String[] cityWords = cityName.split(" ");
		        final int cityWordCount = cityWords.length;

		        for (int k = 0; k < cityWordCount; k++) {
		            if (cityWords[k].startsWith(prefixString)) {
		                newValues.add(value);
		                added = true;
		                break;
		            }
		        }
		        
		        if(!added){
			        final String[] countryWords = countryName.split(" ");
			        final int countryWordCount = countryWords.length;

			        for (int k = 0; k < countryWordCount; k++) {
			            if (countryWords[k].startsWith(prefixString)) {
			                newValues.add(value);
			                break;
			            }
			        }
		        }
		        
		    }
		}
		return newValues;
	}
	
	
	public int getCount() {
		return mObjects.size();
	}

	public Object getItem(int position) {
		return mObjects.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup g) {
    	ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(mItemId, null);
			holder = new ViewHolder();
			holder.mCity = (CheckedTextView) convertView.findViewById(android.R.id.text1);
			holder.mCountry = (TextView) convertView.findViewById(R.id.country);
			holder.mStatus = (TextView) convertView.findViewById(R.id.state);
			holder.mCountryISO = (TextView) convertView.findViewById(R.id.country_iso);
			holder.mIsoIcon = (ImageView) convertView.findViewById(R.id.iso_icon);
			holder.mImageContainer = (LinearLayout) convertView.findViewById(R.id.icons);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final String code = mLanguageCode;
		final CatalogMapPair ref = mObjects.get(position);
		final String iso = ref.getCountryISO();
		final int state = mStatusProvider.getCatalogState(ref.getLocal(), ref.getRemote());
		holder.mCity.setText(ref.getCity(code));
		holder.mCountry.setText(ref.getCountry(code));
		holder.mStatus.setText(mStates[state]);
		holder.mStatus.setTextColor(mStateColors[state]);
		holder.mCountryISO.setText( iso );
		
		Drawable d = mIcons.get(iso);
		if(d == null){
			File file = new File(Constants.ICONS_PATH, iso + ".png");
			if(file.exists()){
				d = Drawable.createFromPath(file.getAbsolutePath());
			}else{
				d = mNoCountryIcon;
			}
			mIcons.put(iso, d);
		}
		holder.mIsoIcon.setImageDrawable(d);
		
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

	public int getSortMode() {
		return mSortMode;
	}
}
