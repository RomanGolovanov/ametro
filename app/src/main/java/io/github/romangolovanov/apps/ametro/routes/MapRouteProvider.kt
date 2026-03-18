package io.github.romangolovanov.apps.ametro.routes

import io.github.romangolovanov.apps.ametro.routes.algorithms.DijkstraHeap
import io.github.romangolovanov.apps.ametro.routes.entities.MapRoute
import io.github.romangolovanov.apps.ametro.routes.entities.MapRoutePart
import io.github.romangolovanov.apps.ametro.routes.entities.MapRouteQueryParameters
import java.util.Collections

object MapRouteProvider {

    @JvmStatic
    fun findRoutes(parameters: MapRouteQueryParameters): Array<MapRoute> {
        val graph = DijkstraHeap.TransportGraph(parameters.stationCount)
        createGraphEdges(graph, parameters)
        val result = DijkstraHeap.dijkstra(graph, parameters.beginStationUid)

        if (result.predecessors[parameters.endStationUid] == DijkstraHeap.NO_WAY) {
            return emptyArray()
        }
        return arrayOf(convertToMapRoute(result, parameters.endStationUid))
    }

    private fun convertToMapRoute(result: DijkstraHeap.Result, endStationUid: Int): MapRoute {
        val distances = result.distances
        val predecessors = result.predecessors

        val parts = mutableListOf<MapRoutePart>()
        var to = endStationUid
        var from = predecessors[to]

        while (from != DijkstraHeap.NO_WAY) {
            parts.add(MapRoutePart(from, to, distances[to] - distances[from]))
            to = from
            from = predecessors[to]
        }

        Collections.reverse(parts)
        return MapRoute(parts.toTypedArray())
    }

    private fun createGraphEdges(graph: DijkstraHeap.TransportGraph, parameters: MapRouteQueryParameters) {
        val stationLines = arrayOfNulls<io.github.romangolovanov.apps.ametro.model.entities.MapTransportLine>(parameters.stationCount)

        for (scheme in parameters.getEnabledTransportsSchemes()) {
            for (line in scheme.lines) {
                for (segment in line.segments) {
                    if (segment.delay == 0) continue
                    graph.addEdge(segment.from, segment.to, segment.delay)
                    stationLines[segment.from] = line
                    stationLines[segment.to] = line
                }
            }
            val delayIndex = parameters.delayIndex
            for (transfer in scheme.transfers) {
                var delay = transfer.delay
                if (delayIndex != null && stationLines[transfer.to] != null) {
                    val lineDelays = stationLines[transfer.to]!!.delays
                    if (lineDelays.isNotEmpty()) {
                        delay += lineDelays[delayIndex]
                    }
                }
                graph.addEdge(transfer.from, transfer.to, delay)
                graph.addEdge(transfer.to, transfer.from, delay)
            }
        }
    }
}
