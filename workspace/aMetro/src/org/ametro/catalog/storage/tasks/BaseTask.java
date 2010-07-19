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
package org.ametro.catalog.storage.tasks;

import android.content.Context;
import android.os.Parcelable;

public abstract class BaseTask implements Parcelable {

	public static class CanceledException extends Exception {
		public CanceledException(){
			super();
		}
		
		public CanceledException(String message) {
			super(message);
		}

		private static final long serialVersionUID = -6925970064795146727L;
	}
	
	public abstract boolean isAsync();
	public abstract Object getTaskId();
	
	private ICatalogStorageTaskListener mCallback;
	protected boolean mIsCanceled;
	protected boolean mIsDone;
	protected boolean mIsRunning;


	public void execute(Context context, ICatalogStorageTaskListener callback) {
		mIsRunning = true;
		mIsCanceled = false;
		mIsDone = false;
		mCallback = callback;
		try {
			begin();
			run(context);
			mIsDone = true;
			done();
		}catch(CanceledException ex){
			canceled();
		} catch (Throwable reason) {
			failed(reason);
		}
		mIsRunning = false;
	}

	protected void begin() {
		mCallback.onTaskBegin(this);
	}

	protected void done() {
		mCallback.onTaskDone(this);
	}

	protected void canceled() {
		mCallback.onTaskCanceled(this);
	}

	protected boolean isCanceled() {
		return mIsCanceled || mCallback.isTaskCanceled(this);
	}
	
	protected void cancelCheck() throws CanceledException{
		if(mIsCanceled || mCallback.isTaskCanceled(this)){
			throw new CanceledException();
		}
	}

	protected void failed(Throwable reason) {
		mCallback.onTaskFailed(this, reason);
	}

	protected abstract void run(Context context) throws Exception;

	protected void update(long progress, long total, String message) {
		mCallback.onTaskUpdated(this, progress, total, message);
	}

	public String toString() {
		return "[ID:" + getTaskId() + ";CLASS=" + getClass().getName() + "]";
	}
	
	public void abort() {
		mIsCanceled = true;
	}
	
	public boolean isDone(){
		return mIsDone;
	}
}
