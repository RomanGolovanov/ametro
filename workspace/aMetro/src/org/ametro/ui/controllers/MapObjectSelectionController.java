/*
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
 */
package org.ametro.ui.controllers;

import java.util.ArrayList;

import org.ametro.model.StationView;

public class MapObjectSelectionController {

	public static interface IMapObjectSelectionListener
	{
		void onMapObjectSelectionChanged(int oldMode, int newMode);
	}

	public static final int SELECTION_MODE_BEGIN = 0;
	public static final int SELECTION_MODE_FIRST = 1;
	public static final int SELECTION_MODE_SECOND = 2;
	public static final int SELECTION_MODE_DONE = 3;

	private void setMode(int newMode){
		int oldMode = mSelectionMode;
		mSelectionMode = newMode;
		fireSelectionModeChanged(oldMode, newMode);
	}
	
	public void clearSelection(){
		mSelectionMode = SELECTION_MODE_BEGIN;
		mStartStationView = null;
		mEndStationView = null;
	}
	
	public void setSelection(StationView stationFrom, StationView stationTo) {
		mSelectionMode = SELECTION_MODE_DONE;
		mStartStationView = stationFrom;
		mEndStationView = stationTo;
	}
	
	public void acceptSelection(){
		switch(mSelectionMode){
		case SELECTION_MODE_BEGIN:
			setMode(SELECTION_MODE_FIRST);
			break;
		case SELECTION_MODE_FIRST:
			setMode(SELECTION_MODE_SECOND);
			break;
		case SELECTION_MODE_SECOND:
			setMode(SELECTION_MODE_DONE);
			break;
		case SELECTION_MODE_DONE:
			setMode(SELECTION_MODE_BEGIN);
			mStartStationView = null;
			mEndStationView = null;
			break;
		}		
	}
	
	public void rollbackSelection(){
		switch(mSelectionMode){
		case SELECTION_MODE_FIRST:
			setMode(SELECTION_MODE_BEGIN);
			break;
		case SELECTION_MODE_SECOND:
			setMode(SELECTION_MODE_FIRST);
			break;
		case SELECTION_MODE_BEGIN:
		case SELECTION_MODE_DONE:
			setMode(SELECTION_MODE_BEGIN);
		}
	}
	
	public StationView getStartStationView(){
		return mStartStationView;
	}
	
	public StationView getEndStationView(){
		return mEndStationView;
	}

	public void onClickEvent(StationView station){
		switch(mSelectionMode){
			case SELECTION_MODE_BEGIN:
				if(station!=null){
					mStartStationView = station;
					acceptSelection();
				}
				break;
			case SELECTION_MODE_FIRST:
				if(station!=null){
					mEndStationView = station;
					acceptSelection();
				}
				break;
			case SELECTION_MODE_DONE:
				mStartStationView = null;
				mEndStationView = null;
				setMode(SELECTION_MODE_BEGIN);
				break;
		}		
	}	

	public void addMapObjectSelectionListener(IMapObjectSelectionListener listener){
		mListeners.add(listener);
	}
	
	public void removeMapObjectSelectionListener(IMapObjectSelectionListener listener){
		mListeners.remove(mListeners.indexOf(listener));
	}

	private void fireSelectionModeChanged(int oldMode, int newMode) {
		for(IMapObjectSelectionListener listener : mListeners){
			listener.onMapObjectSelectionChanged(oldMode, newMode);
		}
	}

	private int mSelectionMode = SELECTION_MODE_BEGIN;
	private StationView mStartStationView;
	private StationView mEndStationView;
	private ArrayList<IMapObjectSelectionListener> mListeners = new ArrayList<IMapObjectSelectionListener>();

}
