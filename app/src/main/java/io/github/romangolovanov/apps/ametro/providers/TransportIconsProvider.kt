package io.github.romangolovanov.apps.ametro.providers

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.catalog.entities.TransportType

class TransportIconsProvider(context: Context) {

    private val icons: Map<TransportType, Drawable?> = createIconsMap(context)

    fun getTransportIcon(transportType: TransportType): Drawable? = icons[transportType]

    fun getTransportIcons(transportTypes: Array<TransportType>): Array<Drawable?> =
        Array(transportTypes.size) { i -> icons[transportTypes[i]] }

    companion object {
        private fun createIconsMap(context: Context): Map<TransportType, Drawable?> =
            TransportType.entries.associateWith { type ->
                ContextCompat.getDrawable(context, getTransportTypeIcon(type))
            }

        private fun getTransportTypeIcon(transportType: TransportType): Int = when (transportType) {
            TransportType.Subway -> R.drawable.icon_b_metro
            TransportType.Tram -> R.drawable.icon_b_tram
            TransportType.Bus -> R.drawable.icon_b_bus
            TransportType.Train -> R.drawable.icon_b_train
            TransportType.WaterBus -> R.drawable.icon_b_water_bus
            TransportType.TrolleyBus -> R.drawable.icon_b_trolleybus
            TransportType.CableWay -> R.drawable.icon_b_cableway
            else -> R.drawable.icon_b_unknown
        }
    }
}
