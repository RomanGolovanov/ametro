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
	
	public TileManagerDescription(Model model){
		mMapName = model.getMapName();
		mCityName = model.getCityName();
		countryName = model.getCountryName();
		width = model.getWidth();
		height = model.getHeight();
		minimumLevel = 0;
		maximumLevel = 0;
	}
	
}

