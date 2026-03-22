package io.github.romangolovanov.apps.ametro.ui.testing

import android.content.Context
import android.widget.Toast

object DebugToast {

    fun show(context: Context, message: String, toastLength: Int) {
        Toast.makeText(context, message, toastLength).show()
    }
}
