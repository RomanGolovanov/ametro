package io.github.romangolovanov.apps.ametro.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.ApplicationEx
import io.github.romangolovanov.apps.ametro.app.Constants

class StationAboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_station_about_view, container, false)
        val arguments = requireArguments()
        setupWebView(
            rootView.findViewById(R.id.web),
            arguments.getString(Constants.LINE_NAME),
            arguments.getString(Constants.STATION_NAME)
        )
        return rootView
    }

    fun setupWebView(webView: WebView, lineName: String?, stationName: String?) {
        val application = ApplicationEx.getInstance(requireActivity())
        val mapContainer = application.container!!
        val station = mapContainer.findStationInformation(lineName!!, stationName!!)!!

        webView.setInitialScale(1)
        val settings: WebSettings = webView.settings
        settings.setSupportZoom(true)
        settings.displayZoomControls = false
        settings.builtInZoomControls = true
        settings.useWideViewPort = true

        webView.loadDataWithBaseURL(
            "not_defined",
            station.about ?: "",
            "text/html",
            "utf-8",
            "not_defined"
        )

        webView.visibility = View.VISIBLE
    }
}
