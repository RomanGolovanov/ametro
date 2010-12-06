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
package org.ametro.ui.dialog;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.storage.tasks.DownloadIconsTask;
import org.ametro.util.FileUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class DownloadIconsDialog extends AlertDialog implements OnClickListener, OnCancelListener {

	private boolean mChangeSettingsEnabled;

	public DownloadIconsDialog(final Context context, final boolean changeSettings) {
		super(context);
		mChangeSettingsEnabled = changeSettings;

		setTitle(R.string.msg_download_icons_dialog_title);
		setCancelable(true);
		setIcon(android.R.drawable.ic_dialog_info);
		
		final TextView message = new TextView(context);
		String str;
		try {
			str = FileUtil.writeToString(context.getResources().openRawResource(R.raw.icons_pack));
		} catch (Exception e) {
			str = e.toString();
		}
		final SpannableString s = new SpannableString(Html.fromHtml(str));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		message.setPadding(5, 5, 5, 5);
		message.setTextColor(context.getResources().getColorStateList(R.color.dialog_text_color));
		message.setLinkTextColor(context.getResources().getColorStateList(R.color.links_color));
		setView(message);
		
		setButton(AlertDialog.BUTTON_POSITIVE, context.getText(android.R.string.yes), this);
		setButton(AlertDialog.BUTTON_NEGATIVE, context.getText(android.R.string.no), this);
		setOnCancelListener(this);

	}

	public void onClick(DialogInterface dialog, int which) {
		if(which == AlertDialog.BUTTON_POSITIVE){
			((ApplicationEx)getContext().getApplicationContext()).getCatalogStorage().requestTask( DownloadIconsTask.getInstance() );
		}
		if(which == AlertDialog.BUTTON_NEGATIVE){
			if(mChangeSettingsEnabled){
				GlobalSettings.setCountryIconsEnabled(getContext(), false);
			}
			dialog.dismiss();
		}

	}

	public void onCancel(DialogInterface dialog) {
		if(mChangeSettingsEnabled){
			GlobalSettings.setCountryIconsEnabled(getContext(), false);
		}
	}

}
