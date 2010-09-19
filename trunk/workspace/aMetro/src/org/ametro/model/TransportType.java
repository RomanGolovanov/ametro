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
 */
package org.ametro.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.util.CollectionUtil;
import org.ametro.util.StringUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class TransportType {

	public final static int UNKNOWN_RESOURCE_INDEX = 0;
	public final static int METRO_RESOURCE_INDEX = 1;
	public final static int TRAM_RESOURCE_INDEX = 2;
	public final static int BUS_RESOURCE_INDEX = 3;
	public final static int TRAIN_RESOURCE_INDEX = 4;
	public final static int WATER_BUS_RESOURCE_INDEX = 5;
	public final static int TROLLEYBUS_RESOURCE_INDEX = 6;
	public final static int CABLEWAY_RESOURCE_INDEX = 7;
	
	public final static int UNKNOWN_ID = 1;
	public final static int METRO_ID = 2;
	public final static int TRAM_ID = 4;
	public final static int BUS_ID = 8;
	public final static int TRAIN_ID = 16;
	public final static int WATER_BUS_ID = 32;
	public final static int TROLLEYBUS_ID = 64;
	public final static int CABLEWAY_ID = 128;

	public final static int TRANSPORT_COUNT = 7;
	
	private final static String TRANSPORT_TYPE_METRO = "Метро";
	private final static String TRANSPORT_TYPE_TRAIN = "Электричка";
	private final static String TRANSPORT_TYPE_TRAM = "Трамвай";
	private final static String TRANSPORT_TYPE_BUS = "Автобус";
	private final static String TRANSPORT_TYPE_WATER_BUS = "Речной трамвай";
	private final static String TRANSPORT_TYPE_TROLLEYBUS = "Троллейбус";
	private final static String TRANSPORT_TYPE_CABLEWAY = "Фуникулер";
	
	private final static HashMap<String, Integer> mIndex;
	private final static HashMap<String, Integer> mResourceIndex;
	private final static HashMap<Integer, Integer> mResourceByIdIndex;
	
	static {
		mIndex = new HashMap<String, Integer>();
		mIndex.put(TRANSPORT_TYPE_METRO, METRO_ID);
		mIndex.put(TRANSPORT_TYPE_TRAM, TRAM_ID);
		mIndex.put(TRANSPORT_TYPE_BUS, BUS_ID);
		mIndex.put(TRANSPORT_TYPE_TRAIN, TRAIN_ID);
		mIndex.put(TRANSPORT_TYPE_WATER_BUS, WATER_BUS_ID);
		mIndex.put(TRANSPORT_TYPE_TROLLEYBUS, TROLLEYBUS_ID);
		mIndex.put(TRANSPORT_TYPE_CABLEWAY, CABLEWAY_ID);
		
		mResourceIndex = new HashMap<String, Integer>();
		mResourceIndex.put(TRANSPORT_TYPE_METRO, METRO_RESOURCE_INDEX);
		mResourceIndex.put(TRANSPORT_TYPE_TRAM, TRAM_RESOURCE_INDEX);
		mResourceIndex.put(TRANSPORT_TYPE_BUS, BUS_RESOURCE_INDEX);
		mResourceIndex.put(TRANSPORT_TYPE_TRAIN, TRAIN_RESOURCE_INDEX);
		mResourceIndex.put(TRANSPORT_TYPE_WATER_BUS, WATER_BUS_RESOURCE_INDEX);
		mResourceIndex.put(TRANSPORT_TYPE_TROLLEYBUS, TROLLEYBUS_RESOURCE_INDEX);
		mResourceIndex.put(TRANSPORT_TYPE_CABLEWAY, CABLEWAY_RESOURCE_INDEX);
		
		mResourceByIdIndex = new HashMap<Integer, Integer>();
		mResourceByIdIndex.put(UNKNOWN_ID, UNKNOWN_RESOURCE_INDEX);
		mResourceByIdIndex.put(METRO_ID, METRO_RESOURCE_INDEX);
		mResourceByIdIndex.put(TRAM_ID, TRAM_RESOURCE_INDEX);
		mResourceByIdIndex.put(BUS_ID, BUS_RESOURCE_INDEX);
		mResourceByIdIndex.put(TRAIN_ID, TRAIN_RESOURCE_INDEX);
		mResourceByIdIndex.put(WATER_BUS_ID, WATER_BUS_RESOURCE_INDEX);
		mResourceByIdIndex.put(TROLLEYBUS_ID, TROLLEYBUS_RESOURCE_INDEX);
		mResourceByIdIndex.put(CABLEWAY_ID, CABLEWAY_RESOURCE_INDEX);
	}
	
	public static int getTransportTypeId(String value){
		if(value.indexOf('+')==-1){
			Integer id = mIndex.get(value);
			if(id != null){
				return id;
			}
		}else{
			int types = 0;
			String[] parts = StringUtil.fastSplit(value,'+');
			for(String part : parts){
				Integer id = mIndex.get(part);
				if(id != null){
					types |= id;
				}else{
					types |= UNKNOWN_ID;
				}
			}
			if(types>0){
				return types;
			}
		}
		return UNKNOWN_ID;	
	}

	public static int getTransportTypeResource(String value){
		Integer resId = mResourceIndex.get(value);
		return resId!=null ? resId : UNKNOWN_RESOURCE_INDEX;
	}

	public static int getTransportTypeResource(int transportType) {
		Integer resId = mResourceByIdIndex.get(transportType);
		return resId!=null ? resId : UNKNOWN_RESOURCE_INDEX;
	}	
	
	public static int[] unpackTransports(long transports){
		int transportBit = 1;
		int transportId = 0;
		ArrayList<Integer> list = new ArrayList<Integer>();  
		while(transports>0){
			if((transports % 2)>0){
				list.add(transportId);
			}
			transports = transports >> 1;
			transportBit = transportBit << 1;
			transportId++;
		}
		return CollectionUtil.toArray(list);
		
	}

	public static long packTransports(int[] transports){
		long val = 0;
		for(int transportId : transports){
			val |= transportId;
		}
		return val;
	}
	
	public static HashMap<Integer, Drawable> getIconsMap(Context context){
		HashMap<Integer, Drawable> iconsMap = new HashMap<Integer, Drawable>();
		final Resources res = context.getResources();
		iconsMap.put( TransportType.UNKNOWN_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.UNKNOWN_ID))  );
		iconsMap.put( TransportType.METRO_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.METRO_ID))  );
		iconsMap.put( TransportType.TRAM_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.TRAM_ID))  );
		iconsMap.put( TransportType.BUS_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.BUS_ID))  );
		iconsMap.put( TransportType.TRAIN_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.TRAIN_ID))  );
		iconsMap.put( TransportType.WATER_BUS_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.WATER_BUS_ID))  );
		iconsMap.put( TransportType.TROLLEYBUS_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.TROLLEYBUS_ID))  );
		iconsMap.put( TransportType.CABLEWAY_ID , res.getDrawable(getTransportTypeWhiteIconId(TransportType.CABLEWAY_ID))  );
		return iconsMap;
	}
	
	
	public static int getTransportTypeBlackIconId(int transportTypeId){
		switch(transportTypeId){
			case TransportType.METRO_ID : return R.drawable.icon_b_metro;
			case TransportType.TRAM_ID : return R.drawable.icon_b_tram;
			case TransportType.BUS_ID : return R.drawable.icon_b_bus;
			case TransportType.TRAIN_ID : return R.drawable.icon_b_train;
			case TransportType.WATER_BUS_ID : return R.drawable.icon_b_water_bus;
			case TransportType.TROLLEYBUS_ID : return R.drawable.icon_b_trolleybus;		
			case TransportType.CABLEWAY_ID : return R.drawable.icon_b_cableway;		
		}
		return R.drawable.icon_b_unknown;
	}	
	
	public static int getTransportTypeWhiteIconId(int transportTypeId){
		switch(transportTypeId){
			case TransportType.METRO_ID : return R.drawable.icon_w_metro;
			case TransportType.TRAM_ID : return R.drawable.icon_w_tram;
			case TransportType.BUS_ID : return R.drawable.icon_w_bus;
			case TransportType.TRAIN_ID : return R.drawable.icon_w_train;
			case TransportType.WATER_BUS_ID : return R.drawable.icon_w_water_bus;
			case TransportType.TROLLEYBUS_ID : return R.drawable.icon_w_trolleybus;		
			case TransportType.CABLEWAY_ID : return R.drawable.icon_w_cableway;		
		}
		return R.drawable.icon_w_unknown;
	}
	
}
