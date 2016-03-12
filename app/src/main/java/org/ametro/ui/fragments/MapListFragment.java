package org.ametro.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
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
        AbsListView.MultiChoiceModeListener, View.OnClickListener {

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

    private final static int LOCAL_CATALOG_LOADER = 1;
    private final static int REMOTE_CATALOG_LOADER = 2;

    private IMapListEventListener listener = new IMapListEventListener() {
        @Override
        public void onOpenMap(MapInfo map) {
        }

        @Override
        public void onDeleteMaps(MapInfo[] map) {
        }

        @Override
        public void onLoadedMaps(ExtendedMapInfo[] maps) {
        }

        @Override
        public void onAddMap() {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_list_view, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressText = (TextView) view.findViewById(R.id.progressText);

        noMapsView = view.findViewById(R.id.no_maps);
        noMapsView.setOnClickListener(this);

        emptyView = view.findViewById(R.id.empty);

        list = (ListView) view.findViewById(R.id.list);
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
        getLoaderManager().initLoader(LOCAL_CATALOG_LOADER, null, this).forceLoad();
        getLoaderManager().initLoader(REMOTE_CATALOG_LOADER, null, this).forceLoad();
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
        final ApplicationEx app = ApplicationEx.getInstance(MapListFragment.this.getActivity());
        switch(id){
            case LOCAL_CATALOG_LOADER:
                return new AsyncTaskLoader<MapCatalog>(getActivity()) {
                    @Override
                    public MapCatalog loadInBackground() {
                        return app.getLocalMapCatalogManager().getMapCatalog();
                    }
                };
            case REMOTE_CATALOG_LOADER:
                return new AsyncTaskLoader<MapCatalog>(getActivity()) {
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
        if(data == null){
            return;
        }
        switch(loader.getId()){
            case LOCAL_CATALOG_LOADER:
                localMapCatalog = data;
                break;
            case REMOTE_CATALOG_LOADER:
                remoteMapCatalog = data;
        }
        resetAdapter();
    }

    @Override
    public void onLoaderReset(Loader<MapCatalog> loader) {
    }

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
    public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
        final int checkedCount = list.getCheckedItemCount();
        mode.setTitle(checkedCount + " " + getText(R.string.msg_selected));
        adapter.toggleSelection(position);
    }

    @Override
    public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.map_list_context_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                listener.onDeleteMaps(adapter.getSelection());
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(android.view.ActionMode mode) {
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
        if(localMapCatalog == null || localMapCatalog.getMaps().length == 0){
            adapter.clear();
            adapter.getFilter().filter(filterValue);
            setNoMapsShown();
            return;
        }

        MapInfo[] localMaps = localMapCatalog.getMaps();

        ExtendedMapInfo[] maps = new ExtendedMapInfo[localMaps.length];
        for(int i=0;i< maps.length;i++){
            maps[i] = new ExtendedMapInfo(localMaps[i], remoteMapCatalog==null
                    ? ExtendedMapStatus.Fetching
                    : getMapStatus(localMaps[i], remoteMapCatalog)
            );
        }

        if (actionModeSelection.size() > 0) {
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
        if(remoteMap == null){
            return ExtendedMapStatus.Unknown;
        }
        if(remoteMap.getTimestamp() == map.getTimestamp()){
            return ExtendedMapStatus.Installed;
        }
        return ExtendedMapStatus.Outdated;
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
            switch (item.getItemId()) {
                case R.id.action_delete:
                    listener.onDeleteMaps(adapter.getSelection());
                    mode.finish();
                    return true;
                default:
                    return false;
            }
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
