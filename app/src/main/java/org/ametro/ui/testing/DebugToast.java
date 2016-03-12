package org.ametro.ui.testing;

import android.content.Context;
import android.widget.Toast;

import org.ametro.BuildConfig;

public class DebugToast {

    public static void show(Context context, String message, int toastLength){
        if(BuildConfig.DEBUG) {
            Toast.makeText(context, message, toastLength).show();
        }
    }
}
