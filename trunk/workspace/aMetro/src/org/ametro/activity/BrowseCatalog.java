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

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.ametro.MapSettings;
import org.ametro.R;
import org.ametro.adapter.CatalogDifferenceListAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMapDifference;
import org.ametro.catalog.storage.LocalCatalogStorage;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ExpandableListView;

public class BrowseCatalog extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MapSettings.checkPrerequisite(this);
		//BrowseVectorMap.Instance.setupLocale();

		invokeLoadLocalCatalogTask();
		invokeLoadRemoteCatalogTask();
	}
	
	protected void onPause() {
		super.onPause();
		cleanupLoadLocalCatalogTask();
		cleanupLoadRemoteCatalogTask();
	};
	
	/*package*/ void setView(){
		synchronized (mutex) {
			mCatalogDifferences = Catalog.diff(mLocal, mRemote);
			setContentView(R.layout.browse_catalog_main);
			mList = (ExpandableListView)findViewById(R.id.browse_catalog_list);
			mList.setAdapter(new CatalogDifferenceListAdapter(this, mCatalogDifferences, Locale.getDefault().getLanguage() ));
		}
	}

	
	/*package*/ void invokeLoadLocalCatalogTask(){
		if(mLoadLocalCatalogTask==null){
			synchronized (mutex) {
				if(mLoadLocalCatalogTask==null){
					mLoadLocalCatalogTask = new LocalCatalogLoadTask();
					mLoadLocalCatalogTask.execute();
					
					setContentView(R.layout.global_wait);
				}
			}
		}		
	}
	
	/*package*/ void cleanupLoadLocalCatalogTask(){
		if(mLoadLocalCatalogTask!=null){
			synchronized (mutex) {
				if(mLoadLocalCatalogTask!=null){
					mLoadLocalCatalogTask.cancel(true);
					mLoadLocalCatalogTask = null;
					
					setContentView(R.layout.browse_catalog_main);
				}
			}
		}		
	}
	
	/*package*/ void invokeLoadRemoteCatalogTask(){
		if(mLoadRemoteCatalogTask==null){
			synchronized (mutex) {
				if(mLoadRemoteCatalogTask==null){
					mLoadRemoteCatalogTask = new RemoteCatalogLoadTask();
					mLoadRemoteCatalogTask.execute();
					
					setContentView(R.layout.global_wait);
				}
			}
		}		
	}
	
	/*package*/ void cleanupLoadRemoteCatalogTask(){
		if(mLoadRemoteCatalogTask!=null){
			synchronized (mutex) {
				if(mLoadRemoteCatalogTask!=null){
					mLoadRemoteCatalogTask.cancel(true);
					mLoadRemoteCatalogTask = null;
					
					setContentView(R.layout.browse_catalog_main);
				}
			}
		}		
	}	
	
	/*package*/ Object mutex = new Object();
	/*package*/ LocalCatalogLoadTask mLoadLocalCatalogTask;
	/*package*/ RemoteCatalogLoadTask mLoadRemoteCatalogTask;
	
	/*package*/ Catalog mLocal;
	/*package*/ Catalog mRemote;
	
	/*package*/ ArrayList<CatalogMapDifference> mCatalogDifferences;
	
	private ExpandableListView mList;
	
	private class RemoteCatalogLoadTask extends AsyncTask<Void, Void, Catalog> {
		protected Catalog doInBackground(Void... params) {
//			String url = MapSettings.getOnlineCatalogUrl(BrowseCatalog.this);
//			return OnlineCatalogStorage.loadCatalog(url);
			File path = MapSettings.getImportCatalog();
			File url = MapSettings.getImportCatalogStorageUrl();
			return LocalCatalogStorage.loadCatalog(url, path, true, LocalCatalogStorage.FILE_TYPE_PMETRO);
		}
		
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			mRemote = result;
			cleanupLoadRemoteCatalogTask();
			setView();
			super.onPostExecute(result);
		}
		
		protected void onCancelled() {
			cleanupLoadRemoteCatalogTask();
			super.onCancelled();
		}
	}
	
	private class LocalCatalogLoadTask extends AsyncTask<Void, Void, Catalog> {
		protected Catalog doInBackground(Void... params) {
			File path = MapSettings.getLocalCatalog();
			File url = MapSettings.getLocalCatalogStorageUrl();
			return LocalCatalogStorage.loadCatalog(url, path, true, LocalCatalogStorage.FILE_TYPE_AMETRO);
		}
		
		protected void onPreExecute() {
			setContentView(R.layout.global_wait);
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			mLocal = result;
			setContentView(R.layout.browse_catalog_main);
			cleanupLoadLocalCatalogTask();
			setView();
			super.onPostExecute(result);
		}
		
		protected void onCancelled() {
			cleanupLoadLocalCatalogTask();
			super.onCancelled();
		}
	}

}
