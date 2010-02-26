/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro;

import android.net.Uri;

public class MapUri {

    public static Uri create(String mapName) {
        return Uri.parse("ametro://" + mapName);
    }

    public static Uri createSearch(String stationName) {
        return Uri.parse("ametro://search/" + stationName);
    }
    
    public static String getMapName(Uri uri) {
        if ("ametro".equals(uri.getScheme())) {
            return uri.toString().replace("ametro://", "");
        }
        return null;
    }

    public static String getSearch(Uri uri) {
        if ("ametro".equals(uri.getScheme())) {
            return uri.toString().replace("ametro://search/", "");
        }
        return null;
    }
    

}
