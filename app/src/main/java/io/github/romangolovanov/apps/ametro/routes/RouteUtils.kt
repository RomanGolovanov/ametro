package io.github.romangolovanov.apps.ametro.routes

import android.util.Pair
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.routes.entities.MapRoute
import java.util.HashSet

object RouteUtils {

    @JvmStatic
    fun convertRouteToSchemeObjectIds(route: MapRoute, scheme: MapScheme): HashSet<Int> {
        val ids = HashSet<Int>()
        val transfers = HashSet<Pair<Int, Int>>()

        for (part in route.parts) {
            ids.add(part.from)
            ids.add(part.to)
            transfers.add(Pair(part.from, part.to))
        }

        for (line in scheme.lines) {
            for (segment in line.segments) {
                val id = Pair(segment.from, segment.to)
                val reverseId = Pair(segment.to, segment.from)
                if (transfers.contains(id) || transfers.contains(reverseId)) {
                    ids.add(segment.uid)
                }
            }
        }

        for (transfer in scheme.transfers) {
            val id = Pair(transfer.from, transfer.to)
            val reverseId = Pair(transfer.to, transfer.from)
            if (transfers.contains(id) || transfers.contains(reverseId)) {
                ids.add(transfer.uid)
            }
        }

        return ids
    }
}
