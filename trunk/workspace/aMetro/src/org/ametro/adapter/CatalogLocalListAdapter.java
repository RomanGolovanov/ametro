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
 */package org.ametro.adapter;

import static org.ametro.catalog.CatalogMapPair.CORRUPTED;
import static org.ametro.catalog.CatalogMapPair.INSTALLED;
import static org.ametro.catalog.CatalogMapPair.NOT_SUPPORTED;
import static org.ametro.catalog.CatalogMapPair.OFFLINE;
import static org.ametro.catalog.CatalogMapPair.UPDATE;

import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;

import android.content.Context;
import android.graphics.Color;


public class CatalogLocalListAdapter extends BaseCatalogExpandableAdapter {

	
    public CatalogLocalListAdapter(Context context, Catalog localCatalog, Catalog onlineCatalog) {
    	super(context);
		mStates = context.getResources().getStringArray(R.array.catalog_map_states);
		mStateColors = new int[]{
			Color.LTGRAY,
			Color.RED,
			Color.RED,
			Color.YELLOW,
			Color.WHITE,
			Color.MAGENTA, // SKIP COLOR
			Color.MAGENTA, // SKIP COLOR
			Color.MAGENTA  // SKIP COLOR
		};
		mData = Catalog.diff(localCatalog,onlineCatalog, Catalog.DIFF_MODE_LEFT);
        bindData(mLanguageCode);
    }

    public int getState(CatalogMapPair diff)
    {
    	CatalogMap local = diff.getLocal();
    	CatalogMap remote = diff.getRemote();
    	if(remote==null){
    		// remote not exist
    		if(local.isCorruted()){
    			return CORRUPTED;
    		}else if(!local.isSupported()){
    			return NOT_SUPPORTED;
    		}else{
    			return OFFLINE;
    		}
    	}else if(!remote.isSupported()){
    		// remote not supported
    		if(local.isCorruted()){
    			return CORRUPTED;
    		}else if(!local.isSupported()){
    			return NOT_SUPPORTED;
    		}else{
    			return INSTALLED;
    		}
    	}else{
    		// remote OK
    		if(local.isCorruted()){
    			return UPDATE;
    		}else if(!local.isSupported()){
    			return UPDATE;
    		}else{
    			if(local.getTimestamp() >= remote.getTimestamp()){
    				return INSTALLED;
    			}else{
    				return UPDATE;
    			}
    		}
    	}
    }

}
