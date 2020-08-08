package com.opustech.bartertayo.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class HomeViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;

    public HomeViewPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
    }

    public void add(Fragment fragment) {
        this.fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0)
        {
            title = "News";
        }
        else if (position == 1)
        {
            title = "Posts";
        }
        return title;
    }
}
