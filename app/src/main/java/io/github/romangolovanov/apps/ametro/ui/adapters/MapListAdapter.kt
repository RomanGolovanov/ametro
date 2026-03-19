package io.github.romangolovanov.apps.ametro.ui.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo
import io.github.romangolovanov.apps.ametro.providers.IconProvider
import io.github.romangolovanov.apps.ametro.providers.TransportIconsProvider
import io.github.romangolovanov.apps.ametro.ui.loaders.ExtendedMapInfo
import io.github.romangolovanov.apps.ametro.ui.loaders.ExtendedMapStatus

class MapListAdapter(context: Context, countryFlagProvider: IconProvider) :
    ArrayAdapter<ExtendedMapInfo>(context, R.layout.fragment_map_list_item) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val countryFlagProvider: IconProvider = countryFlagProvider
    private val transportIconsProvider: TransportIconsProvider = TransportIconsProvider(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: MapViewHolder
        val view: View
        if (convertView == null) {
            view = inflater.inflate(R.layout.fragment_map_list_item, parent, false)
            holder = MapViewHolder(view, countryFlagProvider, transportIconsProvider)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as MapViewHolder
        }
        holder.update(getItem(position)!!)
        return view
    }

    fun clearSelection() {
        for (i in 0 until count) {
            getItem(i)!!.selected = false
        }
        notifyDataSetChanged()
    }

    fun toggleSelection(position: Int) {
        val item = getItem(position)!!
        item.selected = !item.selected
        notifyDataSetChanged()
    }

    fun getSelection(): Array<MapInfo> {
        val maps = mutableListOf<MapInfo>()
        for (i in 0 until count) {
            val map = getItem(i)!!
            if (map.selected) {
                maps.add(MapInfo(map))
            }
        }
        return maps.toTypedArray()
    }

    private class MapViewHolder(
        view: View,
        private val countryFlagProvider: IconProvider,
        private val transportIconsProvider: TransportIconsProvider
    ) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val city: TextView = view.findViewById(R.id.city)
        private val country: TextView = view.findViewById(R.id.country)
        private val transportsContainer: ViewGroup = view.findViewById(R.id.icons)
        private val comment: TextView = view.findViewById(R.id.comment)
        private val status: TextView = view.findViewById(R.id.status)
        private val container: View = view

        private val defaultStatusColor: Int = status.currentTextColor
        private val outdatedStatusColor: Int = ContextCompat.getColor(status.context, R.color.accent)

        fun update(map: ExtendedMapInfo) {
            city.text = map.city
            country.text = map.country
            icon.setImageDrawable(countryFlagProvider.getIcon(map.iso))
            transportsContainer.removeAllViews()
            for (drawable: Drawable? in transportIconsProvider.getTransportIcons(map.types)) {
                val img = ImageView(transportsContainer.context)
                img.setImageDrawable(drawable)
                transportsContainer.addView(img)
            }
            comment.text = "" //TODO: add comments
            status.text = container.resources.getStringArray(R.array.map_states)[map.status.ordinal]
            status.setTextColor(if (map.status == ExtendedMapStatus.Outdated) outdatedStatusColor else defaultStatusColor)
            container.isSelected = map.selected
        }
    }
}
