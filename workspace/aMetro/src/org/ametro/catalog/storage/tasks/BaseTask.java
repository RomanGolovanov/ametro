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

	public void execute(Context context, ICatalogStorageTaskListener callback) {
		mCallback = callback;
		try {
			begin();
			run(context);
			done();
		}catch(CanceledException ex){
			canceled();
		} catch (Throwable reason) {
			failed(reason);
		}
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
		return mCallback.isTaskCanceled(this);
	}
	
	protected void cancelCheck() throws CanceledException{
		if(mCallback.isTaskCanceled(this)){
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
}
