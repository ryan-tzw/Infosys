package com.example.infosys.fragments.main.nav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.example.infosys.fragments.main.CommunitiesFragment;

public class NavCommunitiesFragment extends Fragment {
    public NavCommunitiesFragment() {
        // Required empty public constructor
    }

    public static NavCommunitiesFragment newInstance(String param1, String param2) {
        NavCommunitiesFragment fragment = new NavCommunitiesFragment();
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
        return inflater.inflate(R.layout.fragment_nav_communities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.communities_nav_container, new CommunitiesFragment())
                    .commit();
        }
    }
}