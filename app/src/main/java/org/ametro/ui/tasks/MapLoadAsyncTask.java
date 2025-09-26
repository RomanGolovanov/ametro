package org.ametro.ui.tasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import org.ametro.model.MapContainer;

public class MapLoadAsyncTask extends AsyncTask<Void, String, Throwable> {

    private static final String DEFAULT_SCHEME = "metro";
    private static final String[] DEFAULT_TRANSPORTS = null;

    private final MapContainer container;
    private final String schemeName;
    private final String[] enabledTransports;

    private final IMapLoadingEventListener listener;

    private long start;
    private long end;

    public MapLoadAsyncTask(AppCompatActivity activity,
                            IMapLoadingEventListener listener,
                            MapContainer container) {
        this(activity, listener, container, DEFAULT_SCHEME, DEFAULT_TRANSPORTS);
    }

    public MapLoadAsyncTask(Context context,
                            IMapLoadingEventListener listener,
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
        listener.onBeforeMapLoading(container, schemeName, enabledTransports);
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
        void onBeforeMapLoading(MapContainer container, String schemeName, String[] enabledTransports);

        void onMapLoadComplete(MapContainer container, String schemeName, String[] enabledTransports, long time);

        void onMapLoadFailed(MapContainer container, String schemeName, String[] enabledTransports, Throwable reason);
    }
}
