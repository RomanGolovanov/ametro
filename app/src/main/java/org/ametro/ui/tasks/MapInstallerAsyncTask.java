package org.ametro.ui.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import org.ametro.app.ApplicationEx;
import org.ametro.catalog.MapCatalogManager;
import org.ametro.catalog.RemoteMapCatalogProvider;
import org.ametro.catalog.entities.MapCatalog;
import org.ametro.catalog.entities.MapInfo;
import org.ametro.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapInstallerAsyncTask extends AsyncTask<Void, Object, Throwable> {

    private final IMapInstallerEventListener listener;
    private final ApplicationEx application;
    private final MapInfo[] maps;
    private final PowerManager.WakeLock wakeLock;
    private long totalSize = 0;
    private long currentSize = 0;

    public MapInstallerAsyncTask(Activity context, IMapInstallerEventListener listener, MapInfo[] maps) {
        this.listener = listener;
        this.maps = maps;
        this.application = ApplicationEx.getInstance(context);
        this.wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        totalSize = getSize(maps);
        MapCatalogManager localManager = null;
        try {
            wakeLock.acquire();
            RemoteMapCatalogProvider remoteProvider = application.getRemoteMapCatalogProvider();
            localManager = application.getLocalMapCatalogManager();
            
            publishProgress(currentSize, totalSize, maps[0]);

            List<MapInfo> updatedMaps = new ArrayList<>();
            MapCatalog remoteMapCatalog = remoteProvider.getMapCatalog(false);

            for (MapInfo map : maps) {
                publishProgress(currentSize, totalSize, map);
                if (isCancelled()) {
                    throw new InterruptedException();
                }
                URI source = remoteProvider.getMapFileUrl(map);
                File destination = localManager.getTempMapFile(map);
                downloadMap(map, source, destination);

                updatedMaps.add(remoteMapCatalog.findMap(map.getFileName()));
            }

            for (MapInfo map : maps) {
                FileUtils.move(localManager.getTempMapFile(map), localManager.getMapFile(map));
            }
            localManager.addOrReplaceMapAll(updatedMaps.toArray(new MapInfo[updatedMaps.size()]));
        } catch (Throwable ex) {
            return ex;
        } finally {
            if(localManager!=null) {
                for (MapInfo map : maps) {
                    FileUtils.safeDelete(localManager.getTempMapFile(map));
                }
            }
            wakeLock.release();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... progress) {
        super.onProgressUpdate(progress);
        listener.onMapDownloadingProgress((long) progress[0], (long) progress[1], (MapInfo) progress[2]);
    }

    @Override
    protected void onPostExecute(Throwable reason) {
        if (reason != null) {
            listener.onMapDownloadingFailed(maps, reason);
        } else {
            listener.onMapDownloadingComplete(maps);
        }
    }

    @Override
    protected void onCancelled(Throwable throwable) {
        super.onCancelled(throwable);
        listener.onMapDownloadingFailed(maps, throwable);
    }

    private long getSize(MapInfo[] maps) {
        long size = 0;
        for (MapInfo m : maps) {
            size += m.getSize();
        }
        return size;
    }

    private void downloadMap(MapInfo map, URI source, File destination) throws InterruptedException, IOException {
        if (destination.exists()) {
            FileUtils.delete(destination);
        }
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(source.toASCIIString());
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            input = connection.getInputStream();
            output = new FileOutputStream(destination);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    throw new InterruptedException();
                }
                output.write(data, 0, count);
                currentSize += count;
                publishProgress(currentSize, totalSize, map);
            }
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            if (connection != null)
                connection.disconnect();
        }
    }

    public interface IMapInstallerEventListener {
        void onMapDownloadingComplete(MapInfo[] maps);

        void onMapDownloadingProgress(long currentSize, long totalSize, MapInfo downloadingMap);

        void onMapDownloadingFailed(MapInfo[] maps, Throwable reason);

    }
}
