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

import org.ametro.Constants;
import org.ametro.R;
import org.ametro.activity.DonateActivity;
import org.ametro.util.FileUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

public class AboutDialog extends AlertDialog implements OnClickListener {

	private String mAppName;
	private String mVersionName;
	private String[] mChangelog;
		
	public static void show(Context context){
		try {
			new AboutDialog(context).show();
		} catch (Exception e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
				Log.w(Constants.LOG_TAG_MAIN,"Failed to show about dialog", e);
			}
		}
	}
	
	protected AboutDialog(Context context) throws Exception {
		super(context);
		bindData(context);
		setTitle(mAppName + " v." + mVersionName );
		setCancelable(true);
		setIcon(android.R.drawable.ic_dialog_info);
		setButton(BUTTON_POSITIVE, context.getString(R.string.btn_ok), this);
		// TODO: enable after creating changelog activity 
		//setButton(BUTTON_NEGATIVE, context.getString(R.string.btn_changelog), this);
		setButton(BUTTON_NEUTRAL, context.getString(R.string.btn_donate), this);
		
		final TextView message = new TextView(context);
		final SpannableString s = new SpannableString(getContent());
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		message.setPadding(5, 5, 5, 5);

		final ScrollView view= new ScrollView(context);
		view.addView(message);
		setView(view);
	}

	protected Spannable getContent() {
		try {
			final Context context = getContext();
			String template = FileUtil.writeToString(context.getResources().openRawResource(R.raw.about));
			StringBuilder htmlText = new StringBuilder(template);
			htmlText.append("<p>");
			int count = 10;
			for (int i = mChangelog.length - 1; i >= 0; i--) {
				String chages = mChangelog[i];
				htmlText.append(chages);
				htmlText.append("<br/>");
				count--;
				if (count <= 0) {
					break;
				}
			}
			htmlText.append("</p>");
			Spanned text = Html.fromHtml(htmlText.toString());
			SpannableStringBuilder spannable = new SpannableStringBuilder(text);
			Linkify.addLinks(spannable, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
			return spannable;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void bindData(final Context context) throws NameNotFoundException {
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		info = manager.getPackageInfo(context.getPackageName(), 0);
		mVersionName = info.versionName;
		mAppName = getContext().getString( info.applicationInfo.labelRes );
		mChangelog = context.getResources().getStringArray(R.array.version_changelog);
	}

	public void onClick(DialogInterface dialog, int which) {
		if(which == BUTTON_POSITIVE){
			dismiss();
		}
		if(which == BUTTON_NEGATIVE){
			// TODO: start change log activity
		}
		if(which == BUTTON_NEUTRAL){
			Context ctx = getContext();
			ctx.startActivity(new Intent(ctx, DonateActivity.class));
		}
	}
}
