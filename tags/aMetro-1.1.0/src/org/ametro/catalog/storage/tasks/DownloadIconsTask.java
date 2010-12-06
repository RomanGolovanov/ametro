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
package org.ametro.catalog.storage.tasks;

import java.io.File;
import java.net.URI;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.ui.CatalogTabHostActivity;
import org.ametro.util.FileUtil;
import org.ametro.util.IDownloadListener;
import org.ametro.util.WebUtil;
import org.ametro.util.ZipUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import static org.ametro.app.Notifications.DOWNLOAD_ICONS_ID;

public class DownloadIconsTask extends BaseTask implements IDownloadListener {

	private static class Holder{
		private static DownloadIconsTask INSTANCE = new DownloadIconsTask();
	}
	
	public static DownloadIconsTask getInstance(){
		return Holder.INSTANCE;
	}
	
	
	private static final int DOWNLOAD_ICON = android.R.drawable.stat_sys_download;
	private static final int UNPACK_ICON = android.R.drawable.stat_sys_download;
	private static final int DONE_ICON = android.R.drawable.stat_sys_download_done;
	private static final int FAILED_ICON = android.R.drawable.stat_sys_warning;
	
	private String mProgressMessage;
	
	private NotificationManager mNotificationManager;
	private Context mContext;
	private Resources mResources;
	
	private boolean mCompleted;
	
	public boolean isAsync() {
		return true;
	}
	
	public Object getTaskId() {
		return DownloadIconsTask.class;
	}
	
	protected void run(Context context) throws Exception {
		mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mContext = context;
		mResources = context.getResources();
		mProgressMessage = mResources.getString(R.string.msg_icons_pack_download_progress);

		FileUtil.touchDirectory(Constants.TEMP_CATALOG_PATH);
		FileUtil.touchDirectory(Constants.ICONS_PATH);
		final File temp = GlobalSettings.getTemporaryDownloadIconFile();
		
		mCompleted = false;
		for(String iconUrl : Constants.ICONS_URLS){
			try{
				WebUtil.downloadFile(null, URI.create(iconUrl), temp, true, this);
				if(mCompleted){
					break;
				}
			}catch(Exception ex){
				FileUtil.delete(temp);
			}
		}
		
	}

	private DownloadIconsTask(Parcel in) {
	}

	private DownloadIconsTask(){
		
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
	}

	public static final Parcelable.Creator<DownloadIconsTask> CREATOR = new Parcelable.Creator<DownloadIconsTask>() {
		public DownloadIconsTask createFromParcel(Parcel in) {
			return new DownloadIconsTask(in);
		}

		public DownloadIconsTask[] newArray(int size) {
			return new DownloadIconsTask[size];
		}
	};

	public void onBegin(Object context, File file) {
		displayNotification(mResources.getString(R.string.msg_begin_download_icons),true,DOWNLOAD_ICON);
	}

	public void onDone(Object context, File file) throws Exception {
		displayNotification(mResources.getString(R.string.msg_begin_unpack_icons),true,UNPACK_ICON);
		final File path = Constants.ICONS_PATH;
		ZipUtil.unzip(file, path);
		displayNotification(mResources.getString(R.string.msg_icons_pack_installed),false,DONE_ICON);
		mCompleted = true;
	}

	public boolean onUpdate(Object context, long position, long total) throws Exception {
		String msg = mProgressMessage +  " " + position + "/" + total;
		displayNotification(msg,true,DOWNLOAD_ICON);
		update(position,total, msg);
		cancelCheck(); // can throws CanceledException 
		return true;
	}

	public void onFailed(Object context, File file, Throwable reason) {
		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
			Log.e(Constants.LOG_TAG_MAIN,mResources.getString(R.string.msg_icons_pack_download_failed),reason);
		}
		displayNotification(mResources.getString(R.string.msg_icons_pack_download_failed),false,FAILED_ICON);
	}
	
	public void onCanceled(Object context, File file) {
		displayNotification(mResources.getString(R.string.msg_icons_pack_download_canceled),false,FAILED_ICON);
	}
	
	private void displayNotification(String message, boolean fixed, int iconId)
	{
		Notification notification = new Notification(iconId, message,System.currentTimeMillis());
		if(fixed){
			notification.flags |= Notification.FLAG_NO_CLEAR;
		}
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, CatalogTabHostActivity.class), 0);
		notification.setLatestEventInfo(mContext, "aMetro" ,message, contentIntent);
		mNotificationManager.notify(DOWNLOAD_ICONS_ID, notification);
	}

	public static BaseTask create(boolean force) {
		if(force){
			FileUtil.delete(GlobalSettings.getTemporaryDownloadIconFile());
		}
		return new DownloadIconsTask();
	}

	public static boolean isRunning() {
		return Holder.INSTANCE.mIsRunning;
	}	
}
