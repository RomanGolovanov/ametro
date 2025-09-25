package org.ametro.ui.toolbar;

import androidx.fragment.app.Fragment;          // AndroidX Fragment
import androidx.fragment.app.FragmentManager;  // AndroidX FragmentManager
import androidx.fragment.app.FragmentPagerAdapter; // AndroidX PagerAdapter

import java.util.List;

public class FragmentPagerArrayAdapter extends FragmentPagerAdapter {
    private final List<FragmentPagerTabInfo> tabs;

    public FragmentPagerArrayAdapter(FragmentManager fm, List<FragmentPagerTabInfo> tabs) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); // ✅ use modern behavior
        this.tabs = tabs;
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position).getFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle();
    }
}
