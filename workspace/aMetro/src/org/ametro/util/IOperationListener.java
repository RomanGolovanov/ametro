package org.ametro.util;

import java.io.File;

public interface IOperationListener {
	void onBegin(Object context, File file);
	boolean onUpdate(Object context, long position, long total);
	void onDone(Object context, File file) throws Exception;
	void onFailed(Object context, File file, Throwable reason);
	void onCanceled(Object context, File file);
}
