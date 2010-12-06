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
import org.ametro.ui.DonateActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class AboutDialog {

		
	public static void show(Context context){
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info;
			info = manager.getPackageInfo(context.getPackageName(), 0);
			String versionName = info.versionName;
			String appName = context.getString( info.applicationInfo.labelRes );
			
			final Context dialogContex = context;
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder
			.setTitle(appName + " v." + versionName)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setCancelable(true)
			.setItems(R.array.about_list, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch(which){
					case 0:
						AboutDetailsDialog.show(dialogContex);
						break;
					case 1:
						ChangeLogDialog.show(dialogContex);
						break;
					case 2:
						EULADialog.show(dialogContex);
						break;
					case 3:
						Intent intent = new Intent(dialogContex, DonateActivity.class);
						dialogContex.startActivity(intent);
						break;
					}
					dialog.dismiss();
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		} catch (Exception e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
				Log.w(Constants.LOG_TAG_MAIN,"Failed to show about dialog", e);
			}
		}
	}
	

}
