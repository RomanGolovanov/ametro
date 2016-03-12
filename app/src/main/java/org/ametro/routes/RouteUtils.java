package org.ametro.routes;

import android.support.v4.util.Pair;

import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeSegment;
import org.ametro.model.entities.MapSchemeTransfer;
import org.ametro.routes.entities.MapRoute;
import org.ametro.routes.entities.MapRoutePart;

import java.util.HashSet;

public class RouteUtils {

    public static HashSet<Integer> convertRouteToSchemeObjectIds(final MapRoute route, final MapScheme scheme){
        HashSet<Integer> ids = new HashSet<>();

        HashSet<Pair<Integer,Integer>> transfers = new HashSet<>();
        for(MapRoutePart part: route.getParts()){
            ids.add(part.getFrom());
            ids.add(part.getTo());
            transfers.add(new Pair<>(part.getFrom(), part.getTo()));
        }

        for(MapSchemeLine line: scheme.getLines()){
            for(MapSchemeSegment segment: line.getSegments()){
                Pair<Integer,Integer> id = new Pair<>(segment.getFrom(), segment.getTo());
                Pair<Integer,Integer> reverseId = new Pair<>(segment.getTo(), segment.getFrom());
                if(transfers.contains(id) || transfers.contains(reverseId)){
                    ids.add(segment.getUid());
                }
            }
        }

        for(MapSchemeTransfer transfer: scheme.getTransfers()){
            Pair<Integer,Integer> id = new Pair<>(transfer.getFrom(), transfer.getTo());
            Pair<Integer,Integer> reverseId = new Pair<>(transfer.getTo(), transfer.getFrom());
            if(transfers.contains(id) || transfers.contains(reverseId)){
                ids.add(transfer.getUid());
            }
        }

        return ids;
    }

}
