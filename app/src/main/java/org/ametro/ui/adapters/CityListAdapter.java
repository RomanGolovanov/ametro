package org.ametro.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.providers.FilteringMapGeographyProvider;
import org.ametro.providers.IconProvider;

public class CityListAdapter extends BaseExpandableListAdapter  {

    private final IconProvider countryIconProvider;
    private final FilteringMapGeographyProvider geoNamesProvider;
    private final LayoutInflater inflater;

    public CityListAdapter(Context context, FilteringMapGeographyProvider geoNamesProvider, IconProvider countryIconProvider) {
        this.geoNamesProvider = geoNamesProvider;
        this.countryIconProvider = countryIconProvider;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return geoNamesProvider.getCountries().length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return geoNamesProvider.getCountryCities(groupPosition).length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return geoNamesProvider.getCountries()[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return geoNamesProvider.getCountryCities(groupPosition)[childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return geoNamesProvider.getCountryId(
                geoNamesProvider.getCountries()[groupPosition]);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return geoNamesProvider.getCityId(
                geoNamesProvider.getCountryCities(groupPosition)[childPosition]);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        CountryViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_city_list_group_item, parent, false);
            holder = new CountryViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (CountryViewHolder) convertView.getTag();
        }
        String countryName = geoNamesProvider.getCountries()[groupPosition];
        holder.update(countryName,
                countryIconProvider.getIcon(geoNamesProvider.getCountryIsoCode(countryName)));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        CityViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.fragment_city_list_child_item, parent, false);
            holder = new CityViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (CityViewHolder) convertView.getTag();
        }
        holder.update(geoNamesProvider.getCountryCities(groupPosition)[childPosition]);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class CountryViewHolder {

        private final TextView country;
        private final ImageView icon;

        public CountryViewHolder(View view) {
            country = (TextView) view.findViewById(R.id.country);
            icon = (ImageView) view.findViewById(R.id.icon);
        }

        public void update(String countryName, Drawable countryIcon){
            country.setText(countryName);
            icon.setImageDrawable(countryIcon);
        }
    }

    private static class CityViewHolder {
        private final TextView city;

        public CityViewHolder(View view) {
            city = (TextView) view.findViewById(R.id.city);
        }

        public void update(String cityName){
            city.setText(cityName);
        }
    }
}
