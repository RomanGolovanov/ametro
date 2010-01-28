package com.ametro.model;

import java.io.Serializable;

public class ModelDescription implements Serializable {

	private static final long serialVersionUID = 6520993350037904315L;

	private String mMapName;
	private String mCountryName;
	private String mCityName;

	private int mWidth;
	private int mHeight;

	public ModelDescription(){
		super();
	}

	public ModelDescription(String mapName, String countryName, String cityName, int width, int height) {
		super();
		this.mMapName = mapName;
		this.mCountryName = countryName;
		this.mCityName = cityName;
		this.mWidth = width;
		this.mHeight = height;
	}

	public ModelDescription(Model model) {
		super();
		mMapName = model.getMapName();
		mCountryName = model.getCountryName(); 
		mCityName = model.getCityName(); 
		mWidth = model.getWidth(); 
		mHeight = model.getHeight();
	}

	public String getMapName() {
		return mMapName;
	}
	public void setMapName(String mapName) {
		this.mMapName = mapName;
	}
	public String getCountryName() {
		return mCountryName;
	}
	public void setCountryName(String countryName) {
		this.mCountryName = countryName;
	}
	public String getCityName() {
		return mCityName;
	}
	public void setCityName(String cityName) {
		this.mCityName = cityName;
	}
	public int getWidth() {
		return mWidth;
	}
	public void setWidth(int width) {
		this.mWidth = width;
	}
	public int getHeight() {
		return mHeight;
	}
	public void setHeight(int height) {
		this.mHeight = height;
	}

	public boolean equals(ModelDescription model) {
		return getCountryName().equals(model.getCountryName()) 
			&& getCityName().equals(model.getCityName());
	}


}
