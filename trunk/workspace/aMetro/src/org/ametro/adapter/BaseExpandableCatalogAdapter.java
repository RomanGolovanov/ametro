package org.ametro.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ametro.MapSettings;
import org.ametro.R;
import org.ametro.catalog.CatalogMapDifference;
import org.ametro.catalog.CatalogMapDifference.CatalogMapDifferenceCityNameComparator;
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

public abstract class BaseExpandableCatalogAdapter extends BaseExpandableListAdapter {

	protected String mLanguageCode;
	protected Context mContext;
    protected LayoutInflater mInflater;
	protected List<CatalogMapDifference> mData;
	
	protected String[] mCountries;
    protected CatalogMapDifference[][] mRefs;

    protected String[] mStates;
    protected int[] mStateColors;
    
    protected HashMap<Integer,Drawable> mTransportTypes;

    public CatalogMapDifference getData(int groupId, int childId) {
        return mRefs[groupId][childId];
    }

    public BaseExpandableCatalogAdapter(Context context, String code) {
        mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLanguageCode = code;
		bindTransportTypes();
    }

    public abstract int getState(CatalogMapDifference diff);

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
			convertView = mInflater.inflate(R.layout.browse_catalog_list_item, null);
			holder = new ViewHolder();
			holder.mText = (TextView) convertView.findViewById(R.id.browse_catalog_list_item_text);
			holder.mStatus = (TextView) convertView.findViewById(R.id.browse_catalog_list_item_status);
			holder.mImageContainer = (LinearLayout) convertView.findViewById(R.id.browse_catalog_list_item_icons);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final String code = mLanguageCode;
		final CatalogMapDifference ref = mRefs[groupPosition][childPosition];
		final int state = getState(ref);
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
		mTransportTypes.put( TransportType.UNKNOWN_ID , res.getDrawable(MapSettings.getTransportTypeWhiteIconId(TransportType.UNKNOWN_ID))  );
		mTransportTypes.put( TransportType.METRO_ID , res.getDrawable(MapSettings.getTransportTypeWhiteIconId(TransportType.METRO_ID))  );
		mTransportTypes.put( TransportType.TRAM_ID , res.getDrawable(MapSettings.getTransportTypeWhiteIconId(TransportType.TRAM_ID))  );
		mTransportTypes.put( TransportType.BUS_ID , res.getDrawable(MapSettings.getTransportTypeWhiteIconId(TransportType.BUS_ID))  );
		mTransportTypes.put( TransportType.TRAIN_ID , res.getDrawable(MapSettings.getTransportTypeWhiteIconId(TransportType.TRAIN_ID))  );
		mTransportTypes.put( TransportType.WATER_BUS_ID , res.getDrawable(MapSettings.getTransportTypeWhiteIconId(TransportType.WATER_BUS_ID))  );
		mTransportTypes.put( TransportType.TROLLEYBUS_ID , res.getDrawable(MapSettings.getTransportTypeWhiteIconId(TransportType.TROLLEYBUS_ID))  );
    }
    
    protected void bindData(String code) {
        TreeSet<String> countries = new TreeSet<String>();
        TreeMap<String, ArrayList<CatalogMapDifference>> index = new TreeMap<String, ArrayList<CatalogMapDifference>>();
        CatalogMapDifferenceCityNameComparator comparator = new CatalogMapDifferenceCityNameComparator(code);

        for(CatalogMapDifference diff : mData){
        	final String country = diff.getCountry(code);
        	countries.add(country);
        	ArrayList<CatalogMapDifference> cities = index.get(country);
        	if(cities == null){
        		cities = new ArrayList<CatalogMapDifference>();
        		index.put(country,cities);
        	}
        	cities.add(diff); 
        }
        mCountries = (String[]) countries.toArray(new String[countries.size()]);
        mRefs = new CatalogMapDifference[mCountries.length][];
        for(int i=0; i<mCountries.length;i++){
        	String country = mCountries[i];
        	ArrayList<CatalogMapDifference> diffSet = index.get(country);
			if(diffSet!=null){        	
	        	int len = diffSet.size();
	        	mRefs[i] = (CatalogMapDifference[]) diffSet.toArray(new CatalogMapDifference[len]);
	        	Arrays.sort(mRefs[i], comparator);
			}
        }
	}
    
}
