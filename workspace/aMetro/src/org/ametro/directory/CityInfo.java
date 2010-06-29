package org.ametro.directory;

import org.ametro.model.ext.ModelLocation;

public class CityInfo {
	public String mCountryNameRu;
	public String mCityNameRu;
	public String mCountryNameEn;
	public String mCityNameEn;
	public ModelLocation mLocation;
	
	
	
	public String getCountryNameRu() {
		return mCountryNameRu;
	}



	public String getCityNameRu() {
		return mCityNameRu;
	}



	public String getCountryNameEn() {
		return mCountryNameEn;
	}



	public String getCityNameEn() {
		return mCityNameEn;
	}



	public ModelLocation getLocation() {
		return mLocation;
	}



	public CityInfo(String countryNameRu, String cityNameRu,
			String countryNameEn, String cityNameEn, ModelLocation location) {
		super();
		mCountryNameRu = countryNameRu;
		mCityNameRu = cityNameRu;
		mCountryNameEn = countryNameEn;
		mCityNameEn = cityNameEn;
		mLocation = location;
	}
	
	
}
