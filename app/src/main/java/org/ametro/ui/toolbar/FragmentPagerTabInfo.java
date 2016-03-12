package org.ametro.ui.toolbar;

import android.support.v4.app.Fragment;

public class FragmentPagerTabInfo {
    private final CharSequence title;
    private final Fragment fragment;

    public FragmentPagerTabInfo(CharSequence title, Fragment fragment) {
        this.title = title;
        this.fragment = fragment;
    }

    public CharSequence getTitle() {
        return title;
    }

    public Fragment getFragment() {
        return fragment;
    }
}

