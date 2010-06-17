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

package org.ametro.activity;

import org.ametro.MapSettings;
import org.ametro.R;
import org.ametro.catalog.storage.CatalogStorage;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class AllMaps extends TabActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapSettings.checkPrerequisite(this);
		mStorage = new CatalogStorage(
				MapSettings.getLocalCatalogStorageUrl(), MapSettings.getLocalCatalog(),
				MapSettings.getImportCatalogStorageUrl(), MapSettings.getImportCatalog(),
				MapSettings.getOnlineCatalogStorageUrl(), MapSettings.getOnlineCatalogUrl());
		Instance = this;

		final TabHost tabHost = getTabHost();
		final Resources res = getResources();

		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(res.getString(R.string.tab_maps_favorited), res.getDrawable(R.drawable.icon_tab_star))
				.setContent(new Intent(this, LocalMaps.class).putExtra(LocalMaps.EXTRA_FAVORITES_ONLY, true)));

		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(res.getString(R.string.tab_maps_local), res.getDrawable(R.drawable.icon_tab_fdd))
				.setContent(new Intent(this, LocalMaps.class)));

		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(res.getString(R.string.tab_maps_online), res.getDrawable(R.drawable.icon_tab_browse))
				.setContent(new Intent(this, OnlineMaps.class)));

		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator(res.getString(R.string.tab_maps_import), res.getDrawable(R.drawable.icon_tab_unbox))
				.setContent(new Intent(this, ImportMaps.class)));
		
	}
	
	protected void onPause() {
		super.onPause();
	};
	
	/*package*/ static AllMaps Instance;
	private CatalogStorage mStorage;
		
	public CatalogStorage getStorage(){
		return mStorage;
	}
	
}
