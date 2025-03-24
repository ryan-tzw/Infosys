package com.example.infosys.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.example.infosys.managers.CommunitiesManager;
import com.example.infosys.model.Community;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends BaseFragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
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

        // test button on click listener
        Button testButton = view.findViewById(R.id.test_button);
        testButton.setOnClickListener(v -> {
            // Add dummy communities to Firestore
            CommunitiesManager communitiesManager = CommunitiesManager.getInstance();
            
            communitiesManager.createCommunity(new Community("testCommunity4", "Test Community 4", "This is a test community 4"));
            communitiesManager.createCommunity(new Community("testCommunity5", "Test Community 5", "This is a test community 5"));
        });

        return view;
    }
}