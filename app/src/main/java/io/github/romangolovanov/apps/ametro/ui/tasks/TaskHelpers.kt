package io.github.romangolovanov.apps.ametro.ui.tasks

import android.app.Activity
import android.widget.Toast
import io.github.romangolovanov.apps.ametro.R

object TaskHelpers {

    fun displayFailReason(activity: Activity, reason: Throwable) {
        if (reason is InterruptedException) {
            Toast.makeText(activity, activity.getString(R.string.msg_operation_interrupted), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(activity, activity.getString(R.string.msg_error, reason.message), Toast.LENGTH_LONG).show()
        }
    }
}
