package org.ametro.routes;

import org.ametro.model.entities.MapTransportLine;
import org.ametro.model.entities.MapTransportScheme;
import org.ametro.model.entities.MapTransportSegment;
import org.ametro.model.entities.MapTransportTransfer;
import org.ametro.routes.algorithms.DijkstraHeap;
import org.ametro.routes.entities.MapRoute;
import org.ametro.routes.entities.MapRoutePart;
import org.ametro.routes.entities.MapRouteQueryParameters;

import java.util.ArrayList;
import java.util.Collections;

public class MapRouteProvider {

    public static MapRoute[] findRoutes(MapRouteQueryParameters parameters) {

        DijkstraHeap.TransportGraph graph = new DijkstraHeap.TransportGraph(parameters.getStationCount());

        CreateGraphEdges(graph, parameters);

        DijkstraHeap.Result result = DijkstraHeap.dijkstra(graph, parameters.getBeginStationUid());

        if(result.getPredecessors()[parameters.getEndStationUid()] == DijkstraHeap.NO_WAY){
            return new MapRoute[0];
        }

        return new MapRoute[]{ ConvertToMapRoute(result, parameters.getEndStationUid()) };
    }

    private static MapRoute ConvertToMapRoute(DijkstraHeap.Result result, int endStationUid) {
        final long[] distances = result.getDistances();
        final int[] predecessors = result.getPredecessors();

        ArrayList<MapRoutePart> parts = new ArrayList<>();
        int _to = endStationUid;
        int _from = predecessors[_to];

        while (_from != DijkstraHeap.NO_WAY) {
            parts.add(new MapRoutePart(_from, _to, distances[_to] - distances[_from]));
            _to = _from;
            _from = predecessors[_to];
        }

        Collections.reverse(parts);

        return new MapRoute(parts.toArray(new MapRoutePart[parts.size()]));
    }

    private static void CreateGraphEdges(DijkstraHeap.TransportGraph graph, MapRouteQueryParameters parameters) {
        MapTransportLine[] stationLines = new MapTransportLine[parameters.getStationCount()];
        for (MapTransportScheme scheme : parameters.getEnabledTransportsSchemes()) {
            for (MapTransportLine line : scheme.getLines()) {
                for (MapTransportSegment segment : line.getSegments()) {
                    if (segment.getDelay() == 0) {
                        continue;
                    }
                    graph.addEdge(segment.getFrom(), segment.getTo(), segment.getDelay());
                    stationLines[segment.getFrom()] = line;
                    stationLines[segment.getTo()] = line;
                }
            }
            final Integer delayIndex = parameters.getDelayIndex();
            for (MapTransportTransfer transfer : scheme.getTransfers()) {

                int delay = transfer.getDelay() ;
                if(delayIndex!=null && stationLines[transfer.getTo()]!=null){

                    final int[] lineDelays = stationLines[transfer.getTo()].getDelays();
                    if(lineDelays.length!=0) {
                        delay += stationLines[transfer.getTo()].getDelays()[delayIndex];
                    }
                }

                graph.addEdge(transfer.getFrom(), transfer.getTo(), delay);
                graph.addEdge(transfer.getTo(), transfer.getFrom(), delay);
            }
        }
    }
}
