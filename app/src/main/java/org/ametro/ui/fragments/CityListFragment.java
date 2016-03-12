package org.ametro.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.providers.FilteringMapGeographyProvider;
import org.ametro.ui.adapters.CityListAdapter;
import org.ametro.utils.ListUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CityListFragment extends Fragment implements ExpandableListView.OnChildClickListener,
        LoaderManager.LoaderCallbacks<MapInfo[]>, SearchView.OnQueryTextListener {

    private ICitySelectionListener citySelectionListener = new ICitySelectionListener() {
        @Override
        public void onCitySelected(MapInfo[] maps) {
        }
    };
    private MapInfo[] maps;
    private ProgressBar progressBar;
    private TextView progressText;
    private ExpandableListView list;
    private CityListAdapter adapter;
    private View noConnectionView;
    private View emptyView;
    private FilteringMapGeographyProvider geographyProvider;
    private String country;
    private String city;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_city_list_view, container, false);

        if (savedInstanceState != null) {
            country = savedInstanceState.getString(Constants.MAP_COUNTRY);
            city = savedInstanceState.getString(Constants.MAP_CITY);
        }
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressText = (TextView) view.findViewById(R.id.progressText);

        noConnectionView = view.findViewById(R.id.no_connection);
        emptyView = view.findViewById(R.id.empty);

        list = (ExpandableListView) view.findViewById(R.id.list);
        list.setOnChildClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.MAP_CITY, country);
        outState.putString(Constants.MAP_CITY, city);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        country = (String) adapter.getGroup(groupPosition);
        city = (String) adapter.getChild(groupPosition, childPosition);

        List<MapInfo> result = new ArrayList<>();
        for (MapInfo map : this.maps) {
            if (map.getCity().equals(city) && map.getCountry().equals(country)) {
                result.add(map);
            }
        }

        citySelectionListener.onCitySelected(result.toArray(new MapInfo[result.size()]));
        return true;
    }

    public void setCitySelectionListener(ICitySelectionListener newListener) {
        citySelectionListener = newListener;
    }

    @Override
    public Loader<MapInfo[]> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<MapInfo[]>(getActivity()) {
            @Override
            public MapInfo[] loadInBackground() {
                ApplicationEx app = ApplicationEx.getInstance(this);
                final Set<String> loadedMaps = new HashSet<>();
                for (MapInfo m : app.getLocalMapCatalogManager().getMapCatalog().getMaps()) {
                    loadedMaps.add(m.getFileName());
                }
                MapCatalog remoteMapCatalog = app.getRemoteMapCatalogProvider()
                        .getMapCatalog(false);

                if(remoteMapCatalog == null){
                    return null;
                }

                Collection<MapInfo> remoteMaps =
                        ListUtils.filter(Arrays.asList(remoteMapCatalog.getMaps()),
                                new ListUtils.IPredicate<MapInfo>() {
                                    @Override
                                    public boolean apply(MapInfo map) {
                                        return !loadedMaps.contains(map.getFileName());
                                    }
                                });
                return remoteMaps.toArray(new MapInfo[remoteMaps.size()]);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<MapInfo[]> loader, MapInfo[] data) {
        if (data != null && data.length > 0) {
            setListShown();
            list.setEmptyView(emptyView);
            maps = data;
            geographyProvider = new FilteringMapGeographyProvider(data);
            adapter = new CityListAdapter(
                    getActivity(),
                    geographyProvider,
                    ApplicationEx.getInstance(getActivity()).getCountryFlagProvider());
            list.setAdapter(adapter);
        } else {
            setNoConnectionShown();
        }
    }

    @Override
    public void onLoaderReset(Loader<MapInfo[]> loader) {
        if(geographyProvider!=null) {
            geographyProvider.setData(new MapInfo[0]);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if(adapter == null){
            return true;
        }

        if (s != null && s.length() > 0)
            geographyProvider.setFilter(s);
        else
            geographyProvider.setFilter(null);
        adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(adapter == null){
            return true;
        }

        if (s != null && s.length() > 0)
            geographyProvider.setFilter(s);
        else
            geographyProvider.setFilter(null);
        adapter.notifyDataSetChanged();
        return false;
    }

    private void setNoConnectionShown() {
        progressText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        noConnectionView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
    }

    private void setListShown() {
        progressText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        noConnectionView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        list.setVisibility(View.VISIBLE);
    }

    public interface ICitySelectionListener {
        void onCitySelected(MapInfo[] maps);
    }


}
