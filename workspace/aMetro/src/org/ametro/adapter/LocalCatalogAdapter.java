package org.ametro.adapter;

import static org.ametro.catalog.CatalogMapDifference.CORRUPTED;
import static org.ametro.catalog.CatalogMapDifference.INSTALLED;
import static org.ametro.catalog.CatalogMapDifference.NOT_SUPPORTED;
import static org.ametro.catalog.CatalogMapDifference.OFFLINE;
import static org.ametro.catalog.CatalogMapDifference.UPDATE;

import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapDifference;

import android.content.Context;
import android.graphics.Color;


public class LocalCatalogAdapter extends BaseExpandableCatalogAdapter {

	
    public LocalCatalogAdapter(Context context, Catalog localCatalog, Catalog onlineCatalog, String code) {
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
		mData = Catalog.diffLocal(localCatalog,onlineCatalog);
        bindData(code);
    }

    public int getState(CatalogMapDifference diff)
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
