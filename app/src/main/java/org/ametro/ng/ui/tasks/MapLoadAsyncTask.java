package org.ametro.ng.ui.tasks;

import android.os.AsyncTask;

import org.ametro.ng.model.MapContainer;

public class MapLoadAsyncTask extends AsyncTask<Void, String, Throwable> {

    private static final String DEFAULT_SCHEME = "metro";
    private static final String[] DEFAULT_TRANSPORTS = null;

    private final MapContainer container;
    private final String schemeName;
    private final String[] enabledTransports;

    private final IMapLoadingEventListener listener;

    private long start;
    private long end;

    public MapLoadAsyncTask(IMapLoadingEventListener listener, MapContainer container) {
        this(listener, container, DEFAULT_SCHEME, DEFAULT_TRANSPORTS);
    }

    public MapLoadAsyncTask(IMapLoadingEventListener listener,
                            MapContainer container,
                            String schemeName,
                            String[] enabledTransports) {
        this.listener = listener;
        this.container = container;
        this.schemeName = schemeName;
        this.enabledTransports = enabledTransports;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            start = System.currentTimeMillis();
            container.loadSchemeWithTransports(schemeName, null);
            end = System.currentTimeMillis();
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Throwable reason) {
        if (reason == null) {
            listener.onMapLoadComplete(container, schemeName, enabledTransports, end - start);
        } else {
            listener.onMapLoadFailed(container, schemeName, enabledTransports, reason);
        }
    }

    public interface IMapLoadingEventListener {
        void onMapLoadComplete(MapContainer container, String schemeName, String[] enabledTransports, long time);

        void onMapLoadFailed(MapContainer container, String schemeName, String[] enabledTransports, Throwable reason);
    }
}
