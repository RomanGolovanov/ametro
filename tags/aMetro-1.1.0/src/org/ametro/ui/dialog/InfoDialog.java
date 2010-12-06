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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.ScrollView;
import android.widget.TextView;

public class InfoDialog  {


	public static void showInfoDialog(final Context context, String title, String text, int icon) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setCancelable(true);
		builder.setIcon(icon);
		builder.setNegativeButton(R.string.btn_close, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		final TextView message = new TextView(context);
		final SpannableString s = new SpannableString(Html.fromHtml(text));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		message.setPadding(5, 5, 5, 5);
		message.setTextColor(context.getResources().getColorStateList(R.color.dialog_text_color));
		message.setLinkTextColor(context.getResources().getColorStateList(R.color.links_color));

		final ScrollView view= new ScrollView(context);
		view.addView(message);
		builder.setView(view);
		
		
		builder.create().show();
	}

	
}
