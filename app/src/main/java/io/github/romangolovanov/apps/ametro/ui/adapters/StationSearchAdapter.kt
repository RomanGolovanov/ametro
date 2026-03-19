package io.github.romangolovanov.apps.ametro.ui.adapters

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation

class StationSearchAdapter protected constructor(
    context: Context,
    cursor: Cursor,
    private val stations: List<StationInfo>
) : CursorAdapter(context, cursor, false) {

    companion object {
        fun createFromMapScheme(context: Context, scheme: MapScheme, query: String): StationSearchAdapter {
            val items = getStations(scheme, query)
            return StationSearchAdapter(context, createStationNameCursor(items), items)
        }

        private fun getStations(scheme: MapScheme, query: String): List<StationInfo> {
            val stationNamesStartsWith = mutableListOf<StationInfo>()
            val stationNamesContains = mutableListOf<StationInfo>()
            for (line in scheme.lines) {
                for (station in line.stations) {
                    if (station.position == null) continue

                    val queryLowerCase = query.lowercase()
                    val allNames = station.getAllDisplayNames() ?: emptyList()

                    if (allNames.any { it.lowercase().startsWith(queryLowerCase) }) {
                        stationNamesStartsWith.add(StationInfo(station, line))
                        stationNamesContains.add(StationInfo(station, line))
                        continue
                    }

                    if (allNames.any { it.lowercase().contains(queryLowerCase) }) {
                        stationNamesContains.add(StationInfo(station, line))
                    }
                }
            }
            return if (stationNamesStartsWith.isNotEmpty()) stationNamesStartsWith else stationNamesContains
        }

        private fun createStationNameCursor(items: List<StationInfo>): MatrixCursor {
            val temp: Array<Any?> = arrayOf(0, "default")
            val cursor = MatrixCursor(arrayOf("_id", "text"))
            for (item in items) {
                val station = item.station
                temp[0] = station.uid
                temp[1] = station.displayName ?: ""
                cursor.addRow(temp)
            }
            return cursor
        }
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        (view.tag as StationInfoHolder).update(stations[cursor.position])
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.station_search_view_list_item, parent, false)
        v.tag = StationInfoHolder(v)
        return v
    }

    fun getStation(position: Int): MapSchemeStation = stations[position].station

    protected class StationInfo(val station: MapSchemeStation, val line: MapSchemeLine)

    private class StationInfoHolder(v: View) {
        private val stationView: TextView = v.findViewById(R.id.station)
        private val lineView: TextView = v.findViewById(R.id.line)
        private val iconView: ImageView = v.findViewById(R.id.icon)

        fun update(station: StationInfo) {
            stationView.text = station.station.displayName
            lineView.text = station.line.displayName
            val drawable = iconView.background as GradientDrawable
            drawable.setColor(station.line.lineColor)
        }
    }
}
