package org.ametro.ui.navigation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.ametro.ui.navigation.entities.INavigationItemGroup;
import org.ametro.ui.navigation.entities.NavigationCheckBoxItem;
import org.ametro.ui.navigation.entities.NavigationHeader;
import org.ametro.ui.navigation.entities.NavigationItem;
import org.ametro.ui.navigation.entities.NavigationSplitter;
import org.ametro.ui.navigation.entities.NavigationSubHeader;
import org.ametro.ui.navigation.entities.NavigationTextItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationDrawerAdapter extends BaseAdapter {

    private final List<NavigationItem> items;
    private final LayoutInflater inflater;

    private final Map<Class, Integer> viewItemTypes = new HashMap<>();
    private final Map<Class, IHolderFactory> viewItemHolderFactories = new HashMap<>();


    public NavigationDrawerAdapter(Context context, NavigationItem[] items) {

        viewItemTypes.put(NavigationHeader.class, 0);
        viewItemTypes.put(NavigationTextItem.class, 1);
        viewItemTypes.put(NavigationSplitter.class, 2);
        viewItemTypes.put(NavigationSubHeader.class, 3);
        viewItemTypes.put(NavigationCheckBoxItem.class, 4);

        viewItemHolderFactories.put(NavigationHeader.class, new NavigationHeaderHolderFactory());
        viewItemHolderFactories.put(NavigationTextItem.class, new NavigationTextItemHolderFactory());
        viewItemHolderFactories.put(NavigationSplitter.class, new NavigationSplitterHolderFactory());
        viewItemHolderFactories.put(NavigationSubHeader.class, new NavigationSubHeaderHolderFactory());
        viewItemHolderFactories.put(NavigationCheckBoxItem.class, new NavigationCheckBoxItemHolderFactory());

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = flattenItems(items);
    }

    public void setNavigationItems(NavigationItem[] items) {
        this.items.clear();
        this.items.addAll(flattenItems(items));
        this.notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return items.get(position).isEnabled();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public NavigationItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IHolder holder;
        if (convertView == null) {
            IHolderFactory factory = viewItemHolderFactories.get(items.get(position).getClass());
            convertView = factory.createView(inflater, parent);
            holder = factory.createHolder(convertView);
        } else {
            holder = (IHolder) convertView.getTag();
        }
        holder.update(items.get(position));
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return viewItemTypes.get(items.get(position).getClass());
    }

    @Override
    public int getViewTypeCount() {
        return viewItemTypes.size();
    }

    @Override
    public boolean isEmpty() {
        return items.size() == 0;
    }

    private List<NavigationItem> flattenItems(NavigationItem[] items) {
        List<NavigationItem> flattenList = new ArrayList<>();
        for (NavigationItem item : items) {
            flattenList.add(item);
            if (item instanceof INavigationItemGroup) {
                INavigationItemGroup group = (INavigationItemGroup) item;
                flattenList.addAll(flattenItems(group.getItems()));
            }
        }
        return flattenList;
    }
}



