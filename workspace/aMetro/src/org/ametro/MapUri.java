package org.ametro;

import android.net.Uri;

public class MapUri  {

	public static Uri create( String mapName ) {
		return Uri.parse("ametro://" + mapName);
	}

	public static String getMapName( Uri uri ) {
		if("ametro".equals(uri.getScheme())){
			return uri.toString().replace("ametro://", "");
		}
		return null;
	}

	
	
}
