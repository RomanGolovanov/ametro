package org.ametro.adapter;

import static org.ametro.catalog.CatalogMapPair.CORRUPTED;
import static org.ametro.catalog.CatalogMapPair.IMPORT;
import static org.ametro.catalog.CatalogMapPair.INSTALLED;
import static org.ametro.catalog.CatalogMapPair.UPDATE;

import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;

import android.content.Context;
import android.graphics.Color;

public class CatalogOnlineListAdapter extends BaseCatalogExpandableAdapter {
	
    public CatalogOnlineListAdapter(Context context, Catalog onlineCatalog, Catalog localCatalog, String code) {
    	super(context, code);
		mStates = context.getResources().getStringArray(R.array.catalog_map_states);
		mStateColors = new int[]{
				Color.MAGENTA, // SKIP COLOR
				Color.MAGENTA, // SKIP COLOR
				Color.RED,
				Color.YELLOW,
				Color.WHITE,
				Color.YELLOW,
				Color.MAGENTA, // SKIP COLOR
				Color.MAGENTA  // SKIP COLOR
			};
		mData = Catalog.diff(localCatalog, onlineCatalog, Catalog.DIFF_MODE_RIGHT);
        bindData(code);
    }

    public int getState(CatalogMapPair diff)
    {
    	CatalogMap local = diff.getLocal();
    	CatalogMap remote = diff.getRemote();
    	if(remote.isCorruted()){
    		return CORRUPTED;
    	}else{
    		if(local == null){
    			return IMPORT;
    		}else if(!local.isSupported() || local.isCorruted()){
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
