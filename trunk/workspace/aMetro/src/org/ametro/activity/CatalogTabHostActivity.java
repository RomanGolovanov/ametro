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

import org.ametro.ApplicationEx;
import org.ametro.Constants;
import org.ametro.R;
import org.ametro.catalog.storage.tasks.DownloadIconsTask;
import org.ametro.util.FileUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TabHost;
import android.widget.TextView;

public class CatalogTabHostActivity extends TabActivity {

	private static final String TAB_LOCAL = "local";
	private static final String TAB_ONLINE = "online";
	private static final String TAB_IMPORT = "import";
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInstance = this;
		checkIcons();
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
				DownloadIconsDialog.create(this).show();				
				FileUtil.touchFile(Constants.ICONS_CHECK);
			}
		}
	}	
	
	protected void onPause() {
		super.onPause();
	};
	
	private static CatalogTabHostActivity mInstance;
	
	public static Activity getInstance()
	{
		return mInstance;
	}
	
	private static class DownloadIconsDialog {

		 public static AlertDialog create(final Context context) {
		  final TextView message = new TextView(context);
		  final SpannableString s = new SpannableString(
				  Html.fromHtml(
						  context.getText(R.string.msg_download_icons_dialog_content).toString()));
		  Linkify.addLinks(s, Linkify.WEB_URLS);
		  message.setText(s);
		  message.setMovementMethod(LinkMovementMethod.getInstance());
		  message.setPadding(5, 5, 5, 5);
		  return new AlertDialog.Builder(context)
			.setTitle(R.string.msg_download_icons_dialog_title)
			.setCancelable(true)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					((ApplicationEx)context.getApplicationContext()).getCatalogStorage().requestTask( new DownloadIconsTask() );
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setView(message)
			.create();
		 }
	}	
	
}
