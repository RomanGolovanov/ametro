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

import org.ametro.R;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class CatalogTabHostActivity extends TabActivity {

	private static final String TAB_LOCAL = "local";
	private static final String TAB_ONLINE = "online";
	private static final String TAB_IMPORT = "import";
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInstance = this;

		setContentView(R.layout.catalog_tab_host);
		
		final TabHost tabHost = getTabHost();
		final Resources res = getResources();

		tabHost.addTab(tabHost.newTabSpec(TAB_LOCAL)
				.setIndicator(res.getString(R.string.tab_maps_local), res.getDrawable(R.drawable.icon_tab_fdd))
				.setContent(new Intent(this, CatalogLocalListActivity.class)));

		tabHost.addTab(tabHost.newTabSpec(TAB_ONLINE)
				.setIndicator(res.getString(R.string.tab_maps_online), res.getDrawable(R.drawable.icon_tab_browse))
				.setContent(new Intent(this, CatalogOnlineListActivity.class)));

		tabHost.addTab(tabHost.newTabSpec(TAB_IMPORT)
				.setIndicator(res.getString(R.string.tab_maps_import), res.getDrawable(R.drawable.icon_tab_unbox))
				.setContent(new Intent(this, CatalogImportListActivity.class)));
	}
	
	protected void onPause() {
		super.onPause();
	};
	
	private static CatalogTabHostActivity mInstance;
	
	public static Activity getInstance()
	{
		return mInstance;
	}
	
	
}
