package org.ametro.util;

import java.io.File;

public class BaseOperationListener implements IOperationListener {

	public void onBegin(Object context, File file) {
	}

	public void onCanceled(Object context, File file) {
	}

	public void onDone(Object context, File file) throws Exception {
	}

	public void onFailed(Object context, File file, Throwable reason) {
	}

	public boolean onUpdate(Object context, long position, long total) {
		return true;
	}

}
