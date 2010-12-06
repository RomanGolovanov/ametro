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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.CatalogMapPair.CatalogMapPairCityComparator;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CatalogExpandableAdapter extends BaseExpandableListAdapter implements Filterable {

	protected Context mContext;
    protected LayoutInflater mInflater;
    
	protected ArrayList<CatalogMapPair> mObjects;
	protected ArrayList<CatalogMapPair> mOriginalValues;
	
	protected int mMode;
	protected String mLanguageCode;
	
	protected CountryInfo[] mCountries;
	protected HashMap<String,Drawable> mIcons;
    protected CatalogMapPair[][] mRefs;

    protected String[] mStates;
    protected int[] mStateColors;
    
    protected ICatalogStateProvider mStatusProvider;
    
    protected HashMap<Integer,Drawable> mTransportTypes;
    
    private boolean mShowCountryFlags;
    
	private CatalogFilter mFilter;
	private Object mLock = new Object();
	private String mSearchPrefix;
	private Drawable mNoCountryIcon;
	private final float mDisplayScale;

	private TreeMap<String,Long> mChildrenIds = new TreeMap<String, Long>();
	private TreeMap<String,Long> mGroupsIds = new TreeMap<String, Long>();
	
	private long mNextChildId;
	private long mNextGroupId;
	
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
    
    public CatalogExpandableAdapter(Context context, Catalog local, Catalog remote, int mode, int colorsArray, ICatalogStateProvider statusProvider) {
        mContext = context;
		mDisplayScale = mContext.getResources().getDisplayMetrics().density;
        mNoCountryIcon = context.getResources().getDrawable(R.drawable.no_country);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mStates = context.getResources().getStringArray(R.array.catalog_map_states);
		mStateColors = context.getResources().getIntArray(colorsArray);
		mStatusProvider = statusProvider;
		mMode = mode;
		mTransportTypes = TransportType.getIconsMap(context);
		
    	mObjects = CatalogMapPair.diff(local, remote, mode);
        bindData();
    }

    public Object getChild(int groupPosition, int childPosition) {
        return mRefs[groupPosition][childPosition];
    }

    public long getChildId(int groupPosition, int childPosition) {
    	if(mRefs==null || mRefs.length<groupPosition || mRefs[groupPosition]==null || mRefs[groupPosition].length<childPosition) return -1;
    	String mapSystemName = mRefs[groupPosition][childPosition].getSystemName(); 
        Long id = mChildrenIds.get(mapSystemName);
        if(id == null){
        	id = mNextChildId++;
        	mChildrenIds.put(mapSystemName,id);
        }
        return id;
    }

    public int getChildrenCount(int groupPosition) {
        return mRefs[groupPosition].length;
    }

	public static class ViewHolder {
		TextView mCity;
		TextView mCountryISO;
		TextView mStatus;
		TextView mSize;
		ImageView mIsoIcon;
		LinearLayout mImageContainer;
		LinearLayout mCountryFlagContainer;
	}    

	public static class GroupViewHolder {
		TextView mCountry;
	}    
    
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

    	ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.catalog_expandable_list_item, null);
			holder = new ViewHolder();
			holder.mCity = (TextView) convertView.findViewById(android.R.id.text1);
			holder.mCountryISO = (TextView) convertView.findViewById(R.id.country);
			holder.mStatus = (TextView) convertView.findViewById(R.id.state);
			holder.mSize = (TextView) convertView.findViewById(R.id.size);
			holder.mIsoIcon = (ImageView) convertView.findViewById(R.id.iso_icon);
			holder.mImageContainer = (LinearLayout) convertView.findViewById(R.id.icons);
			holder.mCountryFlagContainer = (LinearLayout) convertView.findViewById(R.id.country_flag_panel);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final String code = mLanguageCode;
		final CatalogMapPair ref = mRefs[groupPosition][childPosition];
		final int state = mStatusProvider.getCatalogState(ref.getLocal(), ref.getRemote());
		holder.mCity.setText(ref.getCity(code));
		holder.mStatus.setText(mStates[state]);
		holder.mStatus.setTextColor(mStateColors[state]);
		holder.mSize.setText( StringUtil.formatFileSize(ref.getSize(),0) );

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

    public Object getGroup(int groupPosition) {
        return mCountries[groupPosition];
    }

    public int getGroupCount() {
        return mCountries.length;
    }

    public long getGroupId(int groupPosition) {
    	if(mCountries==null || mCountries.length<groupPosition) return -1;
    	String countryName = mCountries[groupPosition].Id; 
        Long id = mGroupsIds.get(countryName);
        if(id == null){
        	id = mNextGroupId++;
        	mGroupsIds.put(countryName,id);
        }
        return id;    
	}

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    	GroupViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.catalog_list_group_item, null);
			holder = new GroupViewHolder();
			holder.mCountry = (TextView) convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}    	
		holder.mCountry.setText(mCountries[groupPosition].Name);
		return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }
    
    protected void bindData() {
        mShowCountryFlags = GlobalSettings.isCountryIconsEnabled(mContext);
    	final String code= GlobalSettings.getLanguage(mContext); 
    	mLanguageCode = code;
    	TreeSet<CountryInfo> countries = new TreeSet<CountryInfo>();
    	HashSet<String> addedCountries = new HashSet<String>();
        TreeMap<String, ArrayList<CatalogMapPair>> index = new TreeMap<String, ArrayList<CatalogMapPair>>();
        CatalogMapPairCityComparator comparator = new CatalogMapPairCityComparator(code);

        for(CatalogMapPair diff : mObjects){
        	final String country = diff.getCountry(code);
        	if(!addedCountries.contains(country)){
        		addedCountries.add(country);
        		
            	final String countryId = diff.getCountry(Constants.LOCALE_EN);
            	CountryInfo info = new CountryInfo();
            	info.Id = countryId;
            	info.Name = country;
            	countries.add(info);
        	}

        	ArrayList<CatalogMapPair> cities = index.get(country);
        	if(cities == null){
        		cities = new ArrayList<CatalogMapPair>();
        		index.put(country,cities);
        	}
        	cities.add(diff); 
        }
        mCountries = (CountryInfo[]) countries.toArray(new CountryInfo[countries.size()]);
        mIcons = new HashMap<String, Drawable>();
        mRefs = new CatalogMapPair[mCountries.length][];

        int lenc = mCountries.length;
        for(int i=0;i<lenc;i++){
        	final String country = mCountries[i].Name;
        	final ArrayList<CatalogMapPair> diffSet = index.get(country);
			if(diffSet!=null){        	
	        	int len = diffSet.size();
	        	CatalogMapPair[] arr = (CatalogMapPair[]) diffSet.toArray(new CatalogMapPair[len]);
	        	Arrays.sort(arr, comparator);
	        	mRefs[i] = arr;
			}
        }
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
            notifyDataSetChanged();
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

	public long findItemPosition(CityDirectory.Entity city) {
		if(city == null || mObjects == null) return -1;
		final String code = mLanguageCode;
		final String cityName = city.getName(code);
		final int countriesLen = mCountries.length;
		for(int group=0; group<countriesLen; group++ ){
			CatalogMapPair[] cities = mRefs[group];
			final int len = cities.length;
			for(int child=0; child<len; child++){
				CatalogMapPair item = cities[child];
				if(item!=null &&  cityName.equalsIgnoreCase(item.getCity(code))){
					return ExpandableListView.getPackedPositionForChild(group, child);
				}
			}
		}
		return -1;
	}
	
	private static class CountryInfo implements Comparable<CountryInfo>
	{
		public String Id;
		public String Name;

		public int compareTo(CountryInfo another) {
			return Name.compareTo(another.Name);
		}
	}
}
