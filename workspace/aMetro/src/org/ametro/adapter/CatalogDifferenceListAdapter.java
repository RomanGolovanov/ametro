package org.ametro.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ametro.R;
import org.ametro.catalog.CatalogMapDifference;
import org.ametro.catalog.CatalogMapDifference.CatalogMapDifferenceCityNameComparator;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class CatalogDifferenceListAdapter  extends BaseExpandableListAdapter {

    private Context mContext;
	
    private String[] mCountries;
    private String[][] mCities;
    private CatalogMapDifference[][] mRefs;

    private String[] mStates;
    
    private int mSelectedGroup;
    private int mSelectedChild;

    public CatalogMapDifference getData(int groupId, int childId) {
        return mRefs[groupId][childId];
    }

    public CatalogDifferenceListAdapter(Context context, List<CatalogMapDifference> data, String code) {
        mContext = context;
        
        mStates = context.getResources().getStringArray(R.array.catalog_map_states);
        
        TreeSet<String> countries = new TreeSet<String>();
        TreeMap<String, ArrayList<CatalogMapDifference>> index = new TreeMap<String, ArrayList<CatalogMapDifference>>();
        CatalogMapDifferenceCityNameComparator comparator = new CatalogMapDifferenceCityNameComparator(code);

        for(CatalogMapDifference diff : data){
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
        mCities = new String[mCountries.length][];
        mRefs = new CatalogMapDifference[mCountries.length][];
        for(int i=0; i<mCountries.length;i++){
        	String country = mCountries[i];
        	ArrayList<CatalogMapDifference> diffSet = index.get(country);
			if(diffSet!=null){        	
	        	int len = diffSet.size();
	        	mRefs[i] = (CatalogMapDifference[]) diffSet.toArray(new CatalogMapDifference[len]);
	        	Arrays.sort(mRefs[i], comparator);
	        	mCities[i] = new String[len];
	        	for(int j=0;j<len;j++){
	        		mCities[i][j] = mRefs[i][j].getCity(code);
	        	}
			}
        }
    }

    public Object getChild(int groupPosition, int childPosition) {
    	CatalogMapDifference ref = mRefs[groupPosition][childPosition];
        return mCities[groupPosition][childPosition] + " (" + mStates[ref.getState()] + "," + ref.getUrl() + ", transports:" + ref.getTransports() + ")";
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return mCities[groupPosition].length;
    }

    public TextView getGenericView() {
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(lp);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setPadding(36, 0, 0, 0);
        return textView;
    }

    public TextView getSelectedView() {
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(lp);
        textView.setBackgroundColor(Color.DKGRAY);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setPadding(36, 0, 0, 0);
        return textView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        boolean isSelected = groupPosition == mSelectedGroup && childPosition == mSelectedChild;
        TextView textView = isSelected ? getSelectedView() : getGenericView();
        textView.setText(getChild(groupPosition, childPosition).toString());
        return textView;
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

//
//    public int getSelectedGroupPosition() {
//        return mSelectedGroup;
//    }
//
//    public int getSelectChildPosition() {
//        return mSelectedChild;
//    }
//
//    public void setSelectedFile(String fileName) {
//        for (int i = 0; i < mFiles.length; i++) {
//            String[] cols = mFiles[i];
//            for (int j = 0; j < cols.length; j++) {
//                if (cols[j].equalsIgnoreCase(fileName)) {
//                    mSelectedGroup = i;
//                    mSelectedChild = j;
//                    return;
//                }
//            }
//        }
//        mSelectedGroup = -1;
//        mSelectedChild = -1;
//    }

    
}
