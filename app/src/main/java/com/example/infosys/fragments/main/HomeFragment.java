package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.infosys.R;
import com.example.infosys.adapters.UserItemAdapter;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.model.User;

import java.util.List;


public class HomeFragment extends BaseFragment {

    private RecyclerView nearbyUsersRecyclerView;
    private UserItemAdapter userItemAdapter;
    private List<User> nearbyUsersList;

    private ViewPager2 communityViewPager;
    private LinearLayout dotIndicator;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

}

