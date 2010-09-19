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
	private Throwable mFailReason;
	private long mBeginTimestamp;
	private long mEndTimestamp;
	private Context mContext;
	
	protected boolean mIsCanceled;
	protected boolean mIsDone;
	protected boolean mIsRunning;

	
	public Throwable getFailReason() {
		return mFailReason;
	}
	public long getBeginTimestamp() {
		return mBeginTimestamp;
	}
	public long getEndTimestamp() {
		return mEndTimestamp;
	}
	
	protected Context getContext(){
		return mContext;
	}
	
	public void execute(Context context, ICatalogStorageTaskListener callback) {
		mContext = context;
		mBeginTimestamp = System.currentTimeMillis();
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
			mFailReason = reason;
			failed(reason);
		}
		mEndTimestamp = System.currentTimeMillis();
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
