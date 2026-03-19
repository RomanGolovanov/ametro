package io.github.romangolovanov.apps.ametro.ui.testing

import android.app.Activity
import android.view.MenuItem
import android.widget.Toast
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.ApplicationEx
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo

class TestMenuOptionsProcessor(private val activity: Activity) {

    private val app: ApplicationEx = ApplicationEx.getInstance(activity)

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_test_outdated) {
            val localMapCatalogProvider = app.mapCatalogProvider
            val maps = localMapCatalogProvider.mapCatalog.maps
            if (maps.isNotEmpty()) {
                val outdatedFirstMap = MapInfo(
                    maps[0].cityId,
                    maps[0].fileName,
                    maps[0].latitude,
                    maps[0].longitude,
                    maps[0].size,
                    maps[0].timestamp - 100,
                    maps[0].types,
                    maps[0].uid,
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
