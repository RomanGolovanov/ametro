package io.github.romangolovanov.apps.ametro.routes

import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.routes.entities.MapRoute

object RouteUtils {

    fun convertRouteToSchemeObjectIds(route: MapRoute, scheme: MapScheme): Set<Int> {
        val ids = mutableSetOf<Int>()
        val transfers = mutableSetOf<Pair<Int, Int>>()

        for (part in route.parts) {
            ids.add(part.from)
            ids.add(part.to)
            transfers.add(part.from to part.to)
        }

        for (line in scheme.lines) {
            for (segment in line.segments) {
                val id = segment.from to segment.to
                val reverseId = segment.to to segment.from
                if (transfers.contains(id) || transfers.contains(reverseId)) {
                    ids.add(segment.uid)
                }
            }
        }

        for (transfer in scheme.transfers) {
            val id = transfer.from to transfer.to
            val reverseId = transfer.to to transfer.from
            if (transfers.contains(id) || transfers.contains(reverseId)) {
                ids.add(transfer.uid)
            }
        }

        return ids
    }
}
