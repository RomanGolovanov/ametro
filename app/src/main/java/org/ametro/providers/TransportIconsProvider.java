package org.ametro.providers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import org.ametro.R;
import org.ametro.catalog.entities.TransportType;

import java.util.HashMap;
import java.util.Map;

import static org.ametro.catalog.entities.TransportType.Bus;
import static org.ametro.catalog.entities.TransportType.CableWay;
import static org.ametro.catalog.entities.TransportType.Subway;
import static org.ametro.catalog.entities.TransportType.Train;
import static org.ametro.catalog.entities.TransportType.Tram;
import static org.ametro.catalog.entities.TransportType.TrolleyBus;
import static org.ametro.catalog.entities.TransportType.Unknown;
import static org.ametro.catalog.entities.TransportType.WaterBus;

public class TransportIconsProvider {

    private final Map<TransportType, Drawable> icons;

    public TransportIconsProvider(Context context){
        icons = createIconsMap(context);
    }

    public Drawable getTransportIcon(TransportType transportType)
    {
        return icons.get(transportType);
    }

    public Drawable[] getTransportIcons(TransportType[] transportTypes)
    {
        Drawable[] result = new Drawable[transportTypes.length];
        for(int i=0;i<transportTypes.length;i++){
            result[i] = icons.get(transportTypes[i]);
        }
        return result;
    }

    private static HashMap<TransportType, Drawable> createIconsMap(Context context){
        HashMap<TransportType, Drawable> iconsMap = new HashMap<>();

        iconsMap.put(Unknown, ContextCompat.getDrawable(context, getTransportTypeIcon(Unknown)));
        iconsMap.put(Bus, ContextCompat.getDrawable(context,getTransportTypeIcon(Bus))  );
        iconsMap.put(CableWay, ContextCompat.getDrawable(context,getTransportTypeIcon(CableWay))  );
        iconsMap.put(Subway, ContextCompat.getDrawable(context,getTransportTypeIcon(Subway))  );
        iconsMap.put(Train, ContextCompat.getDrawable(context,getTransportTypeIcon(Train))  );
        iconsMap.put(Tram, ContextCompat.getDrawable(context,getTransportTypeIcon(Tram))  );
        iconsMap.put(TrolleyBus, ContextCompat.getDrawable(context,getTransportTypeIcon(TrolleyBus))  );
        iconsMap.put(WaterBus, ContextCompat.getDrawable(context,getTransportTypeIcon(WaterBus))  );
        return iconsMap;
    }


    private static int getTransportTypeIcon(TransportType transportType){
        switch(transportType){
            case Subway : return R.drawable.icon_b_metro;
            case Tram : return R.drawable.icon_b_tram;
            case Bus : return R.drawable.icon_b_bus;
            case Train : return R.drawable.icon_b_train;
            case WaterBus : return R.drawable.icon_b_water_bus;
            case TrolleyBus : return R.drawable.icon_b_trolleybus;
            case CableWay : return R.drawable.icon_b_cableway;
            default: return R.drawable.icon_b_unknown;
        }
    }

}
