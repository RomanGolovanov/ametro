package io.github.romangolovanov.apps.ametro.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.ApplicationEx
import io.github.romangolovanov.apps.ametro.app.Constants
import io.github.romangolovanov.apps.ametro.ui.fragments.StationMapFragment

class StationDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_details_view)

        val tabs = buildTabs()

        val viewPager = findViewById<ViewPager2>(R.id.viewpager)
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int): Fragment = tabs[position].second()
        }

        TabLayoutMediator(findViewById<TabLayout>(R.id.tab_layout), viewPager) { tab, position ->
            tab.text = tabs[position].first
        }.attach()

        val app = ApplicationEx.getInstance(this)
        val station = app.container?.findSchemeStation(
            app.schemeName ?: "",
            intent.getStringExtra(Constants.LINE_NAME) ?: "",
            intent.getStringExtra(Constants.STATION_NAME) ?: ""
        )

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDefaultDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = station?.displayName ?: intent.getStringExtra(Constants.STATION_NAME) ?: ""
        }
    }

    /** Returns list of (tab title, fragment factory) pairs. */
    private fun buildTabs(): List<Pair<String, () -> Fragment>> {
        val extras = intent.extras
        return listOf(
            getString(R.string.tab_map) to {
                StationMapFragment().apply { arguments = extras }
            }
            // Uncomment to enable the About tab:
            // getString(R.string.tab_about) to {
            //     StationAboutFragment().apply { arguments = extras }
            // }
        )
    }
}
