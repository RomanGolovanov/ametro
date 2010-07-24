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

import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.R;
import org.ametro.catalog.storage.tasks.DownloadIconsTask;
import org.ametro.dialog.DownloadIconsDialog;
import org.ametro.dialog.EULADialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class CatalogTabHostActivity extends TabActivity implements OnDismissListener{

	private static final int DIALOG_EULA = 1;
	
	private static final String TAB_LOCAL = "local";
	private static final String TAB_ONLINE = "online";
	private static final String TAB_IMPORT = "import";
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_EULA:
			EULADialog dlg = new EULADialog(this);
			dlg.setOnDismissListener(this);
			return dlg;
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	public void onDismiss(DialogInterface dialog) {
		if(dialog instanceof EULADialog){
			if(!Constants.EULA_ACCEPTED_FILE.exists()){
				finish();
			}else{
				if(!DownloadIconsTask.isRunning() && GlobalSettings.isCountryIconsEnabled(this)){
					checkIcons();
				}
			}
		}
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInstance = this;
		final TabHost tabHost = getTabHost();
		final Resources res = getResources();
		tabHost.addTab(tabHost.newTabSpec(TAB_LOCAL)
				.setIndicator(res.getString(R.string.tab_maps_local), res.getDrawable(R.drawable.icon_tab_maps))
				.setContent(new Intent(this, CatalogLocalListActivity.class)));

		tabHost.addTab(tabHost.newTabSpec(TAB_ONLINE)
				.setIndicator(res.getString(R.string.tab_maps_online), res.getDrawable(R.drawable.icon_tab_web))
				.setContent(new Intent(this, CatalogOnlineListActivity.class)));

		tabHost.addTab(tabHost.newTabSpec(TAB_IMPORT)
				.setIndicator(res.getString(R.string.tab_maps_import), res.getDrawable(R.drawable.icon_tab_import))
				.setContent(new Intent(this, CatalogImportListActivity.class)));
	}


	private void checkIcons(){
		if(Constants.ICONS_PATH.exists() && Constants.ICONS_PATH.isDirectory())
		{
			String[] files = Constants.ICONS_PATH.list();
			if(files == null || files.length == 0){
				DownloadIconsDialog dialog = new DownloadIconsDialog(this,true);
				dialog.show();
			}
		}
	}	
	
	protected void onResume() {
		if(!Constants.EULA_ACCEPTED_FILE.exists()){
			showDialog(DIALOG_EULA);
		}else if(!DownloadIconsTask.isRunning() && GlobalSettings.isCountryIconsEnabled(this)){
			checkIcons();
		}
		super.onResume();
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
