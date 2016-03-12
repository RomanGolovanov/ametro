package org.ametro.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.model.MapContainer;
import org.ametro.model.entities.MapStationInformation;

public class StationMapFragment extends Fragment {

    public StationMapFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_station_map_view, container, false);
        Bundle arguments = getArguments();
        setupWebView((WebView) rootView.findViewById(R.id.web),
                arguments.getString(Constants.LINE_NAME),
                arguments.getString(Constants.STATION_NAME));
        return rootView;
    }

    public void setupWebView(WebView webView, String lineName, String stationName) {
        ApplicationEx application = ApplicationEx.getInstance(getActivity());
        try {
            MapContainer container = application.getContainer();

            webView.setInitialScale(1);
            WebSettings settings = webView.getSettings();
            settings.setSupportZoom(true);
            settings.setDisplayZoomControls(false);
            settings.setBuiltInZoomControls(true);
            settings.setUseWideViewPort(true);

            MapStationInformation station = container.findStationInformation(lineName, stationName);
            String mapInSvgFormat = container.loadStationMap(station.getMapFilePath());
            webView.loadDataWithBaseURL(
                    "not_defined",
                    mapInSvgFormat,
                    "image/svg+xml",
                    "utf-8",
                    "not_defined");

            webView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            webView.setVisibility(View.GONE);
        }
    }
}
