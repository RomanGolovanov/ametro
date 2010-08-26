/*
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

package org.ametro.activity;

import static org.ametro.Constants.LOG_TAG_MAIN;
import static org.ametro.Constants.PREFERENCE_FAVORITE_ROUTES;
import static org.ametro.Constants.PREFERENCE_SCROLL_POSITION;
import static org.ametro.Constants.PREFERENCE_ZOOM_LEVEL;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import org.ametro.ApplicationEx;
import org.ametro.Constants;
import org.ametro.GlobalSettings;
import org.ametro.MapUri;
import org.ametro.R;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.storage.ICatalogStorageListener;
import org.ametro.dialog.AboutDialog;
import org.ametro.dialog.ChangeLogDialog;
import org.ametro.dialog.EULADialog;
import org.ametro.dialog.LocationSearchDialog;
import org.ametro.dialog.SchemeListDialog;
import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.model.TransportStation;
import org.ametro.model.ext.ModelLocation;
import org.ametro.model.route.RouteContainer;
import org.ametro.model.route.RouteView;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.model.util.ModelUtil;
import org.ametro.util.DateUtil;
import org.ametro.util.StringUtil;
import org.ametro.widget.VectorMapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class MapViewActivity extends Activity implements OnClickListener, OnDismissListener, ICatalogStorageListener {

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_EULA:
			EULADialog dlg = new EULADialog(this);
			dlg.setOnDismissListener(this);
			return dlg;
		case DIALOG_RELOAD_MAP:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
			.setCancelable(false)
			.setTitle(R.string.msg_map_reload_title)
			.setMessage(R.string.msg_map_reload_confirmation)
			.setIcon(android.R.drawable.ic_dialog_map)
			.setPositiveButton(R.string.btn_apply, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mDisableMapReload = true;
					onInitializeMapView(mModelFileName, null, true);
				}
			})
			.setNegativeButton(R.string.btn_later, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mDisableMapReload = true;
					dialog.cancel();
				}
			});
			return builder.create();
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	public void onDismiss(DialogInterface dialog) {
		if(dialog instanceof EULADialog){
			if(!GlobalSettings.isAcceptedEULA(this)){
				finish();
			}		
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			MapView view = getMapView();
			if(view!=null){
				if(isNavigationActive()){
					clearNavigation(true);
					return true;
				}
				if(!view.systemName.equalsIgnoreCase(mModel.viewSystemNames[0])){
					onInitializeMapView(getMapName(), mModel.viewSystemNames[0], false);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void onClick(View src) {
		if(src == mNavigatePreviousButton){
			navigatePreviuosStation();
		} 
		if(src == mNavigateNextButton){
			navigateNextStation();
		}
		if(src == mNavigateClearButton){
			clearNavigation(true);
		}
		if(src == mNavigateListButton && mCurrentRouteView!=null){
			startActivity(new Intent(this, RouteViewActivity.class));
		}
		if(src == mNavigateListButton && mCurrentRouteView == null && mNavigationStations!=null){
			startActivity(new Intent(this, StationSearchActivity.class));
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDisableEulaDialog = false;
		if(Instance != null){
			mModel = Instance.mModel;
			mModelTimestamp = Instance.mModelTimestamp;
			mModelLastModified = Instance.mModelLastModified;
			mMapView = Instance.mMapView;
			mRouteContainer = Instance.mRouteContainer;
			mModelFileName = Instance.mModelFileName;
			mMapViewName = Instance.mMapViewName;
			if(mModel!=null && isUpdateNeeded()){
				mModel = null;
				mMapView = null;
			}
		}
		Instance = this;
		Instance.mDefaultLocale = Locale.getDefault();

		setupLocale();
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL); 
		setContentView(R.layout.operation_wait_full_screen);

		if(mModel!=null){
			onShowMap(mModel, mMapView);
		}else{
			Intent intent = getIntent();
			Uri uri = intent != null ? intent.getData() : null;
			if (uri != null) {
				String mapName = MapUri.getMapName(uri);
				onInitializeMapView(mapName, null, false);
			} else {
				loadDefaultMapName();
				if (mModelFileName == null) {
					onRequestMap(true);
				} else {
					onInitializeMapView(mModelFileName, mMapViewName, false);
				}
			}
		}
	}

	protected void onNewIntent(Intent intent) {
		String systemName = intent.getStringExtra(EXTRA_SYSTEM_NAME);
		if (systemName!=null) {
			String mapPath = GlobalSettings.getLocalCatalogMapFileName(systemName);
			if(!mapPath.equalsIgnoreCase(getMapName())){
				onInitializeMapView(mapPath,null, false);
			}else if(isUpdateNeeded()){
				mDisableMapReload = true;
				onInitializeMapView(mapPath,null, true);
			}
		} 
		super.onNewIntent(intent);
	}
	
	protected void onResume() {
		if(!mDisableMapReload && isUpdateNeeded()){
			showDialog(DIALOG_RELOAD_MAP);
		}
		ApplicationEx.getInstance().getCatalogStorage().addCatalogStorageListener(this);
		super.onResume();
	}
	
	protected void onPause() {
		onSaveMapState();
		ApplicationEx.getInstance().getCatalogStorage().removeCatalogStorageListener(this);
		super.onPause();
	}

	protected void onDestroy() {
		//Instance = null;
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_MAP:
			if(resultCode == CatalogTabHostActivity.RESULT_EULA_CANCELED){
				mDisableEulaDialog = true;
				finish();
			}
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				if (uri != null) {
					String mapName = MapUri.getMapName(uri);
					if(!mapName.equalsIgnoreCase(getMapName())){
						onInitializeMapView(mapName,null, false);
					}else if(isUpdateNeeded()){
						mDisableMapReload = true;
						onInitializeMapView(mapName,null, true);
					}
				} 
			}else if(resultCode == RESULT_CANCELED && mModelFileName == null){
				finish();
				break;
			}
			if(isConfigurationChanged()){
				setupLocale();
				if (mModelFileName != null) {
					onInitializeMapView(mModelFileName, mMapViewName, false);
				}			
				return;
			}
			break; 
		case REQUEST_SETTINGS:
			updateAntiAliasingState();
			if(isConfigurationChanged()){
				setupLocale();
				if (mModelFileName != null) {
					onInitializeMapView(mModelFileName, mMapViewName, false);
				}			
			}
			break;
		case REQUEST_LOCATION:
			if(resultCode == RESULT_OK){
				Location location = data.getParcelableExtra(LocationSearchDialog.LOCATION);
				if(location!=null){
					mLocationSearchTask = new LocationSearchTask();
					mLocationSearchTask.execute(location);
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean isUpdateNeeded(){
		if(mModelFileName!=null && mModel!=null){
			File file = new File(mModelFileName);
			if(!file.exists()) {
				clearDefaultMapName();
				setContentView(R.layout.map_view_empty);
				return false;
			}
			if(file.lastModified()!=mModelLastModified){
				//Log.w(Constants.LOG_TAG_MAIN,"Map file timestamps aren't same: " + file.lastModified() + " vs " + mModelLastModified );
				return true;
			}
			Model description = ModelBuilder.loadModelDescription(mModelFileName);
			if(description!=null){
				long timestamp = description.timestamp;
				if(timestamp!=mModelTimestamp){
					return true;
				}
			}
		}
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_FIND, 0, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, MAIN_MENU_INFO, 1, R.string.menu_info).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, MAIN_MENU_ROUTES, 2, R.string.menu_routes).setIcon(android.R.drawable.ic_menu_directions);
		menu.add(0, MAIN_MENU_LAYERS, 3, R.string.menu_layers).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, MAIN_MENU_SCHEMES, 4, R.string.menu_schemes).setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(0, MAIN_MENU_LIBRARY, 5, R.string.menu_library).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, MAIN_MENU_SETTINGS, 6, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 7, R.string.menu_about).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, MAIN_MENU_LOCATION, 8, R.string.menu_location).setIcon(android.R.drawable.ic_menu_mylocation);
		//menu.add(0, MAIN_MENU_EXPERIMENTAL, 9,R.string.menu_experimental);

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_INFO).setEnabled(mCurrentStation!=null);

		menu.findItem(MAIN_MENU_FIND).setEnabled(mModel!=null);
		menu.findItem(MAIN_MENU_INFO).setEnabled(mModel!=null);
		menu.findItem(MAIN_MENU_ROUTES).setEnabled(mModel!=null);
		menu.findItem(MAIN_MENU_LAYERS).setVisible(false); //menu.findItem(MAIN_MENU_LAYERS).setEnabled(false);//mModel!=null);
		menu.findItem(MAIN_MENU_SCHEMES).setEnabled(mModel!=null);
		menu.findItem(MAIN_MENU_LOCATION).setVisible(mModel!=null && GlobalSettings.isLocateUserEnabled(this));
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_FIND:
			onSearchRequested();
			return true;
		case MAIN_MENU_LIBRARY: 
			onRequestMap(false);
			return true;
		case MAIN_MENU_ROUTES:
			startActivity(new Intent(this, RouteCreateActivity.class));
			return true;
		case MAIN_MENU_SETTINGS:
			startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
			return true;
		case MAIN_MENU_ABOUT:
			AboutDialog.show(this);
			return true;
		case MAIN_MENU_LAYERS:
			return true;
		case MAIN_MENU_SCHEMES:
			SchemeListDialog dialog = new SchemeListDialog(this, mModel, getMapView()){
				public void onMapViewSelected(String mapViewSystemName) {
					if(mapViewSystemName!=getMapView().systemName){
						clearNavigation(true);
						onInitializeMapView(mModelFileName, mapViewSystemName, false);
					}
					super.onMapViewSelected(mapViewSystemName);
				}
			};
			dialog.show();
			return true;
		case MAIN_MENU_INFO:
			String systemName = getSystemMapName();
			Intent detailsIntent = new Intent(this, MapDetailsActivity.class);
			detailsIntent.putExtra(MapDetailsActivity.EXTRA_HIDE_OPEN, true);
			detailsIntent.putExtra(MapDetailsActivity.EXTRA_SYSTEM_NAME, systemName);
			startActivity(detailsIntent);
//			if(mCurrentStation!=null){
//				startActivity(new Intent(this, StationViewActivity.class));
//			}
			return true;
		case MAIN_MENU_LOCATION:
			startActivityForResult(new Intent(this, LocationSearchDialog.class), REQUEST_LOCATION);
			return true;
		case MAIN_MENU_EXPERIMENTAL:
			startActivity(new Intent(this, CatalogTabHostActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateAntiAliasingState(){
		if(mVectorMapView!=null){
			mVectorMapView.setAntiAliasingDisableOnScroll(isAntiAliasingDisableOnScroll());
			mVectorMapView.setAntiAliasingEnabled(isAntiAliasingEnabled());
			mVectorMapView.postInvalidate();
		}
	}

	public Locale getLocale(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		final String localeName = settings.getString(getString(R.string.pref_locale_key), null);
		return !StringUtil.isNullOrEmpty(localeName) && !"auto".equalsIgnoreCase(localeName)  
		? new Locale(localeName) 
		: mDefaultLocale;
	}

	public void setupLocale() {
		Locale.setDefault(getLocale());
	}

	public boolean isAntiAliasingEnabled(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(getString(R.string.pref_anti_alias_key), true);
	}

	public boolean isAntiAliasingDisableOnScroll(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(getString(R.string.pref_anti_alias_disable_on_scroll_key), true);
	}

	public boolean isEnabledAddonsImport() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		return settings.getBoolean(getString(R.string.pref_auto_import_addons_key), false);
	}

	public boolean isConfigurationChanged() {
		Locale currentLocale = Locale.getDefault();
		Locale newLocale = getLocale();
		if(!currentLocale.equals(newLocale)){
			return true;
		}
		return false;
	}

	public void loadDefaultMapName(){
		GlobalSettings.MapPath path = GlobalSettings.getCurrentMap(this);
		mModelFileName = path.FilePath;
		mMapViewName = path.ViewName;		
	}

	public void saveDefaultMapName() {
		GlobalSettings.setCurrentMap(this, mModelFileName, mMapViewName);
	}

	public void clearDefaultMapName() {
		GlobalSettings.clearCurrentMap(this);
		mModelFileName = null;
		mModel = null;
		mMapView = null;
	}

	public void addFavoriteRoute(int fromId, int toId)
	{
		ArrayList<Point> routes = new ArrayList<Point>( Arrays.asList(getFavoriteRoutes()) );
		Point r = new Point(fromId, toId);
		if(!routes.contains(r)){
			routes.add(0, r);
			setFavoriteRoutes((Point[]) routes.toArray(new Point[routes.size()]));
		}
	}

	public void removeFavoriteRoute(int fromId, int toId)
	{
		ArrayList<Point> routes = new ArrayList<Point>( Arrays.asList(getFavoriteRoutes()) );
		Point r = new Point(fromId, toId);
		if(routes.contains(r)){
			routes.remove(r);
			setFavoriteRoutes((Point[]) routes.toArray(new Point[routes.size()]));
		}
	}

	public boolean isFavoriteRoute(int fromId, int toId)
	{
		ArrayList<Point> routes = new ArrayList<Point>( Arrays.asList(getFavoriteRoutes()) );
		Point r = new Point(fromId, toId);
		return routes.contains(r);
	}

	public void clearFavoriteRoutes()
	{
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_FAVORITE_ROUTES + "_" + mModelFileName, "");
		editor.commit();
	}

	public void setFavoriteRoutes(Point[] routes)
	{
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		for(Point p : routes){
			sb.append(StringUtil.formatPoint(p));
			idx++;
			if(idx<routes.length){
				sb.append(",");
			}
		}
		String routesBudle = sb.toString();
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_FAVORITE_ROUTES + "_" + mModelFileName, routesBudle);
		editor.commit();
	}

	public Point[] getFavoriteRoutes()
	{
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		String routes = preferences.getString(PREFERENCE_FAVORITE_ROUTES + "_" + mModelFileName, "");
		return StringUtil.parsePointArray(routes);

	}

	public void saveScrollPosition(PointF position) {
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = preferences.edit();
		String scrollPosition = StringUtil.formatPointF(position);
		editor.putString(PREFERENCE_SCROLL_POSITION + "_" + mModelFileName + "_" + mMapViewName, scrollPosition);
		editor.commit();
		if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(LOG_TAG_MAIN, getString(R.string.log_save_map_position) + scrollPosition);
		}
	}

	public PointF loadScrollPosition() {
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		String pref = preferences.getString(PREFERENCE_SCROLL_POSITION + "_" + mModelFileName + "_" + mMapViewName, null);
		if (pref != null) {
			return StringUtil.parsePointF(pref);
		} else {
			return null;
		}
	}

	public void clearScrollPosition(String mapName) {
		if (mapName != null) {
			SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.remove(PREFERENCE_SCROLL_POSITION + "_" + mapName);
			editor.commit();
		}
	}

	public void saveZoom(int zoomLevel) {
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFERENCE_ZOOM_LEVEL + "_" + mModelFileName + "_" + mMapViewName, Integer.toString(zoomLevel));
		editor.commit();
		if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG)){
			Log.d(LOG_TAG_MAIN, getString(R.string.log_save_map_zoom) + zoomLevel);
		}
	}

	public Integer loadZoom() {
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
		String pref = preferences.getString(PREFERENCE_ZOOM_LEVEL + "_" + mModelFileName + "_" + mMapViewName, null);
		if (pref != null) {
			return StringUtil.parseNullableInteger(pref);
		} else {
			return null;
		}
	}

	public void clearZoom(String mapName) {
		if (mapName != null) {
			SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, 0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.remove(PREFERENCE_ZOOM_LEVEL + "_" + mapName);
			editor.commit();
		}
	}

	public MapView getMapView() {
		return mMapView;
	}

	public String getMapName() {
		return mModelFileName;
	}

	public String getSystemMapName() {
		return mModelFileName==null ? null : mModelFileName.substring(mModelFileName.lastIndexOf('/')+1);
	}

	public void onCatalogMapChanged(String systemName) {
		final String name = systemName;
		mUIDispatcher.post(new Runnable() {
			public void run() {
				String mapPath = GlobalSettings.getLocalCatalogMapFileName(name);
				if(mModelFileName!=null && mModelFileName.equalsIgnoreCase(mapPath)){
					showDialog(DIALOG_RELOAD_MAP);
				}
			}
		});
	}
	
	public void onCatalogMapDownloadDone(String systemName) {
		onCatalogMapChanged(systemName);
	}

	public void onCatalogMapImportDone(String systemName) {
		onCatalogMapChanged(systemName);
	}

	public void onCatalogFailed(int catalogId, String message) {
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
	}

	public void onCatalogMapDownloadFailed(String systemName, Throwable ex) {
	}

	public void onCatalogMapDownloadProgress(String systemName, int progress, int total) {
	}

	public void onCatalogMapImportFailed(String systemName, Throwable e) {
	}

	public void onCatalogMapImportProgress(String systemName, int progress, int total) {
	}

	public void onCatalogProgress(int catalogId, int progress, int total, String message) {
	}
	
	/*package*/ StationView getCurrentStation()
	{
		return mCurrentStation;	
	}

	/*package*/ void setCurrentStation(StationView station){
		if(station!=null && mNavigationStations.contains(station)){
			mCurrentStation = station;
			int idx = mNavigationStations.indexOf(mCurrentStation);
			mNavigatePreviousButton.setEnabled( idx != 0 );
			mNavigateNextButton.setEnabled( idx != (mNavigationStations.size()-1) );
			if(mNavigationStations.size()>1){
				mNavigateListButton.setVisibility(View.VISIBLE);
				mNavigatePreviousButton.setVisibility(View.VISIBLE);
				mNavigateNextButton.setVisibility(View.VISIBLE);
			}else{
				mNavigateListButton.setVisibility(View.GONE);
				mNavigatePreviousButton.setVisibility(View.INVISIBLE);
				mNavigateNextButton.setVisibility(View.INVISIBLE);
			}
			mUIDispatcher.post(mUpdateUI);
		}else{
			mCurrentStation = null;
		}
	}

	/*package*/ ArrayList<StationView> getNavigationStations()
	{
		return mNavigationStations;
	}

	/*package*/ void setNavigationStations(ArrayList<StationView> stations){
		boolean refreshNeeded = (stations != mNavigationStations) || (stations == null && mNavigationStations!=null);
		if(refreshNeeded){
			if(stations!=null){
				mCurrentRouteView = null;
				mNavigationSegments = null;
				mNavigationTransfers = null;
				mNavigationStations = stations;
				mCurrentStation = stations.get(0);
				showNavigationControls();
			}else{
				hideNavigationControls();
				mCurrentRouteView = null;
				mNavigationStations = null;
				mNavigationSegments = null;
				mNavigationTransfers = null;
				mCurrentStation = null;
			}
			mVectorMapView.setModelSelection(stations, null, null);
			mVectorMapView.postInvalidate();
		}
	}

	/*package*/ RouteView getCurrentRouteView() {
		return mCurrentRouteView;
	}

	/*package*/ RouteContainer getNavigationRoute()
	{
		return mRouteContainer;
	}

	/*package*/ void setNavigationRoute(RouteContainer result){
		boolean refreshNeeded = (result != mRouteContainer) || (result == null && mRouteContainer!=null) || (result!=null && mRouteContainer == null);
		if(refreshNeeded){
			mRouteContainer = result;
			mCurrentRouteView = new RouteView(mMapView, mRouteContainer.getDefaultRoute());
			if(result!=null){
				mNavigationSegments = mCurrentRouteView.getSegments();
				mNavigationStations = mCurrentRouteView.getStations();
				mNavigationTransfers = mCurrentRouteView.getTransfers();
				setCurrentStation( mNavigationStations.get(0) );
				showNavigationControls();
			}else{
				hideNavigationControls();
				mNavigationStations = null;
				mNavigationSegments = null;
				mNavigationTransfers = null;
				setCurrentStation(null);
			}
			mVectorMapView.setModelSelection(mNavigationStations, mNavigationSegments,mNavigationTransfers);
			mVectorMapView.postInvalidate();
		}
	}

	/*package*/ boolean isNavigationActive(){
		return mRouteContainer!=null || mNavigationStations!=null;
	}
	
	/*package*/ void clearNavigation(boolean changeUI){
		if(changeUI){
			hideNavigationControls();
		}
		mRouteContainer = null;
		mCurrentRouteView = null;
		mNavigationStations = null;
		mNavigationSegments = null;
		mNavigationTransfers = null;
		if(changeUI){
			setCurrentStation(null);
			mVectorMapView.setModelSelection(mNavigationStations, mNavigationSegments,mNavigationTransfers);
			mVectorMapView.postInvalidate();
		}
	}

	private void onSaveMapState() {
		if (mMapView != null && mVectorMapView != null && mModelFileName != null) {
			PointF pos = mVectorMapView.getModelScrollCenter();
			int zoom = mVectorMapView.getModelZoom();;
			saveScrollPosition(pos);
			saveZoom(zoom);
		}
	}

	private void onRestoreMapState() {
		if (mMapView != null && mVectorMapView != null
				&& mModelFileName != null) {
			Integer zoom = loadZoom();
			if (zoom != null) {
				if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG)){ 
					Log.d(LOG_TAG_MAIN, getString(R.string.log_restore_map_zoom) + zoom);
				}
				mVectorMapView.setZoom(zoom);
			} else {
				zoom = VectorMapView.DEFAULT_ZOOM_LEVEL+1;
				if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG)){
					Log.d(LOG_TAG_MAIN, getString(R.string.log_default_map_zoom) + zoom);
				}
			 	mVectorMapView.setZoom(zoom);
			}
			PointF pos = loadScrollPosition();
			if (pos != null) {
				if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG)){
					Log.d(LOG_TAG_MAIN, getString(R.string.log_restore_map_position) + pos.x + "x" + pos.y);
				}
				mVectorMapView.setModelScrollCenter(pos);
			} else {
				int x = mMapView.width / 2;
				int y = mMapView.height / 2;
				if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG))
					Log.d(LOG_TAG_MAIN, getString(R.string.log_default_map_position) + x
							+ "x" + y);
				mVectorMapView.setModelScrollCenter(new PointF(x, y));
			}

		}
	}
	
	private void onRequestMap(boolean setNoMapLoadingView) {
		if (setNoMapLoadingView) {
			setContentView(R.layout.map_view_empty);
		}
		Intent i = new Intent(this, CatalogTabHostActivity.class);
		if (mModelFileName != null) {
			i.setData(MapUri.create(mModelFileName));
		}
		startActivityForResult(i, REQUEST_MAP);
	}

	private void onInitializeMapView(String mapName, String viewName, boolean force) {
		if(!mDisableEulaDialog && !GlobalSettings.isAcceptedEULA(this)){
			showDialog(DIALOG_EULA);
			return;
		}
		if(!GlobalSettings.isChangeLogShowed(this)){
			ChangeLogDialog.show(this);
			GlobalSettings.setChangeLogShowed(this);
		}
		if(!force && mModel!=null && mMapViewName!=null){
			String mapNameLoaded = mModel.fileSystemName;
			String schemeNameLoaded = mMapView.systemName;
			if(mapNameLoaded.equals(mapName) ){
				if(schemeNameLoaded.equals(viewName)){
					// map and view is similar
					// so only need to check locales
					Locale locale = getLocale();
					mLoadLocaleTask = new LoadLocaleTask();
					mLoadLocaleTask.execute(locale);
				}else{
					// load new view
					//clearNavigation(true);
					MapView v = mModel.loadView(viewName);
					if(v!=null){
						onShowMap(mModel, v);
					}else{
						Toast.makeText(MapViewActivity.this, "Scheme loading error", Toast.LENGTH_SHORT).show();
					}

				}
			}else{
				mInitTask = new InitTask();
				mInitTask.execute(mapName, viewName);
			}
		}else{
			mInitTask = new InitTask();
			mInitTask.execute(mapName, viewName);
		}
		
	}


	private void onShowMap(Model model, MapView view) {

		if(mModel!=null && mMapView!=null){
			onSaveMapState();
		}

		mDisableMapReload = false;
		mModel = model;
		mModelTimestamp = model.timestamp;
		mModelLastModified = (new File(model.fileSystemName)).lastModified();
		mMapView = view;

		clearNavigation(false);
		
// 		TODO: resolve errors with map/view reloading.
//		Model previousModel = mModel;
//		MapView previousMap = mMapView;
//		if(previousModel==model){
//			// we only change view, model didn't change
//			if( previousMap!=null && previousMap.systemName.equals(view.systemName)){
//				if(mRouteContainer!=null){
//					mCurrentRouteView = new RouteView(view, mRouteContainer.getDefaultRoute());
//					mNavigationSegments = mCurrentRouteView.getSegments();
//					mNavigationStations = mCurrentRouteView.getStations();
//					mNavigationTransfers = mCurrentRouteView.getTransfers();
//					if(mNavigationStations!=null && mNavigationStations.size()>0){
//						mCurrentStation = mNavigationStations.get(0);
//					}
//				}else if (mNavigationStations!=null){
//					clearNavigation(false);
//				}else{
//					clearNavigation(false);
//				}
//			}else{
//
//				if(mRouteContainer!=null){
//					mCurrentRouteView = new RouteView(view, mRouteContainer.getDefaultRoute());
//					mNavigationSegments = mCurrentRouteView.getSegments();
//					mNavigationStations = mCurrentRouteView.getStations();
//					mNavigationTransfers = mCurrentRouteView.getTransfers();
//					if(mNavigationStations!=null && mNavigationStations.size()>0){
//						mCurrentStation = mNavigationStations.get(0);
//					}
//				}else {				
//					clearNavigation(false);
//				}
//			}
//		}else{
//			// we change model, so need to drop any route/selection
//			clearNavigation(false);
//		}

		if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG))
			Log.d(LOG_TAG_MAIN, getString(R.string.log_loaded_subway_map) + mMapView.systemName
					+ getString(R.string.log_with_size) + mMapView.width + "x"
					+ mMapView.height);

		setContentView(R.layout.map_view);

		mVectorMapView = (VectorMapView) findViewById(R.id.browse_vector_map_view);
		updateAntiAliasingState();

		mVectorMapView.setModel(mMapView);
		mVectorMapView.setModelSelection(mNavigationStations, mNavigationSegments, mNavigationTransfers);
		mVectorMapView.setZoomControls((ZoomControls) findViewById(R.id.browse_vector_map_zoom));

		mNavigationPanelTop = (View)findViewById(R.id.browse_vector_map_panel_top);
		mNavigationPanelBottom = (View)findViewById(R.id.browse_vector_map_panel_bottom);

		mNavigatePreviousButton = (ImageButton)findViewById(R.id.browse_vector_map_button_prev);
		mNavigateNextButton = (ImageButton)findViewById(R.id.browse_vector_map_button_next);
		mNavigateClearButton = (ImageButton)findViewById(R.id.browse_vector_map_button_clear);
		mNavigateListButton = (ImageButton)findViewById(R.id.browse_vector_map_button_list);

		mNavigateTimeText = (TextView)findViewById(R.id.browse_vector_map_time);

		mNavigatePreviousButton.setOnClickListener(this);
		mNavigateNextButton.setOnClickListener(this);
		mNavigateClearButton.setOnClickListener(this);
		mNavigateListButton.setOnClickListener(this);

		if(mCurrentRouteView==null && mCurrentStation == null){
			hideNavigationControls();
		} 

		mModelFileName = mMapView.owner.fileSystemName;
		mMapViewName = view.systemName;

		onRestoreMapState();

		mVectorMapView.requestFocus();

		saveDefaultMapName();
	}

	private void hideNavigationControls() {
		mNavigationPanelBottom.setVisibility(View.INVISIBLE);
		mNavigationPanelTop.setVisibility(View.INVISIBLE);
	}

	private void showNavigationControls() {
		mNavigationPanelBottom.setVisibility(View.VISIBLE);
		mNavigationPanelTop.setVisibility(View.VISIBLE);
		if(mCurrentRouteView!=null){
			long secs = mCurrentRouteView.getStationDelay(mNavigationStations.get(mNavigationStations.size()-1));
			secs = ( secs/60 + (secs%60 == 0 ? 0 : 1) ) * 60;
			Date date = new Date(secs * 1000);
			mNavigateTimeText.setText(String.format(getString(R.string.route_time_format), DateUtil.getDateUTC(date, "HH"), DateUtil.getDateUTC(date, "mm")));
			mNavigateTimeText.setVisibility(View.VISIBLE);
		}else{
			mNavigateTimeText.setText("");
			mNavigateTimeText.setVisibility(View.INVISIBLE);
		}

	}

	private void navigateNextStation(){
		if(mNavigationStations!=null && mNavigationStations.size()>0){
			if(mCurrentStation == null){
				setCurrentStation(mNavigationStations.get(0));
			}else{
				int idx = mNavigationStations.indexOf(mCurrentStation) + 1;
				if(idx < mNavigationStations.size()){
					setCurrentStation(mNavigationStations.get(idx));
				}
			}
		}
	}

	private void navigatePreviuosStation(){
		if(mNavigationStations!=null && mNavigationStations.size()>0){
			if(mCurrentStation == null){
				setCurrentStation(mNavigationStations.get(mNavigationStations.size()-1));
			}else{
				int idx = mNavigationStations.indexOf(mCurrentStation) - 1;
				if(idx >= 0){
					setCurrentStation(mNavigationStations.get(idx));
				}
			}
		}
	}

	private class LoadLocaleTask extends AsyncTask<Locale, Void, Void>
	{
		ProgressDialog mProgressDialog;

		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(MapViewActivity.this, null, getString(R.string.msg_locale_loading), true);
			super.onPreExecute();
		}

		protected Void doInBackground(Locale... params) {
			mModel.setLocale(params[0]);
			mVectorMapView.updateModel();
			return null;
		}

		protected void onPostExecute(Void result) {
			mProgressDialog.dismiss();
			mVectorMapView.setModelSelection(mNavigationStations, mNavigationSegments, mNavigationTransfers);
			mVectorMapView.postInvalidate();
			super.onPostExecute(result);
		}
	}

	private class InitTask extends AsyncTask<String, Void, Model> {

		Throwable mError;
		String mMapName;
		String mViewName;

		protected void onPreExecute() {
			mError = null;
			setContentView(R.layout.operation_wait_full_screen);
			super.onPreExecute();
		}

		protected Model doInBackground(String... params) {
			try {
				mMapName = params[0];
				mViewName = params[1];
				Model m = ModelBuilder.loadModel(mMapName);
				if(m!=null){
					if(mViewName!=null){
						m.loadView(mViewName);
					}else{
						mViewName = m.viewSystemNames[0];
					}
				}
				return m;

			} catch (Exception e) {
				mError = e;
				if (Log.isLoggable(LOG_TAG_MAIN, Log.ERROR))
					Log.e(LOG_TAG_MAIN, getString(R.string.log_failed_map_loading), e);
				return null;
			}
		}

		protected void onCancelled() {
			setContentView(R.layout.map_view_empty);
			super.onCancelled();
		}

		protected void onPostExecute(Model result) {
			if (result != null) {
				onShowMap(result, result.getView(mViewName));
			} else {
				clearDefaultMapName();
				if (mError != null) {
					Toast.makeText(MapViewActivity.this,
							getString(R.string.msg_error_map_loading),
							Toast.LENGTH_SHORT).show();
				}
				onRequestMap(true);
			}
			super.onPostExecute(result);
		}
	}

	private final Runnable mUpdateUI = new Runnable() {
		public void run() {
			final Point point = ModelUtil.toPoint( mCurrentStation.stationPoint );
			mVectorMapView.scrollModelCenterTo(point.x, point.y);
			mVectorMapView.postInvalidate();
		}
	};


	private class LocationSearchTask extends AsyncTask<Location, Void, StationView> {
		private ProgressDialog dialog;

		protected StationView doInBackground(Location... args) {
			Location location = args[0];
			double latitude = 0, longitude = 0;
			latitude = location.getLatitude();
			longitude = location.getLongitude();

			final Model model = mModel; 
			final float[] distances = new float[3];
			final TreeMap<Integer,StationView> map = new TreeMap<Integer, StationView>();
			for (StationView view : mMapView.stations) {
				if (isCancelled()) {
					return null;
				}
				TransportStation transportStation = model.stations[view.stationId];
				ModelLocation loc = transportStation.location;
				if(loc!=null){
					Location.distanceBetween(loc.latitude, loc.longtitude,latitude, longitude, distances);
					int distance = (int)distances[0];
					if(distance < 50000){
						map.put(distance, view);
					}
				}
			}
			return map.size()>0 ? map.get(map.firstKey()) : null;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(MapViewActivity.this,
					getString(R.string.locate_station_wait_title),
					getString(R.string.locate_wait_text), true);
		}

		protected void onCancelled() {
			super.onCancelled();
			dialog.hide();
		}

		protected void onPostExecute(StationView view) {
			dialog.hide();
			if (view != null) {
				Toast.makeText(
						MapViewActivity.this,
						String.format(getString(R.string.msg_location_station_found),
								view.getName(),
								view.getLineName()),
								Toast.LENGTH_SHORT).show();
				final Point point = ModelUtil.toPoint( view.stationPoint );
				mVectorMapView.scrollModelCenterTo(point.x, point.y);
				mVectorMapView.postInvalidate();
			} else {
				Toast.makeText(MapViewActivity.this, R.string.msg_location_unknown, Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(view);
		}
	}


	static MapViewActivity Instance;

	private static final int DIALOG_EULA = 1;
	private static final int DIALOG_RELOAD_MAP = 2;
	
	private static final int MAIN_MENU_FIND = 1;
	private static final int MAIN_MENU_LIBRARY = 2;
	private static final int MAIN_MENU_ROUTES = 3;
	private static final int MAIN_MENU_LAYERS = 4;
	private static final int MAIN_MENU_SCHEMES = 5;
	private static final int MAIN_MENU_INFO = 6;
	private static final int MAIN_MENU_SETTINGS = 7;
	private static final int MAIN_MENU_ABOUT = 8;
	private static final int MAIN_MENU_LOCATION = 9;
	private static final int MAIN_MENU_EXPERIMENTAL = 10;


	private String mModelFileName;
	private String mMapViewName;
	private MapView mMapView;
	private Model mModel;
	private long mModelTimestamp;
	private long mModelLastModified;

	private VectorMapView mVectorMapView;
	
	private View mNavigationPanelTop;
	private View mNavigationPanelBottom;
	private ImageButton mNavigatePreviousButton;
	private ImageButton mNavigateNextButton;
	private ImageButton mNavigateClearButton;
	private ImageButton mNavigateListButton;
	private TextView mNavigateTimeText;

	private RouteView mCurrentRouteView;
	private RouteContainer mRouteContainer;

	private Handler mUIDispatcher = new Handler();

	private InitTask mInitTask;
	private LoadLocaleTask mLoadLocaleTask;
	private LocationSearchTask mLocationSearchTask;

	private static final int REQUEST_MAP = 1;
	private static final int REQUEST_SETTINGS = 2;
	private static final int REQUEST_LOCATION = 3;

	private ArrayList<StationView> mNavigationStations;
	private ArrayList<SegmentView> mNavigationSegments;
	private ArrayList<TransferView> mNavigationTransfers;
	private StationView mCurrentStation;

	private Locale mDefaultLocale;
	private boolean mDisableEulaDialog;
	
	private boolean mDisableMapReload;
	
	public static final String EXTRA_SYSTEM_NAME = "EXTRA_SYSTEM_NAME";

}
