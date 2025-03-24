package com.example.infosys.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager2.widget.ViewPager2;

import com.example.infosys.R;
import com.example.infosys.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CommunitiesFragment extends BaseFragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public CommunitiesFragment() {
        // Required empty public constructor
    }

    public static CommunitiesFragment newInstance(String param1, String param2) {
        CommunitiesFragment fragment = new CommunitiesFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_communities, container, false);

        // Initialize the TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.pager);

        // Create an adapter for ViewPager2
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Set up TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Your Communities");
                    break;
                case 1:
                    tab.setText("All Communities");
                    break;
            }
        }).attach();

        return view;
    }
}