package org.ametro.adapter;

import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapDifference;

import android.content.Context;
import android.graphics.Color;

public class ImportCatalogAdapter  extends BaseExpandableCatalogAdapter {

	private static final int TYPE_CORRUPTED = 0; 
	private static final int TYPE_IMPORT = 1; 
	private static final int TYPE_UPDATE = 2; 
	private static final int TYPE_INSTALLED = 3; 
	
    public ImportCatalogAdapter(Context context, Catalog importCatalog, Catalog localCatalog, String code) {
    	super(context, code);
		mStates = context.getResources().getStringArray(R.array.import_map_states);
		mStateColors = new int[]{
			Color.RED,
			Color.WHITE,
			Color.YELLOW,
			Color.GREEN
		};
		mData = Catalog.diffImport(importCatalog, localCatalog);
        bindData(code);
    }

    public int getState(CatalogMapDifference diff)
    {
    	CatalogMap local = diff.getLocal();
    	CatalogMap remote = diff.getRemote();
    	if(remote.isCorruted()){
    		return TYPE_CORRUPTED;
    	}else{
    		if(local == null){
    			return TYPE_IMPORT;
    		}else if(!local.isSupported() || local.isCorruted()){
    			return TYPE_UPDATE;
    		}else{
    			if(local.getTimestamp() >= remote.getTimestamp()){
    				return TYPE_INSTALLED;
    			}else{
    				return TYPE_UPDATE;
    			}
    		}
    	}
    }

}
