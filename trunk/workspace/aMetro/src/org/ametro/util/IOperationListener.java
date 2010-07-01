package org.ametro.util;

import java.io.File;

public interface IOperationListener {
	void onBegin(Object context);
	boolean onUpdate(Object context, long position, long total);
	void onDone(Object context, File file);
	void onFailed(Object context, Throwable reason);
	void onCanceled(Object context);
}
