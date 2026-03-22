package io.github.romangolovanov.apps.ametro.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.NavUtils
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.Constants
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo
import io.github.romangolovanov.apps.ametro.ui.fragments.MapListFragment
import io.github.romangolovanov.apps.ametro.ui.loaders.ExtendedMapInfo

class MapList : AppCompatActivity(), MapListFragment.IMapListEventListener {

    private lateinit var listFragment: MapListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_list_view)

        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        listFragment = supportFragmentManager.findFragmentById(R.id.list) as MapListFragment
        listFragment.setMapListEventListener(this)
    }

    override fun onOpenMap(map: MapInfo) {
        val viewIntent = Intent()
        viewIntent.putExtra(Constants.MAP_PATH, map.fileName)
        setResult(RESULT_OK, viewIntent)
        finish()
    }

    override fun onLoadedMaps(maps: Array<ExtendedMapInfo>) {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map_list, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(listFragment)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
