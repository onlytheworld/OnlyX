package com.OnlyX.ui.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.OnlyX.ui.fragment.BaseFragment;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class TabPagerAdapter extends FragmentPagerAdapter {

    private final BaseFragment[] fragment;
    private final String[] title;

    public TabPagerAdapter(FragmentManager manager, BaseFragment[] fragment, String[] title) {
        super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragment = fragment;
        this.title = title;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragment[position];
    }

    @Override
    public int getCount() {
        return fragment.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }

}
