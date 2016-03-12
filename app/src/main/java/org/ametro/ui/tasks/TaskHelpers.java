package org.ametro.ui.tasks;

import android.app.Activity;
import android.widget.Toast;

import org.ametro.R;

public class TaskHelpers {

    public static void displayFailReason(Activity activity, Throwable reason){
        if(reason instanceof InterruptedException){
            Toast.makeText(activity, activity.getString(R.string.msg_operation_interrupted), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(activity, activity.getString(R.string.msg_error, reason.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

}

