package org.ametro.model;

public class MapLayer {

	public final static int TYPE_TEXT = 1;
	public final static int TYPE_URL = 2;
	
	
	public int id;
	
	public int type;
	
	public boolean isStationInfo;
	
	public String cityName;
	public String menuName;
	public String menuImage;
	public String imageFileName;
	public String caption;
	public String prefix;
	
	public int[] stations;
	public String[][] texts;
	
	public String systemFileName;
	
	
}
