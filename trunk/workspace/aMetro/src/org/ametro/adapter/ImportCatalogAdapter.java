package org.ametro.adapter;

import static org.ametro.catalog.CatalogMapDifference.CORRUPTED;
import static org.ametro.catalog.CatalogMapDifference.IMPORT;
import static org.ametro.catalog.CatalogMapDifference.INSTALLED;
import static org.ametro.catalog.CatalogMapDifference.UPDATE;

import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapDifference;

import android.content.Context;
import android.graphics.Color;

public class ImportCatalogAdapter extends BaseExpandableCatalogAdapter {
	
    public ImportCatalogAdapter(Context context, Catalog importCatalog, Catalog localCatalog, String code) {
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
		mData = Catalog.diffRemote(importCatalog, localCatalog);
        bindData(code);
    }

    public int getState(CatalogMapDifference diff)
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
