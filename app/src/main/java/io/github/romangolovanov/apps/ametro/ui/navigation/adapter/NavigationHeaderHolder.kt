package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationHeader
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationItem

internal class NavigationHeaderHolder(view: View) : IHolder {

    private val icon: ImageView = view.findViewById(R.id.icon)
    private val city: TextView = view.findViewById(R.id.city)
    private val country: TextView = view.findViewById(R.id.country)
    private val comment: TextView = view.findViewById(R.id.comment)
    private val transportsContainer: ViewGroup = view.findViewById(R.id.icons)

    override fun update(item: NavigationItem) {
        val header = item as NavigationHeader
        icon.visibility = if (header.icon == null) View.INVISIBLE else View.VISIBLE
        icon.setImageDrawable(header.icon)
        city.text = header.city
        country.text = header.country
        comment.text = header.comment
        transportsContainer.removeAllViews()
        for (drawable: Drawable? in header.getTransportTypeIcons()) {
            val img = ImageView(transportsContainer.context)
            img.setImageDrawable(drawable)
            transportsContainer.addView(img)
        }
    }
}
