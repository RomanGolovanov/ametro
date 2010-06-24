package org.ametro.adapter;

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

	
    public CatalogLocalListAdapter(Context context, Catalog localCatalog, Catalog onlineCatalog, String code) {
    	super(context, code);
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
        bindData(code);
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
