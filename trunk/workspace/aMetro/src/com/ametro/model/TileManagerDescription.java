package com.ametro.model;

import java.io.Serializable;

public class TileManagerDescription  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2583296356317936507L;
	
	public String mMapName;
	public String mCityName;
	public String countryName;
	public int width;
	public int height;
	public int minimumLevel;
	public int maximumLevel;
	
	public TileManagerDescription(TransportMap map){
		mMapName = map.getMapName();
		mCityName = map.getCityName();
		countryName = map.getCountryName();
		width = map.getWidth();
		height = map.getHeight();
		minimumLevel = 0;
		maximumLevel = 0;
	}
	
}

