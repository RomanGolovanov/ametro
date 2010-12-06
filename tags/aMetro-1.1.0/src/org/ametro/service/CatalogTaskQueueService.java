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
package org.ametro.service;


import static org.ametro.app.Notifications.TASK_QUEUE_ID;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.ui.TaskQueuedList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
public class CatalogTaskQueueService extends Service {

	private NotificationManager mNotificationManager;
	private Notification mNotification;

	private String mQueueSizeNotificationTitle;
	private String mQueueSizeText;
	
	@SuppressWarnings("unchecked")
	private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class};
	@SuppressWarnings("unchecked")
	private static final Class[] mStopForegroundSignature = new Class[] { boolean.class};
	
	public static final String EXTRA_TASK_LEFT = "EXTRA_TASK_LEFT";

	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	public void onCreate() {
		Log.d(Constants.LOG_TAG_MAIN, "Create CatalogTaskQueueService");
		mQueueSizeNotificationTitle = getString(R.string.msg_queue_size_notify_title);
		mQueueSizeText = getString(R.string.msg_operation_queue_size);
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		try {
			mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			mStartForeground = mStopForeground = null;
		}
		startForegroundCompat();
	}

	public void onStart(Intent intent, int startId) {
		int taskLeft = intent.getIntExtra(EXTRA_TASK_LEFT, 0);
		createNotification(taskLeft);
		super.onStart(intent, startId);
	}
	
	public void onDestroy() {
		Log.d(Constants.LOG_TAG_MAIN, "Destroy CatalogTaskQueueService");
		stopForegroundCompat();
	}	

	public IBinder onBind(Intent intent) {
		return null;
	}

	private void createNotification(int taskLeft)
	{
		Notification notification = mNotification;
		if(notification==null){
			notification = new Notification(android.R.drawable.stat_notify_sync, null,System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT |Notification.FLAG_NO_CLEAR;
			mNotification = notification;
		}
		notification.number = taskLeft;
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TaskQueuedList.class), 0);
		notification.setLatestEventInfo(this, mQueueSizeNotificationTitle , mQueueSizeText + " " + taskLeft, contentIntent);
		mNotificationManager.notify(TASK_QUEUE_ID, notification);
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	private void startForegroundCompat() {
		createNotification(0);
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(TASK_QUEUE_ID);
			mStartForegroundArgs[1] = mNotification;
			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke startForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke startForeground", e);
			}
			return;
		}

		// Fall back on the old API.
		setForeground(true);
		mNotificationManager.notify(TASK_QUEUE_ID, mNotification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	private void stopForegroundCompat() {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(Constants.LOG_TAG_MAIN, "Unable to invoke stopForeground", e);
			}
			return;
		}
		// Fall back on the old API.  Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		mNotificationManager.cancel(TASK_QUEUE_ID);
		setForeground(false);
	}

}
