package com.ametro.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.ametro.libs.FileGroupsDictionary;

public class MapListAdapter extends BaseExpandableListAdapter {

	private String[] mCountries;
	private String[][] mCities; 
	private String[][] mFiles;

	private Context mContext;
	private int mSelectedGroup;
	private int mSelectedChild;

	public String getFileName(int groupId, int childId){
		return mFiles[groupId][childId];
	}

	public MapListAdapter(Context context, FileGroupsDictionary data){
		mContext = context;
		mCountries = data.getGroups();
		mCities = new String[mCountries.length][];
		mFiles = new String[mCountries.length][];
		for (int i = 0; i < mCountries.length; i++) {
			String country = mCountries[i];
			mCities[i] = data.getLabels(country);
			mFiles[i] = data.getPathes(country, mCities[i]);
		}
	}

	public Object getChild(int groupPosition, int childPosition) {
		return mCities[groupPosition][childPosition];
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
		boolean isSelected = groupPosition==mSelectedGroup && childPosition==mSelectedChild;
		TextView textView = isSelected ? getSelectedView() : getGenericView() ;
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

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
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


	public int getSelectedGroupPosition() {
		return mSelectedGroup;
	}

	public int getSelectChildPosition() {
		return mSelectedChild;
	}

	public void setSelectedFile(String fileName){
		for(int i = 0; i < mFiles.length; i++){
			String[] cols = mFiles[i];
			for(int j = 0; j < cols.length; j++){
				if( cols[j].equals(fileName) ){
					mSelectedGroup = i;
					mSelectedChild = j;
					return;
				}
			}
		}
		mSelectedGroup = -1;
		mSelectedChild = -1;
	}

}
