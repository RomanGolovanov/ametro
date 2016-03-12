package org.ametro.ui.toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class FragmentPagerArrayAdapter extends FragmentPagerAdapter {
    private final List<FragmentPagerTabInfo> tabs;

    public FragmentPagerArrayAdapter(FragmentManager fm, List<FragmentPagerTabInfo> tabs) {
        super(fm);
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
