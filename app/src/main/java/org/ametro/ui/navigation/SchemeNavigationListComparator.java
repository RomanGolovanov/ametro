package org.ametro.ui.navigation;

import org.ametro.model.entities.MapMetadata;

import java.util.Comparator;

public class SchemeNavigationListComparator implements Comparator<MapMetadata.Scheme> {
    @Override
    public int compare(MapMetadata.Scheme lhs, MapMetadata.Scheme rhs) {
        int byType = getType(lhs).compareTo(getType(rhs));
        if(byType != 0){
            return byType;
        }
        return getDisplayName(lhs).compareTo(getDisplayName(rhs));
    }

    private String getType(MapMetadata.Scheme scheme){
        String type = scheme.getTypeName().equals("ROOT") ? scheme.getDisplayName() : scheme.getTypeDisplayName();
        if(scheme.getName().equals("metro") || scheme.getTypeName().equals("Метро")){
            return "";
        }
        if(type.equals("OTHER")){
            return ""+Character.MAX_VALUE;
        }
        return type;
    }

    private String getDisplayName(MapMetadata.Scheme scheme){
        return scheme.getTypeName().equals("ROOT") ? "" : scheme.getDisplayName();
    }

}
