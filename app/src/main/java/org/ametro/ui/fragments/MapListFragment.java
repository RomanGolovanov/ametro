package org.ametro.ui.fragments;

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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.widget.SearchView;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.catalog.entities.MapInfoHelpers;
import org.ametro.ui.adapters.MapListAdapter;
import org.ametro.ui.loaders.ExtendedMapInfo;
import org.ametro.ui.loaders.ExtendedMapStatus;

import java.util.HashSet;
import java.util.Set;

public class MapListFragment extends Fragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        LoaderManager.LoaderCallbacks<MapCatalog>,
        AdapterView.OnItemClickListener,
        AbsListView.MultiChoiceModeListener,
        View.OnClickListener {

    private static final String STATE_ACTION_MODE = "STATE_ACTION_MODE";
    private static final String STATE_SELECTION = "STATE_SELECTION";

    private MapListAdapter adapter;
    private ProgressBar progressBar;
    private TextView progressText;
    private View noMapsView;
    private View emptyView;

    private String filterValue;

    private MapCatalog localMapCatalog;
    private MapCatalog remoteMapCatalog;

    private ListView list;
    private ActionMode actionMode;
    private Set<String> actionModeSelection = new HashSet<>();

    private static final int LOCAL_CATALOG_LOADER = 1;
    private static final int REMOTE_CATALOG_LOADER = 2;

    private IMapListEventListener listener = new IMapListEventListener() {
        @Override
        public void onOpenMap(MapInfo map) { }

        @Override
        public void onDeleteMaps(MapInfo[] map) { }

        @Override
        public void onLoadedMaps(ExtendedMapInfo[] maps) { }

        @Override
        public void onAddMap() { }
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
        list.setLongClickable(true);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(this);

        adapter = new MapListAdapter(getActivity(), ApplicationEx.getInstance(getActivity()).getCountryFlagProvider());
        list.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        forceUpdate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ACTION_MODE, actionMode != null);
        outState.putStringArrayList(STATE_SELECTION, MapInfoHelpers.toFileNameArray(adapter.getSelection()));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.getBoolean(STATE_ACTION_MODE)) {
            actionMode = list.startActionMode(new ContextualActionModeCallback());
        }
        actionModeSelection.clear();
        actionModeSelection.addAll(savedInstanceState.getStringArrayList(STATE_SELECTION));
    }

    public void forceUpdate() {
        LoaderManager.getInstance(this).initLoader(LOCAL_CATALOG_LOADER, null, this).forceLoad();
        LoaderManager.getInstance(this).initLoader(REMOTE_CATALOG_LOADER, null, this).forceLoad();
    }

    public void startContextActionMode() {
        actionMode = list.startActionMode(new ContextualActionModeCallback());
    }

    public void setMapListEventListener(IMapListEventListener newListener) {
        listener = newListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (actionMode != null) {
            final int checkedCount = list.getCheckedItemCount();
            actionMode.setTitle(checkedCount + " Selected");
            adapter.toggleSelection(position);
            return;
        }
        listener.onOpenMap(adapter.getItem(position));
    }

    @Override
    public Loader<MapCatalog> onCreateLoader(int id, Bundle args) {
        final ApplicationEx app = ApplicationEx.getInstance(requireActivity());
        switch (id) {
            case LOCAL_CATALOG_LOADER:
                return new AsyncTaskLoader<MapCatalog>(requireActivity()) {
                    @Override
                    public MapCatalog loadInBackground() {
                        return app.getLocalMapCatalogManager().getMapCatalog();
                    }
                };
            case REMOTE_CATALOG_LOADER:
                return new AsyncTaskLoader<MapCatalog>(requireActivity()) {
                    @Override
                    public MapCatalog loadInBackground() {
                        return app.getRemoteMapCatalogProvider().getMapCatalog(false);
                    }
                };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<MapCatalog> loader, MapCatalog data) {
        if (data == null) {
            return;
        }
        switch (loader.getId()) {
            case LOCAL_CATALOG_LOADER:
                localMapCatalog = data;
                break;
            case REMOTE_CATALOG_LOADER:
                remoteMapCatalog = data;
                break;
        }
        resetAdapter();
    }

    @Override
    public void onLoaderReset(Loader<MapCatalog> loader) { }

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
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            listener.onDeleteMaps(adapter.getSelection());
            mode.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.clearSelection();
        actionModeSelection.clear();
    }

    @Override
    public void onClick(View v) {
        listener.onAddMap();
    }

    public interface IMapListEventListener {
        void onOpenMap(MapInfo map);
        void onDeleteMaps(MapInfo[] map);
        void onLoadedMaps(ExtendedMapInfo[] maps);
        void onAddMap();
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
            maps[i] = new ExtendedMapInfo(localMaps[i],
                    remoteMapCatalog == null
                            ? ExtendedMapStatus.Fetching
                            : getMapStatus(localMaps[i], remoteMapCatalog));
        }

        if (!actionModeSelection.isEmpty()) {
            for (ExtendedMapInfo map : maps) {
                if (actionModeSelection.contains(map.getFileName())) {
                    map.setSelected(true);
                }
            }
        }

        adapter.clear();
        adapter.addAll(maps);
        adapter.getFilter().filter(filterValue);

        setListShown();
        listener.onLoadedMaps(maps);
    }

    private ExtendedMapStatus getMapStatus(MapInfo map, MapCatalog catalog) {
        MapInfo remoteMap = catalog.findMap(map.getFileName());
        if (remoteMap == null) {
            return ExtendedMapStatus.Unknown;
        }
        return (remoteMap.getTimestamp() == map.getTimestamp())
                ? ExtendedMapStatus.Installed
                : ExtendedMapStatus.Outdated;
    }

    private class ContextualActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.map_list_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                listener.onDeleteMaps(adapter.getSelection());
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            adapter.clearSelection();
            actionModeSelection.clear();
            actionMode = null;
        }
    }
}
