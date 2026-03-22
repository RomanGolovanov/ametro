package io.github.romangolovanov.apps.ametro.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.ApplicationEx
import io.github.romangolovanov.apps.ametro.catalog.entities.MapCatalog
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo
import io.github.romangolovanov.apps.ametro.ui.adapters.MapListAdapter
import io.github.romangolovanov.apps.ametro.ui.loaders.ExtendedMapInfo
import io.github.romangolovanov.apps.ametro.ui.loaders.ExtendedMapStatus

class MapListFragment : Fragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    LoaderManager.LoaderCallbacks<MapCatalog>,
    AdapterView.OnItemClickListener,
    AbsListView.MultiChoiceModeListener,
    View.OnClickListener {

    private lateinit var adapter: MapListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var noMapsView: View
    private lateinit var emptyView: View
    private lateinit var list: ListView

    private var filterValue: String? = null
    private var localMapCatalog: MapCatalog? = null

    private var listener: IMapListEventListener = object : IMapListEventListener {
        override fun onOpenMap(map: MapInfo) {}
        override fun onLoadedMaps(maps: Array<ExtendedMapInfo>) {}
    }

    companion object {
        private const val LOCAL_CATALOG_LOADER = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map_list_view, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)

        noMapsView = view.findViewById(R.id.no_maps)
        noMapsView.setOnClickListener(this)

        emptyView = view.findViewById(R.id.empty)

        list = view.findViewById(R.id.list)
        list.setOnItemClickListener(this)

        adapter = MapListAdapter(requireActivity(), ApplicationEx.getInstance(requireActivity()).countryFlagProvider)
        list.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        LoaderManager.getInstance(this).initLoader(LOCAL_CATALOG_LOADER, null, this).forceLoad()
    }

    fun setMapListEventListener(newListener: IMapListEventListener) {
        listener = newListener
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        listener.onOpenMap(adapter.getItem(position)!!)
    }

    @NonNull
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<MapCatalog> {
        val app = ApplicationEx.getInstance(requireActivity())
        return MapCatalogAsyncTaskLoader(requireContext(), app)
    }

    override fun onLoadFinished(@NonNull loader: Loader<MapCatalog>, data: MapCatalog?) {
        if (data == null) return
        localMapCatalog = data
        resetAdapter()
    }

    override fun onLoaderReset(@NonNull loader: Loader<MapCatalog>) {}

    override fun onClose(): Boolean {
        filterValue = null
        adapter.filter.filter(null)
        return true
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        filterValue = s
        adapter.filter.filter(s)
        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        filterValue = s
        adapter.filter.filter(s)
        return true
    }

    private fun setNoMapsShown() {
        list.emptyView = null
        progressText.visibility = View.GONE
        progressBar.visibility = View.GONE
        list.visibility = View.GONE
        emptyView.visibility = View.GONE
        noMapsView.visibility = View.VISIBLE
    }

    private fun setListShown() {
        progressText.visibility = View.GONE
        progressBar.visibility = View.GONE
        noMapsView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        list.visibility = View.VISIBLE
        list.emptyView = emptyView
    }

    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
        val checkedCount = list.checkedItemCount
        mode.title = "$checkedCount ${getText(R.string.msg_selected)}"
        adapter.toggleSelection(position)
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = false

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val menuInflater: MenuInflater = mode.menuInflater
        menuInflater.inflate(R.menu.map_list_context_menu, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = true

    override fun onDestroyActionMode(mode: ActionMode) {
        adapter.clearSelection()
    }

    override fun onClick(v: View) {
        // do nothing
    }

    interface IMapListEventListener {
        fun onOpenMap(map: MapInfo)
        fun onLoadedMaps(maps: Array<ExtendedMapInfo>)
    }

    private fun resetAdapter() {
        val catalog = localMapCatalog
        if (catalog == null || catalog.maps.isEmpty()) {
            adapter.clear()
            adapter.filter.filter(filterValue)
            setNoMapsShown()
            return
        }

        val localMaps = catalog.maps
        val maps = Array(localMaps.size) { i -> ExtendedMapInfo(localMaps[i], ExtendedMapStatus.Installed) }

        adapter.clear()
        adapter.addAll(*maps)
        adapter.filter.filter(filterValue)

        setListShown()
        listener.onLoadedMaps(maps)
    }

    private class MapCatalogAsyncTaskLoader(context: Context, private val app: ApplicationEx) :
        AsyncTaskLoader<MapCatalog>(context) {

        override fun loadInBackground(): MapCatalog {
            return app.mapCatalogProvider.mapCatalog
        }
    }
}
