package com.opustech.bartertayo.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ProfileViewPagerAdapter extends FragmentPagerAdapter {

    public ProfileViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0)
        {
            fragment = new ProfileUserPostsFragment();
        }
        else if (position == 1)
        {
            fragment = new ProfileUserDealsFragment();
        }
        return fragment;
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
