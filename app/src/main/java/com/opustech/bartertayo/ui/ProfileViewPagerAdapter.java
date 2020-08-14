package com.opustech.bartertayo.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class ProfileViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;

    public ProfileViewPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    public void add(Fragment fragment) {
        this.fragments.add(fragment);
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
            title = "Posts";
        }
        else if (position == 1)
        {
            title = "Deals";
        }
        return title;
    }
}
