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
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapPairEx;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.CatalogMapPair.CatalogMapPairCityComparator;
import org.ametro.catalog.CatalogMapPair.CatalogMapPairCountryComparator;
import org.ametro.model.TransportType;
import org.ametro.util.BitmapUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CheckedCatalogAdapter extends BaseAdapter { 

	public static class ViewHolder {
		CheckedTextView mCity;
		TextView mCountry;
		TextView mCountryISO;
		TextView mStatus;
		ImageView mIsoIcon;
		LinearLayout mImageContainer;
		LinearLayout mCountryFlagContainer;
	}    
	
	public static final int SORT_MODE_CITY = 1;
	public static final int SORT_MODE_COUNTRY = 2;
	
	private Context mContext;
	private LayoutInflater mInflater;
    
	private ArrayList<CatalogMapPairEx> mObjects;
	private int mSortMode;
	private String mLanguageCode;

	private int[] mStates;
	private String[] mStateNames;
	private int[] mStateColors;
	private ICatalogStateProvider mStatusProvider;
    
	private HashMap<String,Drawable> mIcons;
	private HashMap<Integer,Drawable> mTransportTypes;
    
    private boolean mShowCountryFlags;
	
	private Drawable mNoCountryIcon;
	private ListView mListView;
	private float mDisplayScale;

    public CatalogMapPairEx getData(int itemId) {
        return mObjects.get(itemId);
    }

    public CheckedCatalogAdapter(Context context, ListView owner, ArrayList<CatalogMapPairEx> objects, int colorsArray, ICatalogStateProvider statusProvider, int sortMode) {
        mContext = context;
		mDisplayScale = mContext.getResources().getDisplayMetrics().density;
        mListView = owner;
        mNoCountryIcon = context.getResources().getDrawable(R.drawable.no_country);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mStateNames = context.getResources().getStringArray(R.array.catalog_map_states);
		mStateColors = context.getResources().getIntArray(colorsArray);
		mStatusProvider = statusProvider;
		mSortMode = sortMode;
		mTransportTypes = TransportType.getIconsMap(context);
		mObjects = objects;
		bindData();
    }

    public boolean hasStableIds() {
        return false;
    }
    

    protected void bindData() {
        mShowCountryFlags = GlobalSettings.isCountryIconsEnabled(mContext);
    	final String code = GlobalSettings.getLanguage(mContext); 
    	mLanguageCode = code;
    	Comparator<CatalogMapPair> comparator;
    	if(mSortMode == SORT_MODE_CITY){
    		comparator  = new CatalogMapPairCityComparator(code);
    	}else{
    		comparator  = new CatalogMapPairCountryComparator(code);
    	}
        mIcons = new HashMap<String, Drawable>();
        Collections.sort(mObjects, comparator);
        
        final int len = mObjects.size();
        mStates = new int[len];
        for(int i=0;i<len;i++){
        	CatalogMapPairEx ref = mObjects.get(i);
        	mStates[i] = mStatusProvider.getCatalogState(ref.getLocal(), ref.getRemote());
        }
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
			convertView = mInflater.inflate(R.layout.catalog_list_item_check, null);
			holder = new ViewHolder();
			holder.mCity = (CheckedTextView) convertView.findViewById(android.R.id.text1);
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
		final CatalogMapPairEx ref = mObjects.get(position);
		final int state = mStates[position];
		holder.mCity.setText(ref.getCity(code));
		holder.mCountry.setText(ref.getCountry(code));
		holder.mStatus.setText(mStateNames[state]);
		holder.mStatus.setTextColor(mStateColors[state]);
		
		if(ref.isCheckable()){
			holder.mCity.setChecked(mListView.isItemChecked(position));
			holder.mCity.setEnabled(true);
		}else{
			holder.mCity.setChecked( false );
			holder.mCity.setEnabled(false);
		}
		
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

}
