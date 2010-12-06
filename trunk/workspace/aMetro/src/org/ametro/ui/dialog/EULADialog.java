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
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.util.FileUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.ScrollView;
import android.widget.TextView;

public class EULADialog extends AlertDialog implements OnClickListener {
		
	public static void show(Context context){
		new EULADialog(context).show();
	}
	
	public EULADialog(Context context) {
		super(context);
		setTitle(R.string.title_eula);
		setCancelable(true);
		setIcon(android.R.drawable.ic_dialog_alert);
		setButton(BUTTON_POSITIVE, context.getString( !GlobalSettings.isAcceptedEULA(context) ? R.string.btn_accept : R.string.btn_close ) , this);
		if(!GlobalSettings.isAcceptedEULA(context)){
			setButton(BUTTON_NEGATIVE, context.getString(R.string.btn_reject), this);
		}
		setButton(BUTTON_NEUTRAL, context.getString(R.string.btn_gpl), this);
		String str;
		try {
			str = FileUtil.writeToString(context.getResources().openRawResource(R.raw.eula));
		} catch (Exception e) {
			str = e.toString();
		}
		final TextView message = new TextView(context);
		final SpannableString s = new SpannableString(Html.fromHtml(str));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		message.setTextColor(context.getResources().getColorStateList(R.color.dialog_text_color));
		message.setLinkTextColor(context.getResources().getColorStateList(R.color.links_color));
		message.setPadding(5, 5, 5, 5);

		final ScrollView view= new ScrollView(context);
		view.addView(message);
		setView(view);
	}

	public void onClick(DialogInterface dialog, int which) {
		if(which == BUTTON_POSITIVE){
			GlobalSettings.setAcceptedEULA(getContext(), true);
			dismiss();
		}
		if(which == BUTTON_NEGATIVE){
			dismiss();
		}
		if(which == BUTTON_NEUTRAL){
			if(!Constants.EULA_FILE.exists()){
				ApplicationEx.extractEULA(getContext());
			}
			Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(Constants.EULA_FILE), "text/plain");
			getContext().startActivity(intent);
		}
	}
}
