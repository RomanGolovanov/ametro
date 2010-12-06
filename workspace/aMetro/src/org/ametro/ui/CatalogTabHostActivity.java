/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.ametro.ui;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.storage.tasks.DownloadIconsTask;
import org.ametro.ui.dialog.ChangeLogDialog;
import org.ametro.ui.dialog.DownloadIconsDialog;
import org.ametro.ui.dialog.EULADialog;

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

	public static final int RESULT_EULA_CANCELED = 100; 
	
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
			if(!GlobalSettings.isAcceptedEULA(this)){
				setResult(RESULT_EULA_CANCELED);
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
		Intent i = getIntent();
		Bundle extras = null;
		if(i!=null){
			extras = i.getExtras();
		}
		final TabHost tabHost = getTabHost();
		final Resources res = getResources();
		Intent intentLocalCatalog = new Intent(this, CatalogLocalListActivity.class);
		if(extras!=null){
			intentLocalCatalog.putExtras(extras);
		}
		tabHost.addTab(tabHost.newTabSpec(TAB_LOCAL)
				.setIndicator(res.getString(R.string.tab_maps_local), res.getDrawable(R.drawable.icon_tab_maps))
				.setContent(intentLocalCatalog));

		Intent intentOnlineCatalog = new Intent(this, CatalogOnlineListActivity.class);
		if(extras!=null){
			intentOnlineCatalog.putExtras(extras);
		}
		tabHost.addTab(tabHost.newTabSpec(TAB_ONLINE)
				.setIndicator(res.getString(R.string.tab_maps_online), res.getDrawable(R.drawable.icon_tab_web))
				.setContent(intentOnlineCatalog));

		Intent intentImportCatalog = new Intent(this, CatalogImportListActivity.class);
		if(extras!=null){
			intentImportCatalog.putExtras(extras);
		}
		tabHost.addTab(tabHost.newTabSpec(TAB_IMPORT)
				.setIndicator(res.getString(R.string.tab_maps_import), res.getDrawable(R.drawable.icon_tab_import))
				.setContent(intentImportCatalog));
		
	}
	
	protected void onResume() {
		if(!GlobalSettings.isAcceptedEULA(this)){
			showDialog(DIALOG_EULA);
		}else if(!GlobalSettings.isChangeLogShowed(this)){
			ChangeLogDialog.show(this);
			ApplicationEx.getInstance().invalidateAutoUpdate();
			GlobalSettings.setChangeLogShowed(this);
		}else if(!DownloadIconsTask.isRunning() && GlobalSettings.isCountryIconsEnabled(this)){
			checkIcons();
		}
		super.onResume();
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

	private static CatalogTabHostActivity mInstance;
	
	public static Activity getInstance()
	{
		return mInstance;
	}
		
}
