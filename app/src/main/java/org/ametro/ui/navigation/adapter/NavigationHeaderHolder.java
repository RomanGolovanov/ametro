package org.ametro.ui.navigation.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.ui.navigation.entities.NavigationItem;
import org.ametro.ui.navigation.entities.NavigationHeader;

class NavigationHeaderHolder implements IHolder
{
    private final ImageView icon;
    private final TextView city;
    private final TextView country;
    private final TextView comment;
    private final ViewGroup transportsContainer;


    public NavigationHeaderHolder(View view){
        icon = (ImageView)view.findViewById(R.id.icon);
        city = (TextView)view.findViewById(R.id.city);
        country = (TextView)view.findViewById(R.id.country);
        comment = (TextView)view.findViewById(R.id.comment);
        transportsContainer = (ViewGroup)view.findViewById(R.id.icons);
    }

    @Override
    public void update(NavigationItem item) {
        NavigationHeader header = (NavigationHeader)item;
        icon.setVisibility(header.getIcon()==null ? View.INVISIBLE : View.VISIBLE);
        icon.setImageDrawable(header.getIcon());
        city.setText(header.getCity());
        country.setText(header.getCountry());
        comment.setText(header.getComment());
        transportsContainer.removeAllViews();
        for(Drawable icon : header.getTransportTypeIcons()){
            ImageView img = new ImageView(transportsContainer.getContext());
            img.setImageDrawable(icon);
            transportsContainer.addView(img);
        }

    }
}
