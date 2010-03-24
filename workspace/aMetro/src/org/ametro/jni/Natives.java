package org.ametro.jni;

import org.ametro.Constants;
import org.ametro.render.RenderElement;

import android.graphics.RectF;
import android.util.Log;

public class Natives {
	
	public static final boolean INITIALIZED;
	public static boolean REQUESTED;
	
	static {
		boolean ini;
		try {
			System.loadLibrary("ametro");
			ini = true;
    		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.INFO)){
    			Log.i(Constants.LOG_TAG_MAIN,"Native library initialized");
    		}
		} catch (Throwable t) {
			ini = false;
    		if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.WARN)){
    			Log.w(Constants.LOG_TAG_MAIN, "Native library loading error", t);
    		}
		}
		INITIALIZED = ini;
	}   
	 
	public static native String[] SplitCsvString(String src, char separator);
	public static native RenderElement[] getVisibleRenderElements(RenderElement[] elements, RectF[] views, int filter);
	
	public static void Initialize() { // this is a fake method
		REQUESTED = true;
	}
}
