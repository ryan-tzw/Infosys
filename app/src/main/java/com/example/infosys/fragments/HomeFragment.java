package com.example.infosys.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.example.infosys.managers.FirebaseManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

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
            // Add dummy users to Firebase
            FirebaseManager firebaseManager = FirebaseManager.getInstance(getContext());

            firebaseManager.addUserToFirestore("user1", "John Doe", "user1@example.com");
            firebaseManager.addUserToFirestore("user2", "Jane Doe", "user2@example.com");
            firebaseManager.addUserToFirestore("user3", "Alice Smith", "user3@example.com");
            firebaseManager.addUserToFirestore("user4", "Bob Johnson", "user4@example.com");
            firebaseManager.addUserToFirestore("user5", "Charlie Brown", "user5@example.com");

        });

        return view;
    }
}