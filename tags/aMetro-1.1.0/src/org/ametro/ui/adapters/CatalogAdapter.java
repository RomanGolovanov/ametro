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
 */package org.ametro.ui.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.CatalogMapPair.CatalogMapPairCityComparator;
import org.ametro.catalog.CatalogMapPair.CatalogMapPairCountryComparator;
import org.ametro.directory.CityDirectory;
import org.ametro.model.TransportType;
import org.ametro.util.BitmapUtil;
import org.ametro.util.StringUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CatalogAdapter extends BaseAdapter implements Filterable {

	public static final int SORT_MODE_CITY = 1;
	public static final int SORT_MODE_COUNTRY = 2;
	
	protected Context mContext;
    protected LayoutInflater mInflater;
    
	protected ArrayList<CatalogMapPair> mObjects;
	protected ArrayList<CatalogMapPair> mOriginalValues;
	
	protected int mMode;
	protected int mSortMode;
	protected String mLanguageCode;

	protected HashMap<String,Drawable> mIcons;

    protected String[] mStates;
    protected int[] mStateColors;
    
    protected ICatalogStateProvider mStatusProvider;
    
    protected HashMap<Integer,Drawable> mTransportTypes;
    
    private boolean mShowCountryFlags;
    
	private CatalogFilter mFilter;
	private final Object mLock = new Object();
	private String mSearchPrefix;
	private final Drawable mNoCountryIcon;
	private final float mDisplayScale;

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
	
    public CatalogAdapter(Context context, Catalog local, Catalog remote, int mode, int colorsArray, ICatalogStateProvider statusProvider, int sortMode) {
        mContext = context;
		mDisplayScale = mContext.getResources().getDisplayMetrics().density;
        mNoCountryIcon = context.getResources().getDrawable(R.drawable.no_country);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mStates = context.getResources().getStringArray(R.array.catalog_map_states);
		mStateColors = context.getResources().getIntArray(colorsArray);
		mStatusProvider = statusProvider;
		mMode = mode;
		mSortMode = sortMode;
		mTransportTypes = TransportType.getIconsMap(context);
    	mObjects = CatalogMapPair.diff(local, remote, mode);
        bindData();
    }

	public static class ViewHolder {
		TextView mCity;
		TextView mCountry;
		TextView mCountryISO;
		TextView mStatus;
		ImageView mIsoIcon;
		LinearLayout mImageContainer;
		LinearLayout mCountryFlagContainer;
	}    

    public boolean hasStableIds() {
        return false;
    }
    
    protected void bindData() {
        mShowCountryFlags = GlobalSettings.isCountryIconsEnabled(mContext);
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
		    final String cityName = value.getCity(code);
		    final String countryName = value.getCountry(code);

		    // First match against the whole, non-splitted value
		    if (StringUtil.startsWithoutDiacritics(cityName,prefixString) || StringUtil.startsWithoutDiacritics(countryName,prefixString)) {
		        newValues.add(value);
		    } else {
		    	boolean added = false;
		        final String[] cityWords = cityName.split(" ");
		        final int cityWordCount = cityWords.length;

		        for (int k = 0; k < cityWordCount; k++) {
		            if (StringUtil.startsWithoutDiacritics(cityWords[k],prefixString)) {
		                newValues.add(value);
		                added = true;
		                break;
		            }
		        }
		        
		        if(!added){
			        final String[] countryWords = countryName.split(" ");
			        final int countryWordCount = countryWords.length;

			        for (int k = 0; k < countryWordCount; k++) {
			            if (StringUtil.startsWithoutDiacritics(countryWords[k],prefixString)) {
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
			convertView = mInflater.inflate(R.layout.catalog_list_item, null);
			holder = new ViewHolder();
			holder.mCity = (TextView) convertView.findViewById(android.R.id.text1);
			holder.mCountry = (TextView) convertView.findViewById(R.id.country);
			holder.mStatus = (TextView) convertView.findViewById(R.id.state);
			holder.mCountryISO = (TextView) convertView.findViewById(R.id.country_iso);
			holder.mIsoIcon = (ImageView) convertView.findViewById(R.id.iso_icon);
			holder.mImageContainer = (LinearLayout) convertView.findViewById(R.id.icons);
			holder.mCountryFlagContainer = (LinearLayout) convertView.findViewById(R.id.country_flag_panel);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final String code = mLanguageCode;
		final CatalogMapPair ref = mObjects.get(position);
		final int state = mStatusProvider.getCatalogState(ref.getLocal(), ref.getRemote());
		holder.mCity.setText(ref.getCity(code));
		holder.mCountry.setText(ref.getCountry(code));
		holder.mStatus.setText(mStates[state]);
		holder.mStatus.setTextColor(mStateColors[state]);
		
		if(mShowCountryFlags){
			final String iso = ref.getCountryISO();
			holder.mCountryISO.setText( iso );
			Drawable d = mIcons.get(iso);
			if(d == null){
				File file = new File(Constants.ICONS_PATH, iso + ".png");
				if(file.exists()){
					Bitmap bmp = BitmapUtil.createScaledBitmap(file.getAbsolutePath(), mDisplayScale, false);
					d = new BitmapDrawable(bmp);
				}else{
					d = mNoCountryIcon;
				}
				mIcons.put(iso, d);
			}
			holder.mIsoIcon.setImageDrawable(d);
			holder.mCountryFlagContainer.setVisibility(View.VISIBLE);
		}else{
			holder.mCountryFlagContainer.setVisibility(View.GONE);
		}
		
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

	public int findItemPosition(String systemMapName) {
		if(systemMapName == null || mObjects == null) return -1;
		int pos = 0;
		for(CatalogMapPair item : mObjects){
			if(item!=null && systemMapName.equalsIgnoreCase(item.getSystemName())){
				return pos;
			}
			pos++;
		}
		return -1;
	}

	public int findItemPosition(CityDirectory.Entity city) {
		if(city == null || mObjects == null) return -1;
		int pos = 0;
		final String code = mLanguageCode;
		final String cityName = city.getName(code);
		for(CatalogMapPair item : mObjects){
			if(item!=null &&  cityName.equalsIgnoreCase(item.getCity(code))){
				return pos;
			}
			pos++;
		}
		return -1;
	}	
}
