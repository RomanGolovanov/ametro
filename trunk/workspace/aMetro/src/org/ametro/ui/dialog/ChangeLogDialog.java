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
import org.ametro.app.Constants;
import org.ametro.util.StringUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class ChangeLogDialog extends AlertDialog implements OnClickListener {

	private String mAppName;
	private String mVersionName;
	private String[] mChangelog;
		
	public static void show(Context context){
		try {
			new ChangeLogDialog(context).show();
		} catch (Exception e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
				Log.w(Constants.LOG_TAG_MAIN,"Failed to show about dialog", e);
			}
		}
	}
	
	protected ChangeLogDialog(Context context) throws Exception {
		super(context);
		bindData(context);
		setTitle(mAppName + " v." + mVersionName );
		setCancelable(true);
		setIcon(android.R.drawable.ic_dialog_info);
		setButton(BUTTON_POSITIVE, context.getString(R.string.btn_close), this);
		
		final TextView message = new TextView(context);
		final SpannableString s = new SpannableString(getContent());
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		message.setPadding(5, 5, 5, 5);
		message.setTextColor(context.getResources().getColorStateList(R.color.dialog_text_color));
		message.setLinkTextColor(context.getResources().getColorStateList(R.color.links_color));

		final ScrollView view= new ScrollView(context);
		view.addView(message);
		setView(view);
	}

	protected Spannable getContent() {
		try {
			StringBuilder htmlText = new StringBuilder();
			int count = 10;
			for (int i = mChangelog.length - 1; i >= 0; i--) {
				String change = mChangelog[i];
				
				String[] mainParts = StringUtil.fastSplit(change, ':');
				String version = mainParts[0];
				String changeList = mainParts[1];
				String[] changes = StringUtil.fastSplit(changeList,';');
				
				htmlText.append("<p>");
				htmlText.append(version);
				htmlText.append(":<ul>");
				for(String changeText: changes){
					htmlText.append(changeText);
					htmlText.append(";<br/>");
				}
				htmlText.append("</ul></p>");
				count--;
				if (count <= 0) {
					break;
				}
			}
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
		dismiss();
	}
}
