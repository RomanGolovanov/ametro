package org.ametro.ng.ui.testing;

import android.content.Context;
import android.widget.Toast;


public class DebugToast {

    public static void show(Context context, String message, int toastLength) {
        Toast.makeText(context, message, toastLength).show();
    }
}
