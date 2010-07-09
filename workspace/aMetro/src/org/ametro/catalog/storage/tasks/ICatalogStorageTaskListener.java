package org.ametro.catalog.storage.tasks;

public interface ICatalogStorageTaskListener {

	boolean isTaskCanceled(CatalogStorageTask task);
	void onTaskUpdated(CatalogStorageTask task, long progress, long total, String message);
	void onTaskCanceled(CatalogStorageTask task);
	void onTaskFailed(CatalogStorageTask task, Throwable reason);
	void onTaskBegin(CatalogStorageTask task);
	void onTaskDone(CatalogStorageTask task);
}
