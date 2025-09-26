package org.ametro.ng.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.widget.SearchView;

import org.ametro.ng.R;
import org.ametro.ng.app.ApplicationEx;
import org.ametro.ng.catalog.entities.MapCatalog;
import org.ametro.ng.catalog.entities.MapInfo;
import org.ametro.ng.ui.adapters.MapListAdapter;
import org.ametro.ng.ui.loaders.ExtendedMapInfo;
import org.ametro.ng.ui.loaders.ExtendedMapStatus;

public class MapListFragment extends Fragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        LoaderManager.LoaderCallbacks<MapCatalog>,
        AdapterView.OnItemClickListener,
        AbsListView.MultiChoiceModeListener,
        View.OnClickListener {

    private MapListAdapter adapter;
    private ProgressBar progressBar;
    private TextView progressText;
    private View noMapsView;
    private View emptyView;

    private String filterValue;

    private MapCatalog localMapCatalog;
    private ListView list;
    private static final int LOCAL_CATALOG_LOADER = 1;

    private IMapListEventListener listener = new IMapListEventListener() {
        @Override
        public void onOpenMap(MapInfo map) { }

        @Override
        public void onLoadedMaps(ExtendedMapInfo[] maps) { }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_list_view, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressText = view.findViewById(R.id.progressText);

        noMapsView = view.findViewById(R.id.no_maps);
        noMapsView.setOnClickListener(this);

        emptyView = view.findViewById(R.id.empty);

        list = view.findViewById(R.id.list);
        list.setOnItemClickListener(this);

        adapter = new MapListAdapter(getActivity(), ApplicationEx.getInstance(getActivity()).getCountryFlagProvider());
        list.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderManager.getInstance(this).initLoader(LOCAL_CATALOG_LOADER, null, this).forceLoad();
    }

    public void setMapListEventListener(IMapListEventListener newListener) {
        listener = newListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listener.onOpenMap(adapter.getItem(position));
    }

    @NonNull
    @Override
    public Loader<MapCatalog> onCreateLoader(int id, Bundle args) {
        ApplicationEx app = ApplicationEx.getInstance(requireActivity());
        return new MapCatalogAsyncTaskLoader(requireContext(), app);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<MapCatalog> loader, MapCatalog data) {
        if (data == null) {
            return;
        }
        localMapCatalog = data;
        resetAdapter();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<MapCatalog> loader) { }

    @Override
    public boolean onClose() {
        filterValue = null;
        adapter.getFilter().filter(null);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        filterValue = s;
        adapter.getFilter().filter(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        filterValue = s;
        adapter.getFilter().filter(s);
        return true;
    }

    private void setNoMapsShown() {
        list.setEmptyView(null);
        progressText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        noMapsView.setVisibility(View.VISIBLE);
    }

    private void setListShown() {
        progressText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        noMapsView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        list.setVisibility(View.VISIBLE);
        list.setEmptyView(emptyView);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        final int checkedCount = list.getCheckedItemCount();
        mode.setTitle(checkedCount + " " + getText(R.string.msg_selected));
        adapter.toggleSelection(position);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.map_list_context_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.clearSelection();
    }

    @Override
    public void onClick(View v) {
        // do nothing
    }

    public interface IMapListEventListener {
        void onOpenMap(MapInfo map);
        void onLoadedMaps(ExtendedMapInfo[] maps);
    }

    private void resetAdapter() {
        if (localMapCatalog == null || localMapCatalog.getMaps().length == 0) {
            adapter.clear();
            adapter.getFilter().filter(filterValue);
            setNoMapsShown();
            return;
        }

        MapInfo[] localMaps = localMapCatalog.getMaps();
        ExtendedMapInfo[] maps = new ExtendedMapInfo[localMaps.length];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new ExtendedMapInfo(localMaps[i],ExtendedMapStatus.Installed);
        }

        adapter.clear();
        adapter.addAll(maps);
        adapter.getFilter().filter(filterValue);

        setListShown();
        listener.onLoadedMaps(maps);
    }

    private static class MapCatalogAsyncTaskLoader extends AsyncTaskLoader<MapCatalog> {
        private final ApplicationEx app;

        public MapCatalogAsyncTaskLoader(Context context, ApplicationEx app) {
            super(context);
            this.app = app;
        }

        @Override
        public MapCatalog loadInBackground() {
            return app.getMapCatalogProvider().getMapCatalog();
        }
    }

}
