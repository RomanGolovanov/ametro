package io.github.romangolovanov.apps.ametro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;  // ✅ AndroidX

import io.github.romangolovanov.apps.ametro.R;
import io.github.romangolovanov.apps.ametro.app.ApplicationEx;
import io.github.romangolovanov.apps.ametro.app.Constants;
import io.github.romangolovanov.apps.ametro.model.MapContainer;
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation;

public class StationAboutFragment extends Fragment {

    public StationAboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_station_about_view, container, false);
        Bundle arguments = getArguments();
        setupWebView((WebView) rootView.findViewById(R.id.web),
                arguments.getString(Constants.LINE_NAME),
                arguments.getString(Constants.STATION_NAME));
        return rootView;
    }

    public void setupWebView(WebView webView, String lineName, String stationName) {
        ApplicationEx application = ApplicationEx.getInstance(requireActivity()); // ✅ safer
        MapContainer container = application.getContainer();
        MapStationInformation station = container.findStationInformation(lineName, stationName);

        webView.setInitialScale(1);
        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);

        webView.loadDataWithBaseURL(
                "not_defined",
                station.getAbout(),
                "text/html",
                "utf-8",
                "not_defined");

        webView.setVisibility(View.VISIBLE);
    }
}
