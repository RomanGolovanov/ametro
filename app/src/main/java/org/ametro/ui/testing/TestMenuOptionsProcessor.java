package org.ametro.ui.testing;

import android.app.Activity;
import android.view.MenuItem;
import android.widget.Toast;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.catalog.MapCatalogManager;
import org.ametro.catalog.entities.MapInfo;

public class TestMenuOptionsProcessor {

    private final Activity activity;
    private final ApplicationEx app;

    public TestMenuOptionsProcessor(Activity activity) {
        this.activity = activity;
        this.app = ApplicationEx.getInstance(activity);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_test_outdated:
                MapCatalogManager localMapCatalogManager = app.getLocalMapCatalogManager();
                MapInfo[] maps = localMapCatalogManager.getMapCatalog().getMaps();
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

                    localMapCatalogManager.addOrReplaceMapAll(new MapInfo[]{outdatedFirstMap});
                    DebugToast.show(activity, "Map " + outdatedFirstMap.getFileName() + " made outdated in local storage", Toast.LENGTH_LONG);
                }
                return true;
        }
        return false;
    }
}
