package com.opustech.bartertayo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.opustech.bartertayo.R;

public class HomeFragment extends Fragment {

    private HomeViewPagerAdapter homeViewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = root.findViewById(R.id.homeViewPager);
        tabLayout = root.findViewById(R.id.homeTab);
        this.homeViewPagerAdapter = new HomeViewPagerAdapter(getChildFragmentManager());
        this.homeViewPagerAdapter.add(new NewsFragment());
        this.homeViewPagerAdapter.add(new PostsFragment());
        viewPager.setAdapter(this.homeViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        return root;
    }
}