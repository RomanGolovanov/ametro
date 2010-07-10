package org.ametro.catalog.storage.tasks;

public interface ICatalogStorageTaskListener {

	boolean isTaskCanceled(BaseTask task);
	void onTaskUpdated(BaseTask task, long progress, long total, String message);
	void onTaskCanceled(BaseTask task);
	void onTaskFailed(BaseTask task, Throwable reason);
	void onTaskBegin(BaseTask task);
	void onTaskDone(BaseTask task);
}
