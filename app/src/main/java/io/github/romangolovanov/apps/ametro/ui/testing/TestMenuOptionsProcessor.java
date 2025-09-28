package io.github.romangolovanov.apps.ametro.ui.testing;

import android.app.Activity;
import android.view.MenuItem;
import android.widget.Toast;

import io.github.romangolovanov.apps.ametro.R;
import io.github.romangolovanov.apps.ametro.app.ApplicationEx;
import io.github.romangolovanov.apps.ametro.catalog.MapCatalogProvider;
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo;

public class TestMenuOptionsProcessor {

    private final Activity activity;
    private final ApplicationEx app;

    public TestMenuOptionsProcessor(Activity activity) {
        this.activity = activity;
        this.app = ApplicationEx.getInstance(activity);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_test_outdated) {
            MapCatalogProvider localMapCatalogProvider = app.getMapCatalogProvider();
            MapInfo[] maps = localMapCatalogProvider.getMapCatalog().getMaps();
            if (maps.length != 0) {
                MapInfo outdatedFirstMap = new MapInfo(
                        maps[0].getCityId(),
                        maps[0].getFileName(),
                        maps[0].getLatitude(),
                        maps[0].getLongitude(),
                        maps[0].getSize(),
                        maps[0].getTimestamp() - 100,
                        maps[0].getTypes(),
                        maps[0].getUid(),
                        maps[0].getCity(),
                        maps[0].getCountry(),
                        maps[0].getIso()
                );

                DebugToast.show(activity, "Map " + outdatedFirstMap.getFileName() + " made outdated in local storage", Toast.LENGTH_LONG);
            }
            return true;
        }
        return false;
    }
}
