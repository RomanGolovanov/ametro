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

import org.ametro.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.util.Linkify;
import android.widget.TextView;

public class About extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView view = (TextView)findViewById(R.id.about_text);
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            StringBuilder htmlText = new StringBuilder( String.format(getString(R.string.text_about),info.versionName) );
            
            String[] versionChanges = getResources().getStringArray(R.array.version_changelog);
            htmlText.append("<p>");
            for(int i = versionChanges.length-1; i >= 0; i--){
            	String chages = versionChanges[i];
            	htmlText.append(chages);
            	htmlText.append("<br/>");
            }
        	htmlText.append("</p>");
            Spanned text = Html.fromHtml(htmlText.toString());
            SpannableStringBuilder spannable = new SpannableStringBuilder(text);
            Linkify.addLinks(spannable, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
            view.setText( spannable );
        } catch (NameNotFoundException e) {
            finish();
        }

    }
}
