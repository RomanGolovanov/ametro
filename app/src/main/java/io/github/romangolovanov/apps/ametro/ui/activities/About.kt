package io.github.romangolovanov.apps.ametro.ui.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.utils.FileUtils
import java.io.IOException

class About : AppCompatActivity() {

    private var scrollView: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_view)

        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val aboutTextView: TextView = findViewById(R.id.text)
        try {
            aboutTextView.text = getAboutContent()
            aboutTextView.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        scrollView = findViewById(R.id.scrollView)
        if (savedInstanceState != null) {
            val y = savedInstanceState.getInt("scrollY", 0)
            scrollView!!.post { scrollView!!.scrollTo(0, y) }
        }
    }

    override fun onSaveInstanceState(@NonNull outState: Bundle) {
        super.onSaveInstanceState(outState)
        scrollView?.let { outState.putInt("scrollY", it.scrollY) }
    }

    @Throws(IOException::class)
    protected fun getAboutContent(): SpannableString {
        val builder = SpannableStringBuilder()
        builder.append(Html.fromHtml(
            String.format("<p>%s %s</p>", getString(R.string.app_name), getVersionNumber()),
            Html.FROM_HTML_MODE_LEGACY
        ))
        builder.append(Html.fromHtml(
            FileUtils.readAllText(resources.openRawResource(R.raw.about)),
            Html.FROM_HTML_MODE_LEGACY
        ))
        Linkify.addLinks(builder, Linkify.ALL)
        return SpannableString(builder)
    }

    private fun getVersionNumber(): String {
        val manager = packageManager
        return try {
            val info = manager.getPackageInfo(packageName, 0)
            info.versionName ?: "0.0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0.0.0"
        }
    }
}
