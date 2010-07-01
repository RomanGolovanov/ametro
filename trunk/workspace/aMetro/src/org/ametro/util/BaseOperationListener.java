package org.ametro.util;

import java.io.File;

public class BaseOperationListener implements IOperationListener {

	public void onBegin(Object context) {
	}

	public void onCanceled(Object context) {
	}

	public void onDone(Object context, File file) {
	}

	public void onFailed(Object context, Throwable reason) {
	}

	public boolean onUpdate(Object context, long position, long total) {
		return true;
	}

}
