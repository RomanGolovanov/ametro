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
package org.ametro.dialog;

import org.ametro.ApplicationEx;
import org.ametro.GlobalSettings;
import org.ametro.R;
import org.ametro.catalog.storage.tasks.DownloadIconsTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class DownloadIconsDialog extends AlertDialog {

	public DownloadIconsDialog(final Context context, final boolean changeSettings) {
		super(context);
		  final TextView message = new TextView(context);
		  final SpannableString s = new SpannableString(Html.fromHtml(context.getText(R.string.msg_download_icons_dialog_content).toString()));
		  Linkify.addLinks(s, Linkify.WEB_URLS);
		  message.setText(s);
		  message.setMovementMethod(LinkMovementMethod.getInstance());
		  message.setPadding(5, 5, 5, 5);
		  
			setTitle(R.string.msg_download_icons_dialog_title);
			setCancelable(true);
			setIcon(android.R.drawable.ic_dialog_info);
			setButton(AlertDialog.BUTTON_POSITIVE, context.getText(android.R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					((ApplicationEx)context.getApplicationContext()).getCatalogStorage().requestTask( DownloadIconsTask.getInstance() );
				}
			});
			setButton(AlertDialog.BUTTON_NEGATIVE, context.getText(android.R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(changeSettings){
						GlobalSettings.setCountryIconsEnabled(context, false);
					}
					dialog.dismiss();
				}
			});
			setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if(changeSettings){
						GlobalSettings.setCountryIconsEnabled(context, false);
					}
				}
			});
			setView(message);
			
	}

}
