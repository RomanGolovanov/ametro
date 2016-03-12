package org.ametro.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.providers.IconProvider;
import org.ametro.providers.TransportIconsProvider;
import org.ametro.ui.loaders.ExtendedMapInfo;
import org.ametro.ui.loaders.ExtendedMapStatus;

import java.util.ArrayList;

public class MapListAdapter extends ArrayAdapter<ExtendedMapInfo> {

    private final LayoutInflater inflater;
    private final IconProvider countryFlagProvider;
    private final TransportIconsProvider transportIconsProvider;

    public MapListAdapter(Context context, IconProvider countryFlagProvider) {
        super(context, R.layout.fragment_map_list_item);
        this.countryFlagProvider = countryFlagProvider;
        this.transportIconsProvider = new TransportIconsProvider(context);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MapViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_map_list_item, parent, false);
            holder = new MapViewHolder(convertView, countryFlagProvider, transportIconsProvider);
            convertView.setTag(holder);
        } else {
            holder = (MapViewHolder) convertView.getTag();
        }
        holder.update(getItem(position));
        return convertView;
    }

    public void clearSelection() {
        for(int i=0; i<getCount();i++){
            getItem(i).setSelected(false);
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        ExtendedMapInfo item = getItem(position);
        item.setSelected(!item.isSelected());
        notifyDataSetChanged();
    }

    public MapInfo[] getSelection() {
        ArrayList<MapInfo> maps = new ArrayList<>();
        for(int i=0; i<getCount();i++){
            ExtendedMapInfo map = getItem(i);
            if(map.isSelected()){
                maps.add(new MapInfo(map));
            }
        }
        return maps.toArray(new MapInfo[maps.size()]);
    }

    private static class MapViewHolder {

        private final IconProvider countryFlagProvider;
        private final TransportIconsProvider transportIconsProvider;
        private final ImageView icon;
        private final TextView city;
        private final TextView country;
        private final ViewGroup transportsContainer;
        private final TextView comment;
        private final TextView status;
        private final View container;

        private final int defaultStatusColor;
        private final int outdatedStatusColor;

        public MapViewHolder(View view, IconProvider countryFlagProvider, TransportIconsProvider transportIconsProvider) {
            this.countryFlagProvider = countryFlagProvider;
            this.transportIconsProvider = transportIconsProvider;
            icon = (ImageView) view.findViewById(R.id.icon);
            city = (TextView) view.findViewById(R.id.city);
            country = (TextView) view.findViewById(R.id.country);
            transportsContainer = (ViewGroup) view.findViewById(R.id.icons);
            comment = (TextView) view.findViewById(R.id.comment);
            status = (TextView) view.findViewById(R.id.status);
            container = view;

            defaultStatusColor = status.getCurrentTextColor();
            outdatedStatusColor = ContextCompat.getColor(status.getContext(), R.color.accent);
        }

        public void update(ExtendedMapInfo map) {
            city.setText(map.getCity());
            country.setText(map.getCountry());
            icon.setImageDrawable(countryFlagProvider.getIcon(map.getIso()));
            transportsContainer.removeAllViews();
            for (Drawable icon : transportIconsProvider.getTransportIcons(map.getTypes())) {
                ImageView img = new ImageView(transportsContainer.getContext());
                img.setImageDrawable(icon);
                transportsContainer.addView(img);
            }
            comment.setText(""); //TODO: add comments
            status.setText(container.getResources().getStringArray(R.array.map_states)[map.getStatus().ordinal()]);
            status.setTextColor(map.getStatus() == ExtendedMapStatus.Outdated ? outdatedStatusColor : defaultStatusColor);

            container.setSelected(map.isSelected());
        }
    }
}
