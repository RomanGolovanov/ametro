package io.github.romangolovanov.apps.ametro.ui.testing

import android.app.Activity
import android.view.MenuItem
import android.widget.Toast
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.ApplicationEx
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfoEntity

class TestMenuOptionsProcessor(private val activity: Activity) {

    private val app: ApplicationEx = ApplicationEx.getInstance(activity)

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_test_outdated) {
            val localMapCatalogProvider = app.mapCatalogProvider
            val maps = localMapCatalogProvider.mapCatalog.maps
            if (maps.isNotEmpty()) {
                val outdatedFirstMap = MapInfo(
                    MapInfoEntity(
                        uid = maps[0].uid,
                        cityId = maps[0].cityId,
                        types = maps[0].types,
                        fileName = maps[0].fileName,
                        size = maps[0].size,
                        timestamp = maps[0].timestamp - 100,
                        latitude = maps[0].latitude,
                        longitude = maps[0].longitude
                    ),
                    maps[0].city,
                    maps[0].country,
                    maps[0].iso
                )

                DebugToast.show(activity, "Map " + outdatedFirstMap.fileName + " made outdated in local storage", Toast.LENGTH_LONG)
            }
            return true
        }
        return false
    }
}
