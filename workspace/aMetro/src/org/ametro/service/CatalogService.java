/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
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
package org.ametro.service;

import org.ametro.ApplicationEx;
import org.ametro.Constants;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CatalogService extends Service {

	public IBinder onBind(Intent intent) {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onBind");
		return null;
	}

	public void onCreate() {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onCreate");
		super.onCreate();
	}

	public void onStart(Intent intent, int startId) {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onStart");
		((ApplicationEx)getApplicationContext()).getCatalogStorage();
	}

	public void onDestroy() {
		Log.i(Constants.LOG_TAG_MAIN, "CatalogService.onDestroy");
		super.onDestroy();
	}

}
