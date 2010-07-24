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
import org.ametro.Constants;
import org.ametro.R;
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
		setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
		if(!Constants.EULA_ACCEPTED_FILE.exists()){
			setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);
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
		message.setPadding(5, 5, 5, 5);

		final ScrollView view= new ScrollView(context);
		view.addView(message);
		setView(view);
	}

	public void onClick(DialogInterface dialog, int which) {
		if(which == BUTTON_POSITIVE){
			FileUtil.touchFile(Constants.EULA_ACCEPTED_FILE);
			dismiss();
		}
		if(which == BUTTON_NEGATIVE){
			FileUtil.delete(Constants.EULA_ACCEPTED_FILE);
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
